<?php

namespace App\Http\Controllers;

use App\Models\Appointment;
use App\Status;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Inertia\Inertia;

class DashboardController extends Controller
{
    public function index(Request $request)
    {
        $doctorId = Auth::id();
        $now = now();

        $startOfCurrentWeek = $now->copy()->startOfWeek(Carbon::SUNDAY);
        $endOfCurrentWeek = $now->copy()->endOfWeek(Carbon::SATURDAY);

        $startOfLastWeek = $now->copy()->subWeek()->startOfWeek(Carbon::SUNDAY);
        $endOfLastWeek = $now->copy()->subWeek()->endOfWeek(Carbon::SATURDAY);


        $visitsLastWeek = Appointment::query()
            ->where('doctor_id', $doctorId)
            ->whereBetween('date', [$startOfLastWeek, $endOfLastWeek])
            ->where('status', status::Completed->value)
            ->count();

        $visitsThisWeek = Appointment::query()
            ->where('doctor_id', $doctorId)
            ->whereBetween('date', [$startOfCurrentWeek, $endOfCurrentWeek])
            ->count();
        $today = Carbon::today();
        $Tomorrow = Carbon::tomorrow();

        $upcomingAppointments = Appointment::query()
            ->where('doctor_id', $doctorId)
            ->whereBetween('date', [$today, $Tomorrow])
            ->whereIn('status', [Status::Confirmed->value, Status::Pending->value])
            ->with('patient')
            ->orderBy('date', "desc")
            ->limit(4)
            ->get();

        if ($request->wantsJson()) {
            return $upcomingAppointments->toJson();
        }

        return Inertia::render('dashboard', [
            'visitsLastWeek' => $visitsLastWeek,
            'visitsThisWeek' => $visitsThisWeek,
            'upcomingAppointments' => $upcomingAppointments,
        ]);
    }
}
