import { Button } from '@/components/ui/button';
import { dashboard, login } from '@/routes';
import { type SharedData } from '@/types';
import { Head, Link, usePage } from '@inertiajs/react';
import { ArrowRight, Stethoscope } from 'lucide-react';

export default function Welcome() {
    const { auth } = usePage<SharedData>().props;

    return (
        <>
            <Head title="Welcome">
                <link rel="preconnect" href="https://fonts.bunny.net" />
                <link href="https://fonts.bunny.net/css?family=instrument-sans:400,500,600" rel="stylesheet" />
            </Head>
            <div className="flex min-h-screen flex-col items-center bg-[#FDFDFC] p-6 text-[#1b1b18] lg:justify-center lg:p-8 dark:bg-[#0a0a0a]">
                <header className="mb-6 w-full max-w-[335px] text-sm not-has-[nav]:hidden lg:max-w-4xl">
                    <nav className="flex items-center justify-end gap-4"></nav>
                </header>
                <div className="flex w-full items-center justify-center opacity-100 transition-opacity duration-750 lg:grow starting:opacity-0">
                    <main className="flex w-full max-w-[335px] flex-col-reverse lg:max-w-4xl lg:flex-row">
                        <div className="min-h-screen bg-gradient-to-b from-background to-slate-50 dark:to-slate-950">
                            {/* Hero Section */}
                            <section className="relative overflow-hidden px-4 pt-20 pb-32 sm:px-6 lg:px-8">
                                {/* Decorative background element */}
                                <div className="absolute inset-0 -z-10">
                                    <div className="absolute top-20 right-0 h-96 w-96 rounded-full bg-blue-100 opacity-20 blur-3xl dark:bg-blue-950" />
                                    <div className="absolute bottom-0 left-1/4 h-96 w-96 rounded-full bg-teal-100 opacity-20 blur-3xl dark:bg-teal-950" />
                                </div>

                                <div className="mx-auto max-w-3xl text-center">
                                    {/* Badge */}
                                    <div className="mb-8 inline-flex items-center gap-2 rounded-full border border-blue-200 bg-blue-50 px-4 py-2 dark:border-blue-800 dark:bg-blue-950/30">
                                        <Stethoscope className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                                        <span className="text-sm font-medium text-blue-600 dark:text-blue-400">Plateforme médicale moderne</span>
                                    </div>

                                    {/* Main Headline */}
                                    <h1 className="mb-6 text-5xl leading-tight font-bold tracking-tight text-pretty sm:text-6xl">
                                        Gérez vos rendez-vous médicaux{' '}
                                        <span className="bg-gradient-to-r from-blue-600 to-teal-600 bg-clip-text text-transparent dark:from-blue-400 dark:to-teal-400">
                                            simplement
                                        </span>
                                    </h1>

                                    {/* Subheading */}
                                    <p className="mx-auto mb-8 max-w-2xl text-lg leading-relaxed text-muted-foreground sm:text-xl">
                                        Une plateforme moderne permettant aux médecins d'organiser facilement leurs consultations, avec assistance IA
                                        intégrée.
                                    </p>

                                    {/* CTA Buttons */}
                                    <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
                                        <Button
                                            size="lg"
                                            asChild
                                            className="rounded-lg bg-blue-600 px-8 py-6 text-lg font-semibold text-white shadow-lg transition-all hover:bg-blue-700 hover:shadow-xl dark:bg-blue-500 dark:hover:bg-blue-600"
                                        >
                                            {auth.user ? (
                                                <Link href={dashboard()} className="flex items-center">
                                                    Commencer
                                                    <ArrowRight className="ml-2 h-5 w-5" />
                                                </Link>
                                            ) : (
                                                <Link href={login()} className="flex items-center">
                                                    Commencer
                                                    <ArrowRight className="ml-2 h-5 w-5" />
                                                </Link>
                                            )}
                                        </Button>
                                    </div>

                                    {/* Trust indicators */}
                                    <div className="mt-16 border-t border-border pt-8">
                                        <p className="mb-6 text-sm text-muted-foreground">Utilisé par plus de 10,000 professionnels de santé</p>
                                        <div className="flex flex-wrap items-center justify-center gap-8 opacity-70">
                                            <div className="flex items-center gap-2">
                                                <div className="h-3 w-3 rounded-full bg-blue-600" />
                                                <span className="text-sm font-medium">Sécurisé HIPAA</span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <div className="h-3 w-3 rounded-full bg-teal-600" />
                                                <span className="text-sm font-medium">Support 24/7</span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <div className="h-3 w-3 rounded-full bg-blue-600" />
                                                <span className="text-sm font-medium">IA Intégrée</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </section>
                        </div>
                    </main>
                </div>
                <div className="hidden h-14.5 lg:block"></div>
            </div>
        </>
    );
}
