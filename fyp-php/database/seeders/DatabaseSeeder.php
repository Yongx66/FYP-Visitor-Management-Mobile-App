<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\User;
use Hash;

class DatabaseSeeder extends Seeder
{
    /**
     * Seed the application's database.
     *
     * @return void
     */
    public function run()
    {
        $superadmin = User::create([
            'email' => config('services.admin.email'),
            'password' => Hash::make(config('services.admin.password')),
        ]);
        $superadmin->assignRole('SUPERADMIN');
    }
}
