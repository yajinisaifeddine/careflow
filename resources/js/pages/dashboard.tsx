import { StatsCards } from '@/components/doctor-stats-card';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import AppLayout from '@/layouts/app-layout';
import { dashboard } from '@/routes';
import { Appointment, type BreadcrumbItem } from '@/types';
import { Head } from '@inertiajs/react';
import { Clock, User, X } from 'lucide-react';

const breadcrumbs: BreadcrumbItem[] = [
    {
        title: 'Dashboard',
        href: dashboard().url,
    },
];

type DashboardProps = {
    visitsLastWeek: number;
    visitsThisWeek: number;
    upcomingAppointments: Appointment[];
};
export default function Dashboard({ visitsLastWeek, visitsThisWeek, upcomingAppointments }: DashboardProps) {
    const formatDuration = (totalMinutes: number): string => {
        const hours = Math.floor(totalMinutes / 60);
        const minutes = totalMinutes % 60;
        const parts: string[] = [];

        if (hours > 0) {
            parts.push(hours + (hours === 1 ? ' hr' : ' hrs'));
        }

        if (minutes > 0) {
            parts.push(minutes + (minutes === 1 ? ' min' : ' mins'));
        }

        return parts.join(' ');
    };
    return (
        <AppLayout breadcrumbs={breadcrumbs}>
            <Head title="Dashboard" />

            <div className="m-4 mb-8">
                <h1 className="text-3xl font-bold text-foreground">Analytics Dashboard</h1>
                <p className="mt-2 text-muted-foreground">Track your weekly visit metrics</p>
            </div>
            <StatsCards visitsLastWeek={visitsLastWeek} visitsThisWeek={visitsThisWeek} />
            <div className="relative mx-4 min-h-[100vh] flex-1 rounded-xl border border-sidebar-border/70 md:min-h-min dark:border-sidebar-border">
                <div className="absolute inset-0 size-full stroke-neutral-900/20 dark:stroke-neutral-100/20">
                    <Card className="mt-6 border-none shadow-sm">
                        <CardHeader className="pb-4">
                            <div className="flex items-center gap-2">
                                <Clock className="h-5 w-5 text-accent" />
                                <div>
                                    <CardTitle className="text-lg">Upcoming Appointments</CardTitle>
                                    <CardDescription>Next scheduled visits</CardDescription>
                                </div>
                            </div>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-2">
                                {upcomingAppointments.length === 0 ? (
                                    <p className="py-8 text-center text-sm text-muted-foreground">No appointments scheduled</p>
                                ) : (
                                    upcomingAppointments.map((apt) => (
                                        <div
                                            key={apt.id}
                                            className="group flex items-center justify-between rounded-lg border border-border bg-muted/50 p-4 transition-colors hover:bg-muted"
                                        >
                                            <div className="flex flex-1 items-center gap-3">
                                                <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10">
                                                    <User className="h-5 w-5 text-accent" />
                                                </div>
                                                <div className="min-w-0 flex-1">
                                                    <div className="text-sm font-medium text-foreground">{apt.patient?.name}</div>
                                                    <div className="text-xs text-muted-foreground">
                                                        {apt.date} at {apt.time.slice(0, 5)}
                                                        <br />
                                                        {formatDuration(Number(apt.duration))} • {apt.description}
                                                    </div>
                                                </div>
                                            </div>
                                            <Button
                                                onClick={() => console.log(apt.id)}
                                                variant="ghost"
                                                size="sm"
                                                className="h-8 w-8 p-0 text-destructive opacity-0 transition-opacity group-hover:opacity-100 hover:bg-destructive/10 hover:text-destructive"
                                            >
                                                <X className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    ))
                                )}
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </AppLayout>
    );
}
