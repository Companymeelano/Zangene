<?php

namespace App\Jobs;

use App\Models\SmsMessage;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Http;
use Throwable;

/**
 * v6.8 · delivers one message through the configured Iranian gateway.
 * Retries are capped; every attempt is written back to the message row.
 */
class SendSmsJob implements ShouldQueue
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public int $tries = 4;
    public int $backoff = 120;

    public function __construct(public int $messageId)
    {
    }

    public function handle(): void
    {
        $message = SmsMessage::find($this->messageId);
        if (!$message || $message->status === 'sent') {
            return;
        }

        $error = $this->deliver($message->mobile, $message->body);

        if ($error === null) {
            $message->forceFill([
                'status' => 'sent',
                'sent_at' => now(),
                'attempts' => $message->attempts + 1,
                'error' => null,
            ])->save();

            return;
        }

        $message->forceFill([
            'status' => $message->attempts + 1 >= $this->tries ? 'failed' : 'pending',
            'attempts' => $message->attempts + 1,
            'error' => $error,
        ])->save();

        if ($message->status === 'pending') {
            $this->release($this->backoff);
        }
    }

    private function deliver(string $mobile, string $body): ?string
    {
        $driver = config('services.sms.driver', 'sandbox');

        try {
            if ($driver === 'kavenegar') {
                $response = Http::timeout(10)->get(
                    'https://api.kavenegar.com/v1/' . config('services.sms.kavenegar.key') . '/sms/send.json',
                    [
                        'receptor' => $mobile,
                        'sender' => config('services.sms.sender'),
                        'message' => $body,
                    ]
                );

                return $response->successful() ? null : 'kavenegar:' . $response->status();
            }

            if ($driver === 'mellipayamak') {
                $response = Http::timeout(10)->post('https://rest.payamak-panel.com/api/SendSMS/SendSMS', [
                    'username' => config('services.sms.mellipayamak.username'),
                    'password' => config('services.sms.mellipayamak.password'),
                    'to' => $mobile,
                    'from' => config('services.sms.sender'),
                    'text' => $body,
                    'isflash' => false,
                ]);

                return $response->successful() ? null : 'mellipayamak:' . $response->status();
            }

            if ($driver === 'smsir') {
                $response = Http::timeout(10)
                    ->withHeaders(['x-api-key' => config('services.sms.smsir.key')])
                    ->post('https://api.sms.ir/v1/send/bulk', [
                        'lineNumber' => config('services.sms.sender'),
                        'messageText' => $body,
                        'mobiles' => [$mobile],
                    ]);

                return $response->successful() ? null : 'smsir:' . $response->status();
            }

            if ($driver === 'ghasedak') {
                $response = Http::timeout(10)
                    ->withHeaders(['apikey' => config('services.sms.ghasedak.key')])
                    ->asForm()
                    ->post('https://api.ghasedak.me/v2/sms/send/simple', [
                        'receptor' => $mobile,
                        'message' => $body,
                        'linenumber' => config('services.sms.sender'),
                    ]);

                return $response->successful() ? null : 'ghasedak:' . $response->status();
            }

            // sandbox: nothing leaves the server, but the record stays for QA
            return null;
        } catch (Throwable $e) {
            return 'network:' . $e->getMessage();
        }
    }
}
