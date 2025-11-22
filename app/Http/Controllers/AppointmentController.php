<?php

namespace App\Http\Controllers;

use App\Models\Appointment;
use App\Models\User;
use App\Status;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class AppointmentController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $appointments = Auth::user()
            ->doctorAppointments()
            ->with('patient')
            ->orderBy('appointments.date', 'desc')
            ->get();

        $doctorId = Auth::id();
        $patients = Appointment::where('doctor_id', $doctorId)
            ->with('patient:id,name')
            ->get()
            ->pluck('patient')
            ->unique('id')
            ->values();
        return Inertia::render('appointments/index', [
            'data' => $appointments,
            'patients' => $patients
        ]);
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {

    }

    /**
     * Store a newly created resource in storage.
     */
    // Note: This method would be inside a Controller class.
    public function store(Request $request)
    {
        // Validate the incoming request data
        $request->validate([
            'date' => ['required', 'date'],
            'time' => ['required'],
            'patientId' => ['required', 'exists:users,id'],
            // 'duration' is validated as minutes (min: 30 minutes, max: 360 minutes/6 hours)
            'duration' => ['nullable', 'integer', 'min:30', 'max:360'],
            'description' => ['nullable', 'string'],
        ]);

        // Check for conflict using the authenticated doctor's user object
        /** @var User $doctor */
        $doctor = Auth::user();

        if ($this->hasConflict($doctor, $request->date, $request->time, $request->duration)) {
            return back()->withErrors(['time' => 'The time slot conflicts with an existing appointment. Please choose a different time or duration.']);
        }

        // Create the new appointment record
        $appointment = Appointment::create([
            'doctor_id' => $doctor->id,
            'patient_id' => $request->patientId,
            'date' => $request->date,
            'time' => $request->time,
            // The duration is stored in minutes as validated
            'duration' => $request->duration,
            'status' => 'pending',
        ]);

        return redirect('appointments')->with('success', 'Appointment created successfully.');
    }
    /**
     * Display the specified resource.
     */
    public function show(string $id)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(string $id)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, string $id)
    {
        $validated = $request->validate([
            'status' => ['required', 'string', 'in:pending,confirmed,completed,cancelled']
        ]);


        $appointment = Appointment::findOrFail($id);

        $oldStatus = $appointment->status;
        $newStatus = $validated['status'];

        // Prevent changing from cancelled or completed
        if (in_array($oldStatus, ['cancelled', 'completed'])) {
            return back()->withErrors([
                "Cannot change status because it is already {$oldStatus}."
            ]);
        }

        $validTransitions = [
            'pending' => ['confirmed', 'cancelled'],
            'confirmed' => ['completed', 'cancelled'],
            'completed' => [],
            'cancelled' => [],
        ];

        if (!in_array($newStatus, $validTransitions[$oldStatus])) {
            return back()->withErrors([
                "Invalid transition from '$oldStatus' to '$newStatus'."
            ]);
        }

        // Update status
        $appointment->status = $newStatus;
        $appointment->save();

        return back()->with('success', 'Status updated successfully.');

    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(string $id)
    {
        //
    }

    public function hasConflict(User $doctor, string $date, ?string $time = null, ?int $duration = null): bool
    {
        // 1. Calculate the New Appointment's Start and End Times
        $newStart = Carbon::parse("$date $time");
        // CRITICAL FIX: Use addMinutes() instead of addHours()
        $newEnd = $newStart->copy()->addMinutes($duration);

        // 2. Fetch potential overlapping appointments (Same Doctor, Same Day)
        // Optimization: We filter by the end time of the new slot to reduce the dataset.
        $appointments = Appointment::where('doctor_id', $doctor->id)
            ->where('date', $date)
            ->where('status', '!=', Status::Cancelled->value)
            // SQL Optimization: Fetch only appointments that *start* before the new one *ends*.
            ->where('time', '<', $newEnd->format('H:i:s'))
            ->get();

        // 3. Strict Temporal Overlap Check
        foreach ($appointments as $existing) {
            $existingStart = Carbon::parse("{$existing->date} {$existing->time}");
            // CRITICAL FIX: Use addMinutes() for the existing appointment's duration
            $existingEnd = $existingStart->copy()->addMinutes($existing->duration);

            // Mathematical Overlap Formula: (StartA < EndB) AND (EndA > StartB)
            // This correctly allows adjacent appointments (e.g., A ends at 10:00, B starts at 10:00).
            if ($newStart->lt($existingEnd) && $newEnd->gt($existingStart)) {
                return true;
            }
        }

        return false;
    }

}
