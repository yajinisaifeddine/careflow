import AppointmentStatus from '@/components/appointment-status';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { UserInfo } from '@/components/user-info';
import type { Appointment } from '@/types';
import { Calendar, Clock, User } from 'lucide-react';
import { DateTime } from 'luxon';

interface AppointmentProps {
    data?: Appointment[];
}

export function DoctorAppointmentsTable({ data = [] }: AppointmentProps) {
    return (
        <div className="w-full overflow-hidden rounded-lg border border-border bg-card">
            <Table>
                <TableHeader className="bg-muted/50">
                    <TableRow className="hover:bg-muted/50">
                        <TableHead className="font-semibold">#</TableHead>
                        <TableHead className="font-semibold">
                            <div className="flex items-center gap-2">
                                <User className="h-4 w-4" />
                                Patient
                            </div>
                        </TableHead>
                        <TableHead className="font-semibold">
                            <div className="flex items-center gap-2">
                                <Calendar className="h-4 w-4" />
                                Date
                            </div>
                        </TableHead>
                        <TableHead className="font-semibold">
                            <div className="flex items-center gap-2">
                                <Clock className="h-4 w-4" />
                                Time
                            </div>
                        </TableHead>
                        <TableHead className="font-semibold">
                            <div className="flex items-center gap-2">
                                <Clock className="h-4 w-4" />
                                Duration
                            </div>
                        </TableHead>
                        <TableHead className="font-semibold">
                            <div className="flex items-center gap-2">
                                <Clock className="h-4 w-4" />
                                Description
                            </div>
                        </TableHead>

                        <TableHead className="font-semibold">Status</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {data.map((app, idx) => {
                        return (
                            <TableRow key={app.id} className="transition-colors hover:bg-muted/30">
                                <TableCell className="font-mono text-sm text-muted-foreground">{idx + 1}</TableCell>
                                <TableCell className="font-medium">
                                    <Tooltip>
                                        <TooltipTrigger>{app.patient.name}</TooltipTrigger>
                                        <TooltipContent>
                                            <UserInfo user={app.patient} showEmail={false} />
                                        </TooltipContent>
                                    </Tooltip>
                                </TableCell>
                                <TableCell className="font-medium">{DateTime.fromISO(app.date).toFormat('dd LLL yyyy')}</TableCell>
                                <TableCell className="font-medium text-primary">
                                    {DateTime.fromFormat(app.time, 'HH:mm:ss').toFormat("h:mm a")}
                                </TableCell>

                                <TableCell>
                                    <span className="inline-flex items-center gap-1 rounded-full bg-blue-50 px-3 py-1 text-sm font-medium text-blue-700 dark:bg-blue-950 dark:text-blue-300">
                                        {app.duration}
                                        <span className="text-xs">hr</span>
                                    </span>
                                </TableCell>
                                <TableCell>
                                    {app.description}
                                </TableCell>
                                <TableCell>
                                    <AppointmentStatus rawStatus={app.status} appointmentId={app.id} />
                                </TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
        </div>
    );
}
