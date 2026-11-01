<?php

namespace App\Console\Commands;

use App\Models\Order;
use App\Models\SmsMessage;
use App\Models\User;
use App\Services\Sms\SmsService;
use Illuminate\Console\Command;

/**
 * v6.8 · the server-side autopilot, mirroring the app's daily routine.
 *
 *   php artisan asiye:automations
 *
 * Scheduled once a day. Every block is guarded so one failing query never
 * stops the rest of the morning's work.
 */
class SmsAutomationCommand extends Command
{
    protected $signature = 'asiye:automations';
    protected $description = 'یادآور سفارش مجدد، تبریک تولد، درخواست نظر و گزارش روزانه مدیر';

    public function handle(): int
    {
        $this->reorderReminders();
        $this->birthdays();
        $this->reviewRequests();
        $this->ownerDigest();

        return self::SUCCESS;
    }

    /** Products past their replenish cycle — one gentle nudge per customer. */
    private function reorderReminders(): void
    {
        try {
            Order::with(['items.variant.product', 'customer.user'])
                ->where('created_at', '<=', now()->subDays(30))
                ->latest('id')
                ->limit(30)
                ->get()
                ->each(function (Order $order) {
                    $user = $order->customer?->user;
                    if (!$user || !$user->sms_optin) {
                        return;
                    }

                    $item = $order->items->first();
                    $product = $item?->variant?->product;
                    $cycle = (int) ($product->replenish_days ?? 45);
                    $days = (int) $order->created_at->diffInDays(now());

                    if ($cycle > 0 && $days < $cycle) {
                        return;
                    }
                    if (SmsMessage::where('mobile', $user->mobile)
                        ->where('template_key', 'reorder_reminder')
                        ->where('created_at', '>=', now()->subDays(7))
                        ->exists()) {
                        return;
                    }

                    SmsService::send('reorder_reminder', $user->mobile, [
                        'name' => $user->name ?: 'مشتری گرامی',
                        'product' => $product->name_fa ?? 'محصول پیشین شما',
                        'days' => $days,
                    ]);
                });
        } catch (\Throwable $e) {
            report($e);
        }
    }

    private function birthdays(): void
    {
        try {
            $today = now()->format('m-d');

            User::whereNotNull('birthday')
                ->where('sms_optin', true)
                ->where('birthday', 'like', "%{$today}")
                ->limit(50)
                ->get()
                ->each(fn (User $user) => SmsService::send('birthday', $user->mobile, [
                    'name' => $user->name ?: 'مشتری گرامی',
                    'coupon' => 'BIRTHDAY20',
                    'percent' => '۲۰',
                ]));
        } catch (\Throwable $e) {
            report($e);
        }
    }

    private function reviewRequests(): void
    {
        try {
            Order::with('customer.user')
                ->where('status', 'delivered')
                ->where('updated_at', '<=', now()->subDay())
                ->latest('id')
                ->limit(25)
                ->get()
                ->each(function (Order $order) {
                    $user = $order->customer?->user;
                    if (!$user || !$user->sms_optin) {
                        return;
                    }
                    if (SmsMessage::where('mobile', $user->mobile)
                        ->where('template_key', 'order_delivered')
                        ->where('created_at', '>=', now()->subDays(14))
                        ->exists()) {
                        return;
                    }

                    SmsService::send('order_delivered', $user->mobile, [
                        'name' => $user->name ?: 'مشتری گرامی',
                        'order' => $order->order_number,
                    ]);
                });
        } catch (\Throwable $e) {
            report($e);
        }
    }

    private function ownerDigest(): void
    {
        try {
            SmsService::send('owner_daily', config('services.sms.owner_mobile'), [
                'count' => Order::whereDate('created_at', today())->count(),
            ]);
        } catch (\Throwable $e) {
            report($e);
        }
    }
}
