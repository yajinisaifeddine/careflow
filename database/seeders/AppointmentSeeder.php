<?php

namespace Database\Seeders;

use App\Models\Appointment;
use App\Models\User;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class AppointmentSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {

        for ($i = 0; $i < 5; $i++) {

            $patient = User::all()->filter(function ($user) {
                return $user->hasRole('patient');
            })->random();

            Appointment::factory()->create([
                'patient_id' => $patient->id,
                'doctor_id'=> 1
            ]);
        }

    }
}
