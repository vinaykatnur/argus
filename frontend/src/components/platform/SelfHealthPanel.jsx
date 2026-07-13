import { Activity, Cpu, HardDrive, Loader2, ShieldCheck, TimerReset } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { useRuntimeDiagnostics, statusTone } from '../../lib/diagnostics';

function toneClass(tone) {
  if (tone === 'success') return 'text-success';
  if (tone === 'warning') return 'text-warning';
  return 'text-danger';
}

export function SelfHealthPanel() {
  const diagnosticsQuery = useRuntimeDiagnostics();
  const data = diagnosticsQuery.data;
  const services = data?.services || [];
  const availableServices = services.filter((service) => service.status !== 'UNAVAILABLE');
  const upServices = availableServices.filter((service) => service.status === 'UP' || service.status === 'OPTIONAL');
  const score = availableServices.length ? Math.round((upServices.length / availableServices.length) * 100) : null;
  const overallTone = diagnosticsQuery.isLoading ? 'warning' : statusTone(data?.status);

  return (
    <Card className="p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm text-slate-400">ARGUS self health</p>
          <h2 className="mt-1 text-xl font-semibold text-white">Live service health</h2>
        </div>
        <Badge>{diagnosticsQuery.isLoading ? 'Loading' : diagnosticsQuery.isError ? 'Unavailable' : 'Live'}</Badge>
      </div>

      <div className="mt-6 rounded-2xl border border-white/10 bg-gradient-to-br from-primary/15 via-slate-950/80 to-accent/10 p-5">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm text-slate-400">Overall health score</p>
            <p className="mt-2 text-4xl font-semibold text-white">{score == null ? '--' : `${score}%`}</p>
          </div>
          <div className={`rounded-full border px-4 py-2 text-sm ${overallTone === 'success' ? 'border-success/20 bg-success/10 text-success' : overallTone === 'warning' ? 'border-warning/20 bg-warning/10 text-warning' : 'border-danger/20 bg-danger/10 text-danger'}`}>
            {diagnosticsQuery.isLoading ? 'Checking backend health' : diagnosticsQuery.isError ? 'Runtime diagnostics unavailable' : data.status === 'UP' ? 'Critical services healthy' : 'System needs attention'}
          </div>
        </div>
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {(diagnosticsQuery.isLoading ? ['Backend Health', 'Database', 'Scheduler', 'Incident Intelligence Engine'] : services).slice(0, 4).map((service) => {
          const serviceName = typeof service === 'string' ? service : service.name;
          const tone = typeof service === 'string' ? 'warning' : statusTone(service.status);
          return (
            <div key={serviceName} className="rounded-2xl border border-white/10 bg-slate-950/70 p-4">
              <div className="flex items-center justify-between">
                <p className="font-medium text-white">{serviceName}</p>
                <span className={`h-2.5 w-2.5 rounded-full ${tone === 'success' ? 'bg-success' : tone === 'warning' ? 'bg-warning' : 'bg-danger'}`} />
              </div>
              <p className="mt-3 text-sm text-slate-400">{typeof service === 'string' ? 'Loading...' : service.status}</p>
              <div className="mt-3 flex items-center gap-2 text-sm text-slate-400">
                {diagnosticsQuery.isLoading ? <Loader2 size={16} className="animate-spin text-primary" /> : <TimerReset size={16} className="text-primary" />}
                <span>{typeof service === 'string' ? 'Measuring' : service.responseTimeMillis == null ? 'No latency endpoint' : `${service.responseTimeMillis}ms`}</span>
              </div>
            </div>
          );
        })}
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-3">
        {[
          { icon: Cpu, label: 'CPU Usage', item: data?.cpu },
          { icon: HardDrive, label: 'Disk Usage', item: data?.disk },
          { icon: ShieldCheck, label: 'Application Uptime', item: data ? { value: data.uptime } : null },
        ].map((entry) => {
          const Icon = entry.icon;
          return (
            <div key={entry.label} className="rounded-2xl border border-white/10 bg-slate-950/70 p-4">
              <div className="flex items-center gap-2 text-primary">
                <Icon size={16} />
                <p className="text-sm font-medium text-white">{entry.label}</p>
              </div>
              <p className="mt-3 text-sm text-slate-400">{diagnosticsQuery.isLoading ? 'Loading...' : diagnosticsQuery.isError ? 'Unavailable' : entry.item?.value || 'Unavailable'}</p>
            </div>
          );
        })}
      </div>
    </Card>
  );
}
