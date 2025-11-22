<?php

namespace App\Policies;

use App\Models\Appointment;
use App\Models\User;
use App\Status;
use Carbon\Carbon;
use http\Exception\InvalidArgumentException;
use http\Exception\RuntimeException;

class AppointmentService
{
    /**
     * Determine whether the user can view any models.
     */
    public function viewAny(User $user): bool
    {
        return false;
    }

    /**
     * Determine whether the user can view the model.
     */
    public function view(User $user, Appointment $appointment): bool
    {
        if (!$user->hasPermissionTo('view appointment')) {
            return false;
        }

    }

    /**
     * Determine whether the user can create models.
     */
    public function create(User $doctor, array $data): bool
    {
        if ($this->hasConflict($doctor, $data['date'], $data['time'], $data['duration'])) {
            throw new RuntimeException("The selected time slot conflicts with an existing appointment.");
        }

        // 2. Create the appointment
        return Appointment::create([
            'doctor_id' => $doctor->id,
            'patient_id' => $data['patient_id'],
            'date' => $data['date'],
            'time' => $data['time'],
            'duration' => $data['duration'],
            'status' => 'pending',
        ]);

    }

    /**
     * Determine whether the user can update the model.
     */
    public function update(User $user, Appointment $appointment): bool
    {
        return $user->hasPermissionTo('update appointment') || $user->hasPermissionTo('reschedule appointment');
    }

    /**
     * Determine whether the user can delete the model.
     */
    public function delete(User $user, Appointment $appointment): bool
    {
        return $user->hasPermissionTo('delete appointment');
    }

    /**
     * Determine whether the user can restore the model.
     */
    public function restore(User $user, Appointment $appointment): bool
    {
        return false;
    }

    /**
     * Determine whether the user can permanently delete the model.
     */
    public function forceDelete(User $user, Appointment $appointment): bool
    {
        return false;
    }

    public function hasConflict(User $doctor, Appointment|string $data, ?string $time = null, ?int $duration = null): bool
    {
        return match (true) {
            $data instanceof Appointment => $this->checkConflictByInstance($doctor, $data),
            is_string($data) => $this->checkConflictByValues($doctor, $data, $time, $duration),
            default => throw new InvalidArgumentException("Invalid arguments provided for conflict check."),
        };
    }

    protected function checkConflictByInstance(User $doctor, Appointment $appointment): bool
    {
        return $this->checkConflictByValues(
            $doctor,
            $appointment->date,
            $appointment->time,
            $appointment->duration,
            $appointment->id // Pass ID to exclude itself (useful for updates)
        );
    }

    protected function checkConflictByValues(User $doctor, string $date, string $time, int $duration, ?int $excludeId = null): bool
    {
        // 1. Calculate requested range
        $newStart = Carbon::parse("$date $time");
        $newEnd = $newStart->copy()->addHours($duration);

        // 2. Fetch potential overlapping appointments (Same Doctor, Same Day)
        // We filter broadly by SQL, then strictly by PHP to handle the duration logic accurately.
        $appointments = Appointment::where('doctor_id', $doctor->id)
            ->where('date', $date)
            ->where('status', '!=', Status::Cancelled->value)
            ->when($excludeId, fn($q) => $q->where('id', '!=', $excludeId))
            ->get();

        // 3. Strict Overlap Check
        foreach ($appointments as $existing) {
            $existingStart = Carbon::parse("{$existing->date} {$existing->time}");
            $existingEnd = $existingStart->copy()->addHours($existing->duration);

            // Mathematical Overlap Formula: (StartA < EndB) AND (EndA > StartB)
            if ($newStart->lt($existingEnd) && $newEnd->gt($existingStart)) {
                return true;
            }
        }

        return false;
    }
}
