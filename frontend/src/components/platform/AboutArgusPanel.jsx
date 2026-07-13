import { BookOpen, Cpu, Layers3, ShieldCheck, Sparkles, Wrench } from 'lucide-react';
import { Card } from '../ui/Card';
import { Button } from '../ui/Button';
import { usePlatformPreferences } from '../../context/PlatformPreferencesContext';
import { useSearchParams } from 'react-router-dom';

const items = [
  { label: 'Version', value: '0.0.1-SNAPSHOT' },
  { label: 'Architecture', value: 'Modular Spring Boot + React SaaS experience' },
  { label: 'Tech stack', value: 'Java 21, Spring Boot 3.5, React 19, Vite, Tailwind' },
  { label: 'Build date', value: '2026-07-08' },
  { label: 'Environment', value: 'Development / Production-ready profile' },
  { label: 'License', value: 'MIT-style demo distribution' },
];

export function AboutArgusPanel() {
  const { replayTour } = usePlatformPreferences();
  const [searchParams, setSearchParams] = useSearchParams();

  const startPresentation = () => {
    const next = new URLSearchParams(searchParams);
    next.set('present', 'true');
    setSearchParams(next);
  };

  return (
    <Card className="p-6">
      <div className="flex items-center gap-3">
        <div className="rounded-full border border-primary/20 bg-primary/10 p-2 text-primary">
          <Sparkles size={18} />
        </div>
        <div>
          <p className="text-sm text-slate-400">About ARGUS</p>
          <h2 className="text-xl font-semibold text-white">Produced for premium operations teams</h2>
        </div>
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-2">
        {items.map((item) => (
          <div key={item.label} className="rounded-2xl border border-white/10 bg-slate-950/70 p-4">
            <p className="text-sm font-medium text-white">{item.label}</p>
            <p className="mt-2 text-sm text-slate-400">{item.value}</p>
          </div>
        ))}
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-3">
        {[
          { icon: ShieldCheck, label: 'Deterministic intelligence', detail: 'Explainable, stable, and audit-friendly' },
          { icon: Layers3, label: 'Modular architecture', detail: 'Operationally focused and extensible' },
          { icon: Wrench, label: 'Deployment ready', detail: 'Docker, CI/CD, and environment profiles included' },
        ].map((item) => {
          const Icon = item.icon;
          return (
            <div key={item.label} className="rounded-2xl border border-white/10 bg-white/5 p-4">
              <div className="flex items-center gap-2 text-primary">
                <Icon size={16} />
                <p className="font-medium text-white">{item.label}</p>
              </div>
              <p className="mt-3 text-sm text-slate-400">{item.detail}</p>
            </div>
          );
        })}
      </div>
      <div className="mt-6 flex justify-end gap-3">
        <Button variant="secondary" onClick={startPresentation}>
          Present ARGUS
        </Button>
        <Button variant="secondary" onClick={replayTour}>
          <BookOpen size={16} className="mr-2 text-primary" />
          Replay Product Tour
        </Button>
      </div>
    </Card>
  );
}
