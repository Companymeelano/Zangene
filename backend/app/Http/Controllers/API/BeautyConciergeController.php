<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Models\Product;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class BeautyConciergeController extends Controller
{
    /**
     * 1. AI Skin Diagnostic & Personalized Morning/Night Luxury Ritual
     */
    public function analyzeSkin(Request $request)
    {
        $validated = $request->validate([
            'skin_type' => ['required', 'in:dry,oily,combination,normal,sensitive'],
            'concern' => ['required', 'in:aging,dullness,dehydration,pores,sensitivity'],
            'age_range' => ['nullable', 'in:18_25,26_35,36_45,46_plus'],
        ]);

        $hydrationScore = match ($validated['skin_type']) {
            'dry' => 62,
            'sensitive' => 68,
            'combination' => 76,
            'oily' => 81,
            default => 85,
        };

        $elasticityScore = match ($validated['concern']) {
            'aging' => 66,
            'dehydration' => 72,
            'dullness' => 74,
            default => 83,
        };

        $morningSku = 'AS-SRM-001';
        $nightSku = 'AS-CRM-108';

        $products = Product::with(['images', 'variants.inventory'])
            ->whereIn('sku', [$morningSku, $nightSku])
            ->where('published', true)
            ->get();

        return response()->json([
            'scores' => [
                'hydration' => $hydrationScore,
                'elasticity' => $elasticityScore,
                'radiance' => 88,
                'barrier_health' => 79,
            ],
            'ritual' => [
                'morning_step' => 'صبح: ۲ قطره سرم طلای ۲۴ عیار کلوئیدی جهت آبرسانی عمقی و درخشش کریستالی',
                'evening_step' => 'شب: کرم بازسازی‌کننده خاویار سیاه و پپتید طلایی برای لیفتینگ و ترمیم سلولی',
                'bundle_discount_percent' => 12,
            ],
            'recommended_products' => $products,
        ]);
    }

    /**
     * 2. Digital Fragrance Sommelier (Olfactory Pyramid Matcher)
     */
    public function matchFragrance(Request $request)
    {
        $validated = $request->validate([
            'mood' => ['required', 'in:imperial_night,parisian_day,velvet_romance'],
            'note_preference' => ['required', 'in:oud_saffron,damask_rose,white_bergamot'],
        ]);

        return response()->json([
            'matched_signature' => 'ASIYE Signature Eau De Parfum',
            'sku' => 'AS-PRF-999',
            'discovery_set_sku' => 'AS-DSC-777',
            'match_confidence_percent' => 97,
            'olfactory_pyramid' => [
                'top_notes' => 'ترنج کالابریا، زعفران سلطنتی خراسان، فلفل صورتی',
                'heart_notes' => 'رز دمشقی مخملی، یاسمن شب‌بو، زنبق فلورانس',
                'base_notes' => 'چوب صندل میسور، کهربای طلایی، مشک سفید و عود ملایم',
            ],
            'voucher_note' => 'پک تستر ۳ عددی Discovery Set شامل ۱۰۰٪ اعتبار بازگشت وجه برای خرید شیشه ۱۰۰ میل است.',
        ]);
    }

    /**
     * 3. Anti-Counterfeit TTAC / QR Hologram Serial Verification
     */
    public function verifyAuthenticity(string $code)
    {
        $clean = strtoupper(trim($code));

        $cert = DB::table('authenticity_certificates')
            ->join('products', 'products.id', '=', 'authenticity_certificates.product_id')
            ->select('authenticity_certificates.*', 'products.name_fa', 'products.name_en', 'products.sku', 'products.country')
            ->where('authenticity_certificates.serial_code', $clean)
            ->orWhere('authenticity_certificates.ttac_uid', $clean)
            ->first();

        if (!$cert) {
            return response()->json([
                'authentic' => false,
                'message' => 'هشدار اصالت: این کد در سامانه اصالت‌سنجی رسمی ASIYE STORE ثبت نشده است.',
            ], 404);
        }

        DB::table('authenticity_certificates')
            ->where('id', $cert->id)
            ->update([
                'verification_scans_count' => $cert->verification_scans_count + 1,
                'first_verified_at' => $cert->first_verified_at ?? now(),
            ]);

        return response()->json([
            'authentic' => true,
            'certificate' => [
                'serial_code' => $cert->serial_code,
                'ttac_uid' => $cert->ttac_uid,
                'product_name_fa' => $cert->name_fa,
                'product_name_en' => $cert->name_en,
                'sku' => $cert->sku,
                'batch_code' => $cert->batch_code,
                'origin_laboratory' => $cert->origin_laboratory,
                'country' => $cert->country,
                'manufactured_at' => $cert->manufactured_at,
                'expires_at' => $cert->expires_at,
                'cold_chain_verified' => (bool) $cert->cold_chain_verified,
                'scans_count' => $cert->verification_scans_count + 1,
            ],
            'message' => 'اصالت ۱۰۰٪ تایید شد — محصول اورجینال دارای زنجیره سرد استاندارد ASIYE.',
        ]);
    }

    /**
     * 4. Smart Replenishment & Product Depletion CRM Reminders
     */
    public function customerReplenishments(Request $request)
    {
        $customer = $request->user()?->customer;
        if (!$customer) {
            return response()->json(['items' => []]);
        }

        $orders = $customer->orders()
            ->with('items.variant.product')
            ->whereIn('status', ['paid', 'confirmed', 'preparing', 'shipped', 'delivered'])
            ->latest()
            ->limit(10)
            ->get();

        $reminders = [];
        foreach ($orders as $order) {
            $daysElapsed = max(1, (int) $order->created_at->diffInDays(now()));
            foreach ($order->items as $item) {
                $product = $item->variant?->product;
                if (!$product) continue;
                $cycle = max(15, (int) ($product->replenishment_cycle_days ?? 45) * $item->quantity);
                $remainingDays = max(0, $cycle - $daysElapsed);
                $usedPercent = min(100, (int) round(($daysElapsed / $cycle) * 100));

                $reminders[] = [
                    'product_id' => $product->id,
                    'name_fa' => $product->name_fa,
                    'sku' => $product->sku,
                    'used_percent' => $usedPercent,
                    'remaining_days' => $remainingDays,
                    'refill_coupon' => 'REFILL10',
                ];
            }
        }

        return response()->json(['items' => $reminders]);
    }

    /**
     * 5. ASIYE Gold Cashback Wallet Summary
     */
    public function walletSummary(Request $request)
    {
        $customer = $request->user()?->customer;
        if (!$customer) {
            return response()->json(['balance' => 0, 'transactions' => []]);
        }

        $txs = DB::table('wallet_transactions')
            ->where('customer_id', $customer->id)
            ->orderByDesc('id')
            ->limit(30)
            ->get();

        return response()->json([
            'balance' => (float) ($customer->wallet_balance ?? 0),
            'tier' => $customer->tier ?? 'gold',
            'cashback_rate_percent' => match ($customer->tier) {
                'signature_vip' => 7,
                'gold' => 5,
                default => 3,
            },
            'transactions' => $txs,
        ]);
    }
}
