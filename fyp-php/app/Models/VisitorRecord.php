<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class VisitorRecord extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id', 'date_of_visit', 'reason_of_visiting', 'token',
    ];

    protected $casts = [
        'date_of_visit' => 'date:Y-m-d',
    ];

    public function user() : BelongsTo
    {
        return $this->belongsTo(User::class, 'user_id');
    }
}
