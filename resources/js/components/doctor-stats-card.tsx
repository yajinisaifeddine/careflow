import { Activity, TrendingUp } from 'lucide-react';

type DashboardProps = {
    visitsLastWeek: number;
    visitsThisWeek: number;
};
export function StatsCards({ visitsLastWeek, visitsThisWeek }: DashboardProps) {
    return (
        <div className="flex flex-col gap-4 overflow-x-auto rounded-xl p-4">
            <div className="grid auto-rows-min gap-4 md:grid-cols-2">
                {/* Last Week Card */}
                <div className="group relative overflow-hidden rounded-lg border border-border bg-card p-6 transition-all duration-300 hover:border-accent hover:shadow-md">
                    {/* Decorative background gradient */}
                    <div className="absolute -top-8 -right-8 h-32 w-32 rounded-full bg-primary/5 blur-2xl transition-all duration-500 group-hover:bg-primary/10" />

                    <div className="relative flex flex-col gap-4">
                        {/* Header with icon and label */}
                        <div className="flex items-start justify-between">
                            <div className="flex flex-col gap-1">
                                <p className="text-sm font-medium text-muted-foreground">Last Week</p>
                                <div className="flex items-baseline gap-2">
                                    <h3 className="text-3xl font-bold text-foreground">{visitsLastWeek}</h3>
                                    <span className="text-xs text-muted-foreground">visits</span>
                                </div>
                            </div>
                            <div className="rounded-full bg-primary/10 p-2">
                                <Activity className="h-5 w-5 text-primary" strokeWidth={2} />
                            </div>
                        </div>

                        {/* Optional stats line */}
                        <div className="h-1 w-full overflow-hidden rounded-full bg-muted">
                            <div className="h-full w-2/3 rounded-full bg-primary/60" />
                        </div>
                    </div>
                </div>

                {/* This Week Card */}
                <div className="group relative overflow-hidden rounded-lg border border-border bg-card p-6 transition-all duration-300 hover:border-accent hover:shadow-md">
                    {/* Decorative background gradient */}
                    <div className="absolute -top-8 -right-8 h-32 w-32 rounded-full bg-accent/5 blur-2xl transition-all duration-500 group-hover:bg-accent/10" />

                    <div className="relative flex flex-col gap-4">
                        {/* Header with icon and label */}
                        <div className="flex items-start justify-between">
                            <div className="flex flex-col gap-1">
                                <p className="text-sm font-medium text-muted-foreground">This Week</p>
                                <div className="flex items-baseline gap-2">
                                    <h3 className="text-3xl font-bold text-foreground">{visitsThisWeek}</h3>
                                    <span className="text-xs text-muted-foreground">visits</span>
                                </div>
                            </div>
                            <div className="rounded-full bg-accent/10 p-2">
                                <Activity className="h-5 w-5 text-accent" strokeWidth={2} />
                            </div>
                        </div>

                        {/* Trend indicator */}
                        <div className="flex items-center gap-2">
                            <div className="flex items-center gap-1 rounded-full bg-muted px-2 py-1">
                                <>
                                    <TrendingUp className="h-4 w-4 text-green-500" strokeWidth={2} />
                                    <span className="text-xs font-semibold text-green-600 dark:text-green-400">+12%</span>
                                </>

                                {/*
                                <>
                                        <TrendingDown className="h-4 w-4 text-red-500" strokeWidth={2} />
                                        <span className="text-xs font-semibold text-red-600 dark:text-red-400">-{change}%</span>
                                    </>
                                */}
                            </div>
                            <span className="text-xs text-muted-foreground">vs last week</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
