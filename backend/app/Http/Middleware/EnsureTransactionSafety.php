<?php

namespace App\Http\Middleware;

use App\Models\AdminActivityLog;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureTransactionSafety
{
    private const SENSITIVE_KEYS = [
        'password',
        'password_confirmation',
        'otp',
        'token',
        'access_token',
        'card_number',
        'cvv2',
        'signature',
    ];

    public function handle(Request $request, Closure $next): Response
    {
        $path = $request->path();
        $user = $request->user();

        if (
            str_starts_with($path, 'api/v1/admin') ||
            str_starts_with($path, 'api/v1/checkout') ||
            str_starts_with($path, 'api/v1/installment')
        ) {
            $actorType = 'guest';
            if ($user) {
                $actorType = $user->role_id ? 'admin' : 'customer';
            }

            try {
                AdminActivityLog::create([
                    'admin_id' => $user?->id,
                    'actor_type' => $actorType,
                    'action' => $request->method() . ' ' . $path,
                    'details' => json_encode(
                        $request->except(self::SENSITIVE_KEYS),
                        JSON_UNESCAPED_UNICODE
                    ),
                    'ip_address' => $request->ip(),
                ]);
            } catch (\Throwable $e) {
                // Never allow audit telemetry failure to crash customer checkout,
                // while still logging to Laravel system error channel.
                report($e);
            }
        }

        /** @var Response $response */
        $response = $next($request);
        $response->headers->set('X-Content-Type-Options', 'nosniff');
        $response->headers->set('X-Frame-Options', 'DENY');
        $response->headers->set('Referrer-Policy', 'strict-origin-when-cross-origin');

        return $response;
    }
}
