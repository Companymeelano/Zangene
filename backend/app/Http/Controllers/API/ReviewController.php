<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Product;
use App\Models\Review;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

/**
 * v6.2 — Verified review engine.
 *
 * Mirrors the offline-first boutique of the Android app:
 *   • guests may submit with a display name, customers link to their account,
 *   • every new review enters the moderation queue (is_approved = false),
 *   • only approved reviews reach the public storefront and its rating summary.
 */
class ReviewController extends Controller
{
    private const MIN_BODY = 8;
    private const MAX_BODY = 700;

    /** GET /v1/products/{id}/reviews — approved reviews + rating summary. */
    public function index($productId)
    {
        $product = Product::where('published', true)->findOrFail($productId);

        $reviews = $product->reviews()
            ->with('customer.user')
            ->latest()
            ->limit(20)
            ->get(['id', 'product_id', 'customer_id', 'author_name', 'rating', 'comment', 'created_at']);

        return response()->json([
            'product_id' => (int) $product->id,
            'summary' => $this->summaryFor(Review::where('product_id', $product->id)->where('is_approved', true)),
            'reviews' => $reviews,
        ]);
    }

    /** GET /v1/reviews/summary — boutique-wide social proof for the hero plaque. */
    public function summary()
    {
        return response()->json($this->summaryFor(Review::where('is_approved', true)));
    }

    /** POST /v1/products/{id}/reviews — public submission, queued for moderation. */
    public function store(Request $request, $productId)
    {
        $product = Product::where('published', true)->findOrFail($productId);

        $validated = $request->validate([
            'rating' => ['required', 'integer', 'min:1', 'max:5'],
            'comment' => ['required', 'string', 'min:' . self::MIN_BODY, 'max:' . self::MAX_BODY],
            'author_name' => ['nullable', 'string', 'min:2', 'max:80'],
        ]);

        $customerId = $request->user()?->customer?->id;

        if (!$customerId && empty($validated['author_name'])) {
            return response()->json([
                'message' => 'برای ثبت نظر یا وارد حساب خود شوید یا نام نمایشی را وارد کنید.',
            ], 422);
        }

        $duplicateQuery = Review::where('product_id', $product->id)
            ->where('comment', $validated['comment'])
            ->where('created_at', '>=', now()->subDay());

        if ($customerId) {
            $duplicateQuery->where('customer_id', $customerId);
        } else {
            $duplicateQuery->whereNull('customer_id')
                ->where('author_name', trim($validated['author_name']));
        }

        $duplicate = $duplicateQuery->exists();

        if ($duplicate) {
            return response()->json(['message' => 'همین نظر پیش‌تر ثبت شده و در صف بررسی است.'], 429);
        }

        $review = Review::create([
            'product_id' => $product->id,
            'customer_id' => $customerId,
            'author_name' => $customerId ? null : trim($validated['author_name']),
            'rating' => (int) $validated['rating'],
            'comment' => trim($validated['comment']),
            'is_approved' => false,
        ]);

        return response()->json([
            'message' => 'نظر شما ثبت شد و پس از تأیید مدیریت منتشر می‌شود.',
            'review' => $review->only(['id', 'rating', 'created_at']),
        ], 201);
    }

    /** GET /v1/admin/reviews/pending — moderation queue with the badge count. */
    public function pending()
    {
        $reviews = Review::with(['product:id,name_fa,sku', 'customer.user'])
            ->where('is_approved', false)
            ->latest()
            ->limit(50)
            ->get();

        return response()->json([
            'count' => Review::where('is_approved', false)->count(),
            'reviews' => $reviews,
        ]);
    }

    /** POST /v1/admin/reviews/{id}/approve — publish (or revert) a review. */
    public function approve(Request $request, $id)
    {
        $validated = $request->validate([
            'approved' => ['nullable', 'boolean'],
        ]);

        $review = Review::findOrFail($id);
        $wasApproved = (bool) $review->is_approved;
        $review->is_approved = $validated['approved'] ?? true;
        $review->save();

        // v6.8 · thank the author the moment the review goes live
        if ($review->is_approved && !$wasApproved) {
            try {
                $review->loadMissing('customer.user');
                $user = $review->customer?->user;
                if ($user && $user->sms_optin) {
                    \App\Services\Sms\SmsService::send('review_approved', $user->mobile, [
                        'name' => $user->name ?: 'مشتری گرامی',
                    ]);
                }
            } catch (\Throwable $ignored) {
            }
        }

        return response()->json([
            'message' => $review->is_approved ? 'نظر تأیید و منتشر شد.' : 'انتشار نظر لغو شد.',
            'review' => $review,
        ]);
    }

    /** DELETE /v1/admin/reviews/{id} — discard an abusive review. */
    public function destroy($id)
    {
        $review = Review::findOrFail($id);
        $review->delete();

        return response()->json(['message' => 'نظر حذف شد.']);
    }

    private function summaryFor($query): array
    {
        $count = (clone $query)->count();
        $average = $count > 0 ? round((float) (clone $query)->avg('rating'), 1) : 0.0;

        $distribution = [];
        foreach ([5, 4, 3, 2, 1] as $star) {
            $distribution[(string) $star] = (clone $query)->where('rating', $star)->count();
        }

        return [
            'count' => $count,
            'average' => $average,
            'distribution' => $distribution,
        ];
    }
}
