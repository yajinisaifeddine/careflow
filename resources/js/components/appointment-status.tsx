import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { router } from '@inertiajs/react';
import { AlertCircle, CheckCircle, Clock3 } from 'lucide-react';

type StatusKey = 'confirmed' | 'pending' | 'completed' | 'cancelled';

const statusConfig = {
    confirmed: { label: 'Confirmed', variant: 'default' as const, icon: CheckCircle },
    pending: { label: 'Pending', variant: 'secondary' as const, icon: Clock3 },
    completed: { label: 'Completed', variant: 'outline' as const, icon: CheckCircle },
    cancelled: { label: 'Cancelled', variant: 'destructive' as const, icon: AlertCircle },
};

interface AppointmentStatusProps {
    rawStatus: StatusKey;
    appointmentId: number;
}

export default function AppointmentStatus({ rawStatus, appointmentId }: AppointmentStatusProps) {
    const status = statusConfig[rawStatus];

    const handleChangeStatus = (newStatus: StatusKey) => {
        router.put(`/appointments/${appointmentId}`, {
            status: newStatus,
        });
    };

    const StatusIcon = status.icon;

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-auto p-0">
                    <Badge variant={status.variant} className="flex w-fit cursor-pointer items-center gap-1">
                        <StatusIcon className="h-3 w-3" />
                        {status.label}
                    </Badge>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
                <DropdownMenuItem onClick={() => handleChangeStatus('confirmed')}>
                    <Badge variant="default" className="flex w-fit items-center gap-1">
                        <CheckCircle className="h-3 w-3" />
                        Confirmed
                    </Badge>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => handleChangeStatus('completed')}>
                    <Badge variant="outline" className="flex w-fit items-center gap-1">
                        <CheckCircle className="h-3 w-3" />
                        Completed
                    </Badge>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => handleChangeStatus('cancelled')}>
                    <Badge variant="destructive" className="flex w-fit items-center gap-1">
                        <AlertCircle className="h-3 w-3" />
                        Cancelled
                    </Badge>
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    );
}
