<?php

namespace App\Models;

use Illuminate\Contracts\Auth\MustVerifyEmail;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;
use Spatie\Permission\Traits\HasRoles;
use Illuminate\Database\Eloquent\Relations\HasOne;
use Illuminate\Database\Eloquent\Relations\HasMany;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable, HasRoles;

    protected $guard_name = 'api';

    /**
     * The attributes that are mass assignable.
     *
     * @var array
     */
    protected $fillable = [
        'email', 'password', 'full_name', 'contact_no',
        'last_ip', 'last_login', 'last_password_change'
    ];

    /**
     * The attributes that should be hidden for arrays.
     *
     * @var array
     */
    protected $hidden = [
        'password', 
        'remember_token',
        'roles',
    ];

    /**
     * The attributes that should be cast to native types.
     *
     * @var array
     */
    protected $casts = [
        'last_login' => 'datetime',
        'last_password_change' => 'datetime'
    ];

    public function self_active_visiting_record($current_date): HasOne
    {
        return $this->hasOne(VisitorRecord::class, 'user_id', 'id')->where('date_of_visit', $current_date);
    }

    public function self_incoming_visiting_record($current_date): HasMany
    {
        return $this->hasMany(VisitorRecord::class, 'user_id', 'id')->where('date_of_visit', '>', $current_date);
    }

    public function self_past_visited_records($current_date) : HasMany
    {
        return $this->hasMany(VisitorRecord::class, 'user_id', 'id')->where('date_of_visit', '<', $current_date);
    }
}
