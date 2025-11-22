<?php

namespace Database\Seeders;

use App\Models\User;
use Illuminate\Database\Seeder;

class UserSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        for($i=0;$i<50;$i++){
            $user = User::factory()->create();
            $user->assignRole(['patient', 'doctor'][random_int(0,1)]);
        }
    }
}
