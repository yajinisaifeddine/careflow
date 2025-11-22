import { InertiaLinkProps } from '@inertiajs/react';
import { LucideIcon } from 'lucide-react';

export interface Auth {
    user: User;
}

export interface BreadcrumbItem {
    title: string;
    href: string;
}

export interface NavGroup {
    title: string;
    items: NavItem[];
}

export interface NavItem {
    title: string;
    href: NonNullable<InertiaLinkProps['href']>;
    icon?: LucideIcon | null;
    isActive?: boolean;
}

export interface SharedData {
    name: string;
    quote: { message: string; author: string };
    auth: Auth;
    sidebarOpen: boolean;
    [key: string]: unknown;
}

export interface User {
    id: number;
    name: string;
    email: string;
    avatar?: string;
    email_verified_at: string | null;
    created_at: string;
    updated_at: string;
    [key: string]: unknown; // This allows for additional properties...
}

export interface Appointment {
    id: number;
    date: string;
    time:string;
    duration: string;
    patient?: Patient;
    doctor?: Doctor;
    status: 'confirmed' | 'pending' | 'completed' | 'cancelled';
    description:string;
}
export interface Doctor {
    id: string;
    name: string;
}
export interface Patient {
    id: string;
    name: string;
}
export interface AppointmentData {
    patientId: string;
    date: string;
    time: string;
    duration: number;
    description:string;
}
