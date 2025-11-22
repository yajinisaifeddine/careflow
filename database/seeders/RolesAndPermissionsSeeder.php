<?php

namespace Database\Seeders;

use Spatie\Permission\Models\Role;
use Spatie\Permission\Models\Permission;
use Illuminate\Database\Console\Seeds\WithoutModelEvents;
use Illuminate\Database\Seeder;

class RolesAndPermissionsSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $permissions = [
            'confirm appointment',
            'request appointment',
            'create appointment',
            'reschedule appointment',
            'update appointment status',
            'delete appointment',
            'view appointment',
            'manage users',
        ];

        foreach ($permissions as $permission) {
            Permission::create(['name' => $permission]);
        }
        $adminRole = Role::create(['name' => 'admin']);
        $patientRole = Role::create(['name' => 'patient']);
        $doctorRole = Role::create(['name' => 'doctor']);

        $adminRole->syncPermissions(Permission::all());
        $doctorRole->syncPermissions([
            'create appointment',
            'reschedule appointment',
            'update appointment status',
            'view appointment',
            'confirm appointment',
            'request appointment',
        ]);
        $patientRole->syncPermissions([
            'create appointment',
            'update appointment status',
            'view appointment',
            'confirm appointment',
            'request appointment',
        ]);

        //
    }
}
