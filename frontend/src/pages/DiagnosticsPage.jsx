import { motion } from 'framer-motion';
import { Activity, Cpu, Database, HardDrive, Monitor, ShieldCheck, Sparkles, TimerReset } from 'lucide-react';
import { SelfHealthPanel } from '../components/platform/SelfHealthPanel';
import { SystemDiagnosticsPanel } from '../components/platform/SystemDiagnosticsPanel';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { useRuntimeDiagnostics } from '../lib/diagnostics';

export default function DiagnosticsPage() {
  const diagnosticsQuery = useRuntimeDiagnostics();
  const data = diagnosticsQuery.data;
  const healthy = data?.status === 'UP';

  const runtimeCards = [
    { icon: Monitor, label: 'Backend', value: diagnosticsQuery.isError ? 'Unavailable' : 'Spring Boot runtime' },
    { icon: Cpu, label: 'Java', value: data?.javaVersion },
    { icon: Database, label: 'Database', value: data?.services?.find((service) => service.name === 'Database')?.status },
    { icon: HardDrive, label: 'Storage', value: data?.disk?.value },
    { icon: TimerReset, label: 'Latency', value: data?.responseTimeMillis == null ? null : `${data.responseTimeMillis}ms` },
    { icon: ShieldCheck, label: 'Health Status', value: data?.status },
    { icon: Activity, label: 'Application Uptime', value: data?.uptime },
  ];

  return (
    <div className="space-y-6 py-4">
      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }} className="rounded-[28px] border border-white/10 bg-slate-900/70 p-6 shadow-soft backdrop-blur-xl">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <Badge>Diagnostics</Badge>
            <h1 className="mt-4 text-3xl font-semibold text-white">Professional operations diagnostics</h1>
            <p className="mt-2 max-w-2xl text-slate-400">A read-only view of ARGUS health, engines, and environment readiness.</p>
          </div>
          <div className={`rounded-2xl border px-4 py-3 ${healthy ? 'border-success/20 bg-success/10 text-success' : diagnosticsQuery.isLoading ? 'border-warning/20 bg-warning/10 text-warning' : 'border-danger/20 bg-danger/10 text-danger'}`}>
            <p className="text-sm font-semibold">{diagnosticsQuery.isLoading ? 'Checking system' : healthy ? 'System healthy' : 'System unavailable'}</p>
            <p className="text-xs uppercase tracking-[0.28em]">{diagnosticsQuery.isError ? 'OFFLINE' : 'LIVE'}</p>
          </div>
        </div>
      </motion.div>

      <SelfHealthPanel />
      <SystemDiagnosticsPanel />

      <Card className="p-6">
        <div className="flex items-center gap-3">
          <div className="rounded-full border border-primary/20 bg-primary/10 p-2 text-primary">
            <Sparkles size={18} />
          </div>
          <div>
            <p className="text-sm text-slate-400">Environment</p>
            <h2 className="text-xl font-semibold text-white">Runtime snapshot</h2>
          </div>
        </div>
        <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {runtimeCards.map((item) => {
            const Icon = item.icon;
            return (
              <div key={item.label} className="rounded-2xl border border-white/10 bg-slate-950/70 p-4">
                <div className="flex items-center gap-2 text-primary">
                  <Icon size={16} />
                  <p className="text-sm font-medium text-white">{item.label}</p>
                </div>
                <p className="mt-3 text-sm text-slate-400">{diagnosticsQuery.isLoading ? 'Loading...' : item.value || 'Unavailable'}</p>
              </div>
            );
          })}
        </div>
      </Card>
    </div>
  );
}
