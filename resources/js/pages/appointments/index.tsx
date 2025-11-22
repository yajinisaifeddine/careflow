import { AppointmentCalendar } from '@/components/appointment-calendar';
import AppLayout from '@/layouts/app-layout';
import { appointments } from '@/routes';
import type { Appointment, BreadcrumbItem, Patient } from '@/types';
import { Head, usePage } from '@inertiajs/react';
import { toast } from 'sonner';

const breadcrumbs: BreadcrumbItem[] = [
    {
        title: 'Appointments',
        href: appointments().url,
    },
];

interface AppointmentProps {
    data: Appointment[];
    patients: Patient[];
}
export default function Index({ data,patients }: AppointmentProps) {
    const { errors } = usePage().props;
    if (errors) {
        toast(errors[0]);
    }
    return (
        <AppLayout breadcrumbs={breadcrumbs}>
            <Head title="Appointments" />
            <div className="m-1">
                <AppointmentCalendar data={data} patients={patients} />
            </div>
        </AppLayout>
    );
}
