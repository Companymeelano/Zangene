<?php

namespace App\Http\Controllers\Customer;

use App\Http\Controllers\Controller;
use App\Models\GiftCard;
use App\Services\Sms\SmsService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

/** v6.9 · buying, checking and spending a gift card. */
class GiftCardController extends Controller
{
    /** POST /v1/gift-cards — a customer buys a card for someone. */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'amount' => ['required', 'numeric', 'min:100000', 'max:50000000'],
            'recipient_name' => ['required', 'string', 'min:2', 'max:80'],
            'recipient_mobile' => ['required', 'string', 'regex:/^09[0-9]{9}$/'],
            'message' => ['nullable', 'string', 'max:240'],
        ]);

        if ($validator->fails()) {
            return response()->json(['errors' => $validator->errors()], 422);
        }

        $user = $request->user();

        $card = GiftCard::create([
            'code' => GiftCard::generateCode(),
            'amount' => $request->input('amount'),
            'remaining' => $request->input('amount'),
            'buyer_name' => $user?->name,
            'buyer_mobile' => $user?->mobile,
            'recipient_name' => $request->input('recipient_name'),
            'recipient_mobile' => $request->input('recipient_mobile'),
            'message' => $request->input('message'),
            'status' => 'active',
        ]);

        SmsService::send('gift_card_gift', $card->recipient_mobile, [
            'name' => $card->recipient_name,
            'coupon' => $card->code,
            'total' => number_format((float) $card->amount) . ' تومان',
        ], true);

        return response()->json([
            'message' => 'کارت هدیه ساخته شد و پیامک برای گیرنده ارسال شد.',
            'card' => $card,
        ], 201);
    }

    /** GET /v1/gift-cards/{code} — check a balance before spending it. */
    public function show(string $code)
    {
        $card = GiftCard::where('code', $code)->first();

        if (!$card) {
            return response()->json(['error' => 'کارتی با این کد پیدا نشد.'], 404);
        }

        return response()->json([
            'code' => $card->code,
            'amount' => (float) $card->amount,
            'remaining' => (float) $card->remaining,
            'status' => $card->status,
            'usable' => $card->isUsable(),
        ]);
    }
}
