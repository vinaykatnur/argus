import { Activity, Database, HardDrive, Loader2, Monitor, ShieldCheck, Sparkles, TimerReset } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { useNotificationHealth, useRuntimeDiagnostics, statusTone } from '../../lib/diagnostics';

function dotClass(status) {
  const tone = statusTone(status);
  if (tone === 'success') return 'bg-success';
  if (tone === 'warning') return 'bg-warning';
  return 'bg-danger';
}

function textClass(status) {
  const tone = statusTone(status);
  if (tone === 'success') return 'text-success';
  if (tone === 'warning') return 'text-warning';
  return 'text-danger';
}

export function SystemDiagnosticsPanel() {
  const diagnosticsQuery = useRuntimeDiagnostics();
  const notificationQuery = useNotificationHealth();
  const data = diagnosticsQuery.data;
  const services = data?.services || [];

  const cards = [
    { icon: Monitor, label: 'Environment', value: data?.environment },
    { icon: Sparkles, label: 'Application Version', value: data?.applicationVersion },
    { icon: TimerReset, label: 'Application Uptime', value: data?.uptime },
    { icon: HardDrive, label: 'Disk Usage', value: data?.disk?.value },
    { icon: Database, label: 'Database Connectivity', value: services.find((service) => service.name === 'Database')?.status },
    { icon: ShieldCheck, label: 'Memory Usage', value: data?.memory?.value },
    { icon: Activity, label: 'Java Version', value: data?.javaVersion },
    { icon: TimerReset, label: 'Response Time', value: data?.responseTimeMillis == null ? null : `${data.responseTimeMillis}ms` },
  ];

  if (notificationQuery.data) {
    cards.push({
      icon: Activity,
      label: 'Notification Queue',
      value: `${notificationQuery.data.notificationQueueSize} queued, ${notificationQuery.data.retryQueueSize} retry`,
    });
  }

  return (
    <Card className="p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm text-slate-400">System diagnostics</p>
          <h2 className="mt-1 text-xl font-semibold text-white">Professional health overview</h2>
        </div>
        <Badge>{diagnosticsQuery.isLoading ? 'Loading' : diagnosticsQuery.isError ? 'Unavailable' : 'Read only'}</Badge>
      </div>

      {diagnosticsQuery.isError ? (
        <div className="mt-6 rounded-2xl border border-warning/20 bg-warning/10 p-4 text-sm text-warning">
          Runtime diagnostics endpoint is unavailable. Existing integration points are ready for live data when the backend is reachable.
        </div>
      ) : null}

      <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {(diagnosticsQuery.isLoading ? ['Backend Health', 'Database', 'Scheduler', 'Analytics Engine', 'Incident Intelligence Engine', 'AI Narrative Engine'] : services).map((service) => {
          const loading = typeof service === 'string';
          const name = loading ? service : service.name;
          const status = loading ? 'UNAVAILABLE' : service.status;
          return (
            <div key={name} className="rounded-2xl border border-white/10 bg-slate-950/70 p-4">
              <div className="flex items-center justify-between">
                <p className="font-medium text-white">{name}</p>
                <span className={`h-2.5 w-2.5 rounded-full ${dotClass(status)}`} />
              </div>
              <div className={`mt-4 flex items-center gap-2 text-sm ${loading ? 'text-slate-400' : textClass(status)}`}>
                {loading ? <Loader2 size={16} className="animate-spin" /> : <Activity size={16} />}
                <span>{loading ? 'Loading' : status}</span>
              </div>
              <p className="mt-2 text-sm text-slate-400">{loading ? 'Waiting for backend response.' : service.detail}</p>
            </div>
          );
        })}
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {cards.map((item) => {
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
  );
}
