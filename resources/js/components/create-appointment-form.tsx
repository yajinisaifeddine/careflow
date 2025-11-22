import { PatientSelect } from '@/components/patient-select';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Appointment, AppointmentData, Patient } from '@/types';
import { useForm } from '@inertiajs/react';
import { Plus } from 'lucide-react';
import { useEffect } from 'react';
import { toast } from 'sonner';

interface CreateAppointmentFormProps {
    patients: Patient[];
    date: string;
    isOpen: boolean;
    setIsOpen: (boolean) => void;
    setAddedAppointment: (appointment: Appointment) => void;
}

export function CreateAppointmentForm({ patients, date, isOpen, setIsOpen, setAddedAppointment }: CreateAppointmentFormProps) {
    const { data, setData, post, processing, errors, recentlySuccessful } = useForm<AppointmentData>({
        patientId: '',
        date: '',
        time: '',
        description: '',
        duration: 0,
    });
    useEffect(() => {
        if (date) setData('date', date);
    }, [date, setData]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        post('/appointments', {
            preserveScroll: true,
            onSuccess: () => {
                setAddedAppointment({
                    id: 0,
                    patient: patients.filter(patient => patient.id == data.patientId).at(0) ,
                    date: data.date,
                    time: `${data.time}:00`,
                    doctor: {
                        id: '0',
                        name: '',
                    },
                    status: 'pending',
                    description: data.description,
                    duration: data.duration.toString(),
                });
                toast('appointment created successfully')
            },
            onError : ()=> {
                toast(`error happens`)
            }
        });
    };

    const handleSelectedPatient = (patient: Patient) => {
        setData('patientId', patient.id);
    };

    return (
        <>
            <Dialog open={isOpen} onOpenChange={setIsOpen}>
                <DialogTrigger asChild>
                    <Button className="w-full bg-accent text-accent-foreground hover:bg-accent/90 md:w-auto">
                        <Plus className="mr-2 h-5 w-5" />
                        New Appointment
                    </Button>
                </DialogTrigger>
                {/* 4. Use a <form> and the structure from CreateAppointmentForm */}
                <DialogContent className="sm:max-w-md">
                    <DialogHeader>
                        <DialogTitle>Schedule Appointment</DialogTitle>
                        <DialogDescription>Add a new patient appointment to the calendar</DialogDescription>
                    </DialogHeader>
                    {/* Form wrapping the content */}
                    <form onSubmit={handleSubmit} className="space-y-4">
                        {/* Patient Select */}
                        <div>
                            <Label htmlFor="patientId">Patient</Label>
                            <PatientSelect patients={patients} setSelected={handleSelectedPatient} placeholder="Select a patient..." />
                            {errors.patientId && <p className="text-sm font-medium text-destructive">{errors.patientId}</p>}
                        </div>

                        {/* Date and Time */}
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <Label htmlFor="date">Date</Label>
                                <Input
                                    id="date"
                                    type="date"
                                    value={data.date}
                                    onChange={(e) => {
                                        console.log(e.target.value);
                                        setData('date', e.target.value);
                                    }}
                                    min={new Date().toISOString().split('T')[0]}
                                    className="mt-1"
                                    aria-invalid={!!errors.date}
                                />
                                {errors.date && <p className="text-sm font-medium text-destructive">{errors.date}</p>}
                            </div>
                            <div>
                                <Label htmlFor="time">Time</Label>
                                <Input
                                    id="time"
                                    type="time"
                                    value={data.time}
                                    onChange={(e) => setData('time', e.target.value)}
                                    className="mt-1"
                                    aria-invalid={!!errors.time}
                                />
                                {errors.time && <p className="text-sm font-medium text-destructive">{errors.time}</p>}
                            </div>
                        </div>

                        {/* Duration and Description (matching the second component) */}
                        <Label>Duration</Label>
                        <div className="flex flex-row gap-4 align-bottom">
                            {/* Duration - Hours */}
                            <Label htmlFor="duration-hours" className="flex flex-row-reverse text-sm">
                                <span className="m-2 text-lg">hs</span>
                                <Input
                                    id="duration-hours"
                                    type="number"
                                    value={Math.floor(data.duration / 60) || ''}
                                    onChange={(e) => {
                                        const hours = Number(e.target.value) || 0;
                                        const minutes = data.duration % 60;
                                        setData('duration', hours * 60 + minutes);
                                    }}
                                    className="mt-1 w-16"
                                    aria-invalid={!!errors.duration}
                                />
                            </Label>

                            {/* Duration - Minutes */}
                            <Label htmlFor="duration-minutes" className="flex flex-row-reverse text-sm">
                                <span className="m-2 text-lg">mn</span>
                                <Input
                                    id="duration-minutes"
                                    type="number"
                                    value={data.duration % 60 || ''}
                                    onChange={(e) => {
                                        const minutes = Number(e.target.value) || 0;
                                        const hours = Math.floor(data.duration / 60);
                                        setData('duration', hours * 60 + minutes);
                                    }}
                                    className="mt-1 w-16"
                                    aria-invalid={!!errors.duration}
                                />
                            </Label>
                            {errors.duration && <p className="text-sm font-medium text-destructive">{errors.duration}</p>}
                        </div>

                        {/* Description (Input type text for description) */}
                        <div>
                            <Label htmlFor="description">Description</Label>
                            <Input
                                id="description"
                                type="text"
                                value={data.description}
                                onChange={(e) => setData('description', e.target.value)}
                                placeholder="Enter appointment description..."
                                className="mt-1"
                                aria-invalid={!!errors.description}
                            />
                            {errors.description && <p className="text-sm font-medium text-destructive">{errors.description}</p>}
                        </div>

                        {/* Success Message */}
                        {recentlySuccessful && (
                            <div className="rounded-md bg-green-50 p-4 text-sm text-green-800">Appointment scheduled successfully!</div>
                        )}

                        {/* Submit Button */}
                        <Button type="submit" disabled={processing} className="w-full bg-accent text-accent-foreground hover:bg-accent/90">
                            {processing ? 'Scheduling...' : 'Schedule Appointment'}
                        </Button>
                    </form>
                </DialogContent>
            </Dialog>
        </>
    );
}
