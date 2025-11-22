<?php

namespace Database\Factories;

use App\Models\Appointment;
use App\Models\User;
use App\Status;
use Carbon\Carbon;
use Illuminate\Database\Eloquent\Factories\Factory;

class AppointmentFactory extends Factory
{
    protected $model = Appointment::class;

    public function definition(): array
    {
        return [
            'patient_id' => User::factory(),
            'doctor_id' => User::factory(),
            'date' => $this->faker->dateTimeBetween('now', '+1 week'),
            'time' => $this->faker->dateTimeBetween('00:00:00', '23:59:59'),
            'duration' => $this->faker->randomElement([30, 60, 90, 120, 150, 180, 210, 240]),
            'status' => $this->faker->randomElement(Status::cases()),
        ];
    }
}
