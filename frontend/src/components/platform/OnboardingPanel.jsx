import { CheckCircle2, Sparkles, Wrench } from 'lucide-react';
import { Card } from '../ui/Card';

const steps = [
  'Connect your first monitor',
  'Review the incident intelligence summary',
  'Enable alerts and notification routing',
];

export function OnboardingPanel() {
  return (
    <Card className="p-6">
      <div className="flex items-center gap-3">
        <div className="rounded-full border border-primary/20 bg-primary/10 p-2 text-primary">
          <Sparkles size={18} />
        </div>
        <div>
          <p className="text-sm text-slate-400">Platform onboarding</p>
          <h2 className="text-xl font-semibold text-white">A faster path to production readiness</h2>
        </div>
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-[1.1fr_0.9fr]">
        <div className="rounded-2xl border border-white/10 bg-slate-950/70 p-5">
          <div className="flex items-center gap-2 text-success">
            <CheckCircle2 size={16} />
            <p className="text-sm font-semibold text-white">Recommended next actions</p>
          </div>
          <ul className="mt-4 space-y-3">
            {steps.map((step) => (
              <li key={step} className="flex items-start gap-3 text-sm text-slate-400">
                <span className="mt-1 h-2.5 w-2.5 rounded-full bg-primary" />
                <span>{step}</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="rounded-2xl border border-dashed border-white/10 bg-white/5 p-5 text-sm text-slate-400">
          <div className="flex items-center gap-2 text-primary">
            <Wrench size={16} />
            <span className="font-medium text-white">Self-diagnostics</span>
          </div>
          <p className="mt-3 leading-7">ARGUS surfaces configuration health, alert routing, and incident narrative readiness in one place so operators can act quickly.</p>
        </div>
      </div>
    </Card>
  );
}
