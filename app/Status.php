<?php

namespace App;

enum Status: string
{
    case Confirmed = 'confirmed';
    case Pending = 'pending';
    case Completed = 'completed';
    case Cancelled = 'cancelled';

}
