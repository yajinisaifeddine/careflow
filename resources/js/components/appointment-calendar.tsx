import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Calendar, ChevronLeft, ChevronRight } from 'lucide-react';
import { ReactElement, useState } from 'react';

import { Appointment, Patient } from '@/types';

import { CreateAppointmentForm } from '@/components/create-appointment-form';
import { DateTime } from 'luxon';

interface AppointmentCalendarProps {
    data: Appointment[];
    patients: Patient[];
}

export function AppointmentCalendar({ data, patients }: AppointmentCalendarProps) {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [selectedDate, setSelectedDate] = useState<string>('');
    const [appointments, setAppointments] = useState<Appointment[]>(data || []);
    const [isOpen, setIsOpen] = useState(false);

    const daysInMonth = (date: Date) => {
        const year = date.getFullYear();
        const month = date.getMonth();
        return new Date(year, month + 1, 0).getDate();
    };

    const firstDayOfMonth = (date: Date) => {
        return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
    };

    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

    const prevMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1));
    };

    const nextMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1));
    };
    const parseMinutes = (mins) => {
        const h = Math.floor(mins / 60);
        const m = mins % 60;
        return [
            h && `${h} hour${h > 1 ? 's' : ''}`,
            m && `${m} minute${m > 1 ? 's' : ''}`
        ].filter(Boolean).join(' and ') || '0 minutes';
    };
    const getAppointmentsForDay = (day: number) => {
        return appointments.filter((apt) => {
            const aptDate = new Date(apt.date);
            return aptDate.getDate() === day && aptDate.getMonth() === currentDate.getMonth() && aptDate.getFullYear() === currentDate.getFullYear();
        });
    };

    const handleDayClick = (day: number) => {
        setIsOpen(true);
        setSelectedDate(getDateFromDay(day));
    };
    const getDateFromDay = (day: number) => {
        const date = new Date();
        date.setDate(day);

        return date.toISOString().split('T')[0];
    };

    const renderCalendarDays = () => {
        const days: ReactElement[] = [];
        const totalDays = daysInMonth(currentDate);
        const firstDay = firstDayOfMonth(currentDate);
        const today = DateTime.now().startOf('day');

        const isBeforeToday = (day: number) => {
            const dateToCheck = DateTime.fromObject({
                year: currentDate.getFullYear(),
                month: currentDate.getMonth() + 1, // Luxon months are 1-indexed
                day: day,
            }).startOf('day');

            return dateToCheck < today;
        };

        for (let i = 0; i < firstDay; i++) {
            days.push(<div key={`empty-${i}`} className="bg-muted/30 p-3"></div>);
        }

        for (let day = 1; day <= totalDays; day++) {
            const dayAppointments = getAppointmentsForDay(day);
            const isToday = today.year === currentDate.getFullYear() && today.month === currentDate.getMonth() + 1 && today.day === day;
            const isDisabled = isBeforeToday(day);

            days.push(
                <div
                    key={day}
                    onClick={() => !isDisabled && handleDayClick(day)}
                    className={`min-h-28 border border-border p-3 transition-all ${
                        isToday ? 'border-2 border-accent bg-accent/10' : 'bg-background'
                    } ${isDisabled ? 'cursor-default bg-muted opacity-50' : 'cursor-pointer hover:bg-accent/5 hover:shadow-md'} `}
                >
                    <div className={`mb-2 text-sm font-semibold ${isToday ? 'text-cyan-600' : 'text-foreground'}`}>{day}</div>
                    <div className="space-y-1">
                        {dayAppointments
                            .sort((a, b) => Number(a.time) - Number(b.time))
                            .map((apt) => (
                                <div
                                    key={apt.id}
                                    className="group truncate rounded-md bg-accent px-2 py-1 text-xs text-accent-foreground transition-all hover:shadow-md"
                                    onClick={(e) => e.stopPropagation()}
                                >
                                    <div className="flex items-center justify-between gap-1">
                                        <span className="font-medium">{DateTime.fromFormat(apt.time, 'HH:mm:ss').toFormat('hh:mm a')}</span>
                                    </div>
                                    <div className="flex flex-col truncate">
                                        <div>{apt?.patient?.name}</div>
                                        <div>{parseMinutes(apt.duration)}</div>
                                    </div>
                                </div>
                            ))}
                    </div>
                </div>,
            );
        }

        return days;
    };
    return (
        <div className="min-h-screen bg-background p-6 md:p-8">
            <div className="mx-auto max-w-7xl">
                {/* Header */}
                <div className="mb-8 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                    <div className="flex items-center gap-3">
                        <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-accent">
                            <Calendar className="h-6 w-6 text-accent-foreground" />
                        </div>
                        <div>
                            <h1 className="text-3xl font-semibold text-foreground">Appointments</h1>
                            <p className="text-sm text-muted-foreground">Manage your patient schedule</p>
                        </div>
                    </div>

                    <CreateAppointmentForm
                        patients={patients}
                        date={selectedDate}
                        isOpen={isOpen}
                        setIsOpen={setIsOpen}
                        setAddedAppointment={(appointment) => setAppointments([...appointments, appointment])}
                    />
                </div>

                {/* Calendar */}
                <Card className="border-border shadow-sm">
                    <CardContent className="p-6">
                        {/* Month Navigation */}
                        <div className="mb-6 flex items-center justify-between">
                            <Button onClick={prevMonth} variant="outline" size="sm" className="h-9 w-9 p-0">
                                <ChevronLeft className="h-4 w-4" />
                            </Button>
                            <h2 className="text-2xl font-semibold text-foreground">
                                {monthNames[currentDate.getMonth()]} {currentDate.getFullYear()}
                            </h2>
                            <Button onClick={nextMonth} variant="outline" size="sm" className="h-9 w-9 p-0">
                                <ChevronRight className="h-4 w-4" />
                            </Button>
                        </div>
                        {/* Days of the Week */}
                        <div className="mb-2 grid grid-cols-7 gap-px">
                            {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day) => (
                                <div key={day} className="p-3 text-center text-sm font-medium text-muted-foreground">
                                    {day}
                                </div>
                            ))}
                        </div>
                        {/* Calendar Grid */}
                        <div className="grid grid-cols-7 gap-px">{renderCalendarDays()}</div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
