<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureRoleOrPermission
{
    public function handle(Request $request, Closure $next, string $requirement): Response
    {
        $user = $request->user();

        if (!$user || !$user->is_active) {
            return response()->json(['error' => 'دسترسی غیرمجاز. لطفا ابتدا وارد شوید.'], 401);
        }

        if ($user->hasRole($requirement) || $user->hasPermission($requirement)) {
            return $next($request);
        }

        return response()->json(['error' => 'شما مجوز دسترسی به این بخش مدیریتی را ندارید.'], 403);
    }
}
