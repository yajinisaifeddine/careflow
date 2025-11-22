import { Button } from '@/components/ui/button';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils';
import { Check, ChevronsUpDown } from 'lucide-react';
import { useState } from 'react';

interface Patient {
    id: string;
    name: string;
}

type PatientSelectProps = {
    patients: Patient[];
    setSelected: (patient: Patient) => void;
    placeholder?: string;
};

export function PatientSelect({ patients, setSelected, placeholder = 'Select a patient...' }: PatientSelectProps) {
    const [open, setOpen] = useState(false);
    const [selectedPatient, setSelectedPatient] = useState<Patient | undefined>(undefined);

    return (
        <Popover open={open} onOpenChange={setOpen} modal={true} >
            <PopoverTrigger asChild >
                <Button variant="outline" role="combobox" aria-expanded={open} className="w-full justify-between">
                    {selectedPatient ? selectedPatient.name : placeholder}
                    <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-full p-0" >
                <Command>
                    <CommandInput placeholder="Search patients..." />
                    <CommandEmpty>No patient found.</CommandEmpty>
                    <CommandList>
                        <CommandGroup>
                            {patients.map((patient) => (
                                <CommandItem
                                    className="cursor-pointer"
                                    key={patient.id}
                                    value={patient.name}
                                    onSelect={() => {
                                        setSelected(patient);
                                        setSelectedPatient(patient);
                                        setOpen(false);
                                    }}
                                >
                                    <Check className={cn('mr-2 h-4 w-4', selectedPatient?.id === patient.id ? 'opacity-100' : 'opacity-0')} />
                                    {patient.name}
                                </CommandItem>
                            ))}
                        </CommandGroup>
                    </CommandList>
                </Command>
            </PopoverContent>
        </Popover>
    );
}
