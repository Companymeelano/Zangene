<?php

namespace App\Console;

use App\Console\Commands\SmsAutomationCommand;
use Illuminate\Console\Scheduling\Schedule;
use Illuminate\Foundation\Console\Kernel as ConsoleKernel;

class Kernel extends ConsoleKernel
{
    protected $commands = [
        SmsAutomationCommand::class,
    ];

    protected function schedule(Schedule $schedule): void
    {
        // v6.8 · one quiet pass every morning: reminders, birthdays, reviews, digest.
        $schedule->command('asiye:automations')
            ->dailyAt('10:00')
            ->withoutOverlapping();
    }
}
