import { motion } from 'framer-motion';
import { AlertTriangle, BrainCircuit, CheckCircle2, ChevronRight, Sparkles } from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { demoTimeline } from '../data/demo';

const evidence = [
  'API Gateway error rate rose 3.2x during the degradation window.',
  'Authentication response time exceeded the 250ms threshold for 14 minutes.',
  'A retry policy drift was identified in the upstream validation layer.',
];

const recommendations = [
  'Throttle the affected auth worker pool while the upstream fix is applied.',
  'Reconcile the validation cache to restore steady-state latency.',
  'Notify the platform owner for a deeper review of the dependency contract.',
];

export default function IncidentPage() {
  return (
    <div className="space-y-6 py-4" data-tour="incident-intelligence">
      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }} className="rounded-[28px] border border-white/10 bg-slate-900/70 p-6 shadow-soft backdrop-blur-xl">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <Badge>Active incident</Badge>
            <h1 className="mt-4 text-3xl font-semibold text-white">Authentication degradation</h1>
            <p className="mt-2 max-w-2xl text-slate-400">A flagship incident experience that surfaces evidence, confidence, and recommendations clearly.</p>
          </div>
          <div className="rounded-2xl border border-danger/20 bg-danger/10 px-4 py-3 text-danger">
            <p className="text-sm font-semibold">Critical</p>
            <p className="text-xs uppercase tracking-[0.28em]">INC-2047</p>
          </div>
        </div>
      </motion.div>

      <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <div className="space-y-6">
          <Card className="p-6">
            <div className="flex items-center gap-3">
              <div className="rounded-full border border-warning/20 bg-warning/10 p-2 text-warning">
                <AlertTriangle size={18} />
              </div>
              <div>
                <p className="text-sm text-slate-400">Timeline</p>
                <h2 className="text-xl font-semibold text-white">What happened</h2>
              </div>
            </div>
            <div className="mt-6 space-y-4">
              {demoTimeline.map((event, index) => (
                <motion.div key={event.time} initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.25, delay: 0.05 * index }} className="flex gap-4 rounded-2xl border border-white/10 bg-slate-950/70 p-4">
                  <div className="flex flex-col items-center">
                    <div className="mt-1 h-2.5 w-2.5 rounded-full bg-primary" />
                    {index < demoTimeline.length - 1 ? <div className="mt-2 h-full w-px bg-white/10" /> : null}
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-white">{event.time} · {event.title}</p>
                    <p className="mt-1 text-sm text-slate-400">{event.detail}</p>
                  </div>
                </motion.div>
              ))}
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-3">
              <div className="rounded-full border border-primary/20 bg-primary/10 p-2 text-primary">
                <Sparkles size={18} />
              </div>
              <div>
                <p className="text-sm text-slate-400">Recommendations</p>
                <h2 className="text-xl font-semibold text-white">Deterministic guidance</h2>
              </div>
            </div>
            <div className="mt-6 space-y-3">
              {recommendations.map((step) => (
                <div key={step} className="flex items-start gap-3 rounded-2xl border border-white/10 bg-slate-950/70 p-4">
                  <CheckCircle2 className="mt-0.5 text-success" size={16} />
                  <p className="text-sm text-slate-400">{step}</p>
                </div>
              ))}
            </div>
          </Card>
        </div>

        <div className="space-y-6">
          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-slate-400">Evidence</p>
                <h2 className="text-xl font-semibold text-white">Confidence overview</h2>
              </div>
              <Badge>92%</Badge>
            </div>
            <div className="mt-6 space-y-3">
              {evidence.map((item) => (
                <div key={item} className="rounded-2xl border border-white/10 bg-slate-950/70 p-4 text-sm text-slate-400">
                  {item}
                </div>
              ))}
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-3">
              <div className="rounded-full border border-accent/20 bg-accent/10 p-2 text-accent">
                <BrainCircuit size={18} />
              </div>
              <div>
                <p className="text-sm text-slate-400">AI narrative</p>
                <h2 className="text-xl font-semibold text-white">Optional enrichment</h2>
              </div>
            </div>
            <p className="mt-4 text-sm leading-7 text-slate-400">When a provider is configured, ARGUS can layer a concise narrative over the deterministic evidence so teams can quickly align on the situation.</p>
            <div className="mt-4 rounded-2xl border border-dashed border-white/10 bg-white/5 p-4 text-sm text-slate-400">
              Rich AI summaries are available when an external provider is connected.
            </div>
          </Card>
        </div>
      </div>

      <Card className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-slate-400">Historical matches</p>
            <h2 className="text-xl font-semibold text-white">Related incidents</h2>
          </div>
          <div className="flex items-center gap-2 text-slate-400">
            <span className="text-sm">View more</span>
            <ChevronRight size={16} />
          </div>
        </div>
        <div className="mt-6 grid gap-4 md:grid-cols-3">
          {['Upstream validation mismatch', 'Credential refresh spike', 'Queue saturation on inventory'].map((item) => (
            <div key={item} className="rounded-2xl border border-white/10 bg-slate-950/70 p-4">
              <p className="text-sm font-semibold text-white">{item}</p>
              <p className="mt-2 text-sm text-slate-400">Historical edge case that mirrors the current signal pattern.</p>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
