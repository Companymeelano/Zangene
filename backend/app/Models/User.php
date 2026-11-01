<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\SoftDeletes;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable, SoftDeletes;

    protected $fillable = [
        'name',
        'mobile',
        'email',
        'password',
        'role_id',
        'is_active',
        'mobile_verified_at',
        'skin_type',
        'birthday',
        'sms_optin',
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected $casts = [
        'is_active' => 'boolean',
        'mobile_verified_at' => 'datetime',
        'password' => 'hashed',
        'sms_optin' => 'boolean',
    ];

    public function role()
    {
        return $this->belongsTo(Role::class);
    }

    public function customer()
    {
        return $this->hasOne(Customer::class);
    }

    public function hasRole(string|array $slugs): bool
    {
        if (!$this->role) {
            return false;
        }
        $slugs = is_array($slugs) ? $slugs : explode('|', $slugs);
        return in_array($this->role->slug, $slugs, true);
    }

    public function hasPermission(string $permissionSlug): bool
    {
        if (!$this->role) {
            return false;
        }
        if ($this->role->slug === 'super-admin') {
            return true;
        }
        return $this->role->permissions()->where('slug', $permissionSlug)->exists();
    }
}
