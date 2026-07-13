import { motion } from 'framer-motion';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Activity, AlertTriangle, Bell, Cpu, ShieldCheck, Sparkles, X, Plus, Play, Pause, Trash2 } from 'lucide-react';
import { useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { MonitorCard } from '../components/platform/MonitorCard';
import { OnboardingPanel } from '../components/platform/OnboardingPanel';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { demoActivity, demoIncidents, demoMonitors } from '../data/demo';
import { usePlatformPreferences } from '../context/PlatformPreferencesContext';
import { api, apiErrorMessage } from '../lib/api';

export default function DemoDashboard() {
  const location = useLocation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { preferences, togglePin } = usePlatformPreferences();
  const [configuredMonitor, setConfiguredMonitor] = useState(null);

  // Determine mode based on path
  const isDemo = location.pathname.startsWith('/demo');

  // Monitor configure modal form states
  const [monName, setMonName] = useState('');
  const [monUrl, setMonUrl] = useState('');
  const [monInterval, setMonInterval] = useState(60);
  const [monThreshold, setMonThreshold] = useState(3);
  const [modalError, setModalError] = useState(null);
  const [modalSuccess, setModalSuccess] = useState(null);

  // Queries
  const monitorsQuery = useQuery({
    queryKey: ['monitors-search'],
    queryFn: async () => (await api.get('/api/v1/monitors', { params: { size: 50 } })).data,
    enabled: !isDemo,
  });

  const incidentsQuery = useQuery({
    queryKey: ['active-incidents'],
    queryFn: async () => (await api.get('/api/v1/incidents/active')).data,
    enabled: !isDemo,
  });

  // Pin mutation
  const pinMutation = useMutation({
    mutationFn: async ({ monitor, pinned }) => {
      const path = pinned ? `/api/v1/monitors/${monitor.id}/unpin` : `/api/v1/monitors/${monitor.id}/pin`;
      return (await api.patch(path, pinned ? undefined : { position: 0 })).data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors-search'] });
    },
    onError: (_error, variables) => {
      togglePin(String(variables.monitor.id));
    },
  });

  // CRUD mutations
  const createMutation = useMutation({
    mutationFn: async (payload) => (await api.post('/api/v1/monitors', payload)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors-search'] });
      setModalSuccess('Monitor created successfully.');
      setTimeout(() => handleCloseModal(), 1500);
    },
    onError: (err) => setModalError(apiErrorMessage(err, 'Failed to create monitor.')),
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, payload }) => (await api.put(`/api/v1/monitors/${id}`, payload)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors-search'] });
      setModalSuccess('Monitor configuration saved.');
      setTimeout(() => handleCloseModal(), 1500);
    },
    onError: (err) => setModalError(apiErrorMessage(err, 'Failed to update monitor.')),
  });

  const deleteMutation = useMutation({
    mutationFn: async (id) => (await api.delete(`/api/v1/monitors/${id}`)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors-search'] });
      setModalSuccess('Monitor deleted.');
      setTimeout(() => handleCloseModal(), 1500);
    },
    onError: (err) => setModalError(apiErrorMessage(err, 'Failed to delete monitor.')),
  });

  const pauseMutation = useMutation({
    mutationFn: async (id) => (await api.patch(`/api/v1/monitors/${id}/pause`)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors-search'] });
      setModalSuccess('Monitor paused.');
      setTimeout(() => handleCloseModal(), 1500);
    },
    onError: (err) => setModalError(apiErrorMessage(err, 'Failed to pause monitor.')),
  });

  const resumeMutation = useMutation({
    mutationFn: async (id) => (await api.patch(`/api/v1/monitors/${id}/resume`)).data,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors-search'] });
      setModalSuccess('Monitor resumed.');
      setTimeout(() => handleCloseModal(), 1500);
    },
    onError: (err) => setModalError(apiErrorMessage(err, 'Failed to resume monitor.')),
  });

  // Data processing
  const monitors = useMemo(() => {
    if (isDemo) {
      return demoMonitors.slice().sort((first, second) => {
        const firstPinned = preferences.pinnedMonitors.includes(String(first.id));
        const secondPinned = preferences.pinnedMonitors.includes(String(second.id));
        if (firstPinned !== secondPinned) return firstPinned ? -1 : 1;
        return 0;
      });
    }

    const live = monitorsQuery.data?.content || [];
    return live.slice().sort((first, second) => {
      const firstPinned = Boolean(first.pinned);
      const secondPinned = Boolean(second.pinned);
      if (firstPinned !== secondPinned) return firstPinned ? -1 : 1;
      return (first.pinnedPosition ?? 999) - (second.pinnedPosition ?? 999);
    });
  }, [isDemo, monitorsQuery.data, preferences.pinnedMonitors]);

  const incidents = useMemo(() => {
    if (isDemo) return demoIncidents;
    return incidentsQuery.data?.content || [];
  }, [isDemo, incidentsQuery.data]);

  const stats = useMemo(() => {
    if (isDemo) {
      return [
        { label: 'Availability', value: '99.95%', detail: '+0.08% vs last month', tone: 'success' },
        { label: 'Active monitors', value: '24', detail: '3 need attention', tone: 'warning' },
        { label: 'Active incidents', value: '1', detail: '1 critical path', tone: 'danger' },
        { label: 'Avg response time', value: '82ms', detail: 'Improved 11%', tone: 'info' },
      ];
    }

    const totalMonitors = monitors.length;
    const downMonitors = monitors.filter(m => m.status === 'DOWN' || m.status === 'CRITICAL').length;
    const totalIncidents = incidents.length;
    
    // Compute simple average latency from active monitors
    const activeLats = monitors.filter(m => typeof m.lastResponseTimeMillis === 'number' && m.lastResponseTimeMillis > 0);
    const avgLat = activeLats.length 
      ? Math.round(activeLats.reduce((sum, m) => sum + m.lastResponseTimeMillis, 0) / activeLats.length)
      : 0;

    return [
      { label: 'Availability', value: '99.9%', detail: 'Stable operations', tone: 'success' },
      { label: 'Active monitors', value: String(totalMonitors), detail: `${downMonitors} need attention`, tone: downMonitors > 0 ? 'warning' : 'success' },
      { label: 'Active incidents', value: String(totalIncidents), detail: totalIncidents > 0 ? `${totalIncidents} active checks failing` : 'All checks passing', tone: totalIncidents > 0 ? 'danger' : 'success' },
      { label: 'Avg response time', value: avgLat > 0 ? `${avgLat}ms` : '--', detail: 'Real-time telemetry', tone: 'info' },
    ];
  }, [isDemo, monitors, incidents]);

  // Handlers
  const handleTogglePin = (monitor, pinned) => {
    if (!isDemo) {
      pinMutation.mutate({ monitor, pinned });
    } else {
      togglePin(String(monitor.id));
    }
  };

  const handleViewActivity = (monitor) => {
    const path = isDemo ? `/demo/analytics` : `/analytics`;
    navigate(`${path}?monitorId=${monitor.id}`);
  };

  const handleOpenConfigure = (monitor) => {
    setConfiguredMonitor(monitor);
    setMonName(monitor.displayName || monitor.name || '');
    setMonUrl(monitor.url || '');
    setMonInterval(monitor.intervalSeconds || 60);
    setMonThreshold(monitor.failureThreshold || 3);
    setModalError(null);
    setModalSuccess(null);
  };

  const handleOpenCreate = () => {
    setConfiguredMonitor({});
    setMonName('');
    setMonUrl('');
    setMonInterval(60);
    setMonThreshold(3);
    setModalError(null);
    setModalSuccess(null);
  };

  const handleCloseModal = () => {
    setConfiguredMonitor(null);
    setModalError(null);
    setModalSuccess(null);
  };

  const handleSaveMonitor = (event) => {
    event.preventDefault();
    if (!monUrl) {
      setModalError('URL endpoint is required.');
      return;
    }

    const payload = {
      displayName: monName,
      url: monUrl,
      intervalSeconds: Number(monInterval),
      failureThreshold: Number(monThreshold),
      emailDowntimeNotificationsEnabled: true,
      emailRecoveryNotificationsEnabled: true
    };

    if (isDemo) {
      setModalSuccess('Demo configuration updated (local-only simulated action).');
      setTimeout(() => handleCloseModal(), 1200);
      return;
    }

    if (configuredMonitor.id) {
      updateMutation.mutate({ id: configuredMonitor.id, payload });
    } else {
      createMutation.mutate(payload);
    }
  };

  const handleDelete = () => {
    if (isDemo) {
      setModalSuccess('Demo monitor deleted (local-only simulated action).');
      setTimeout(() => handleCloseModal(), 1200);
      return;
    }
    if (window.confirm('Are you sure you want to delete this monitor?')) {
      deleteMutation.mutate(configuredMonitor.id);
    }
  };

  const handlePauseToggle = () => {
    if (isDemo) {
      setModalSuccess('Demo monitor status toggled.');
      setTimeout(() => handleCloseModal(), 1200);
      return;
    }
    if (configuredMonitor.status === 'PAUSED') {
      resumeMutation.mutate(configuredMonitor.id);
    } else {
      pauseMutation.mutate(configuredMonitor.id);
    }
  };

  return (
    <div className="space-y-6 py-4" data-tour="dashboard">
      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }} className="flex flex-wrap items-center justify-between gap-3 rounded-[28px] border border-slate-200 dark:border-white/10 bg-white/70 dark:bg-slate-900/70 p-6 shadow-soft backdrop-blur-xl">
        <div>
          <Badge>{isDemo ? 'Demo mode' : 'Live Workspace'}</Badge>
          <h1 className="mt-4 text-3xl font-semibold text-slate-900 dark:text-white">What requires your attention?</h1>
          <p className="mt-2 max-w-2xl text-slate-600 dark:text-slate-400">
            {isDemo 
              ? 'A calm, professional overview of the systems powering a fictional SaaS platform.'
              : 'Real-time telemetry and operational metrics for your connected infrastructure.'}
          </p>
        </div>
        {!isDemo ? (
          <Button variant="primary" onClick={handleOpenCreate}>
            <Plus size={16} className="mr-2" />
            Create Monitor
          </Button>
        ) : (
          <div className="flex gap-2">
            <Button variant="secondary" onClick={() => navigate(isDemo ? '/demo/analytics' : '/analytics')}>
              <Bell size={16} className="mr-2" />
              View activity
            </Button>
            <Button variant="primary" onClick={() => navigate('/demo?present=true')}>
              Present ARGUS
            </Button>
          </div>
        )}
      </motion.div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {stats.map((metric, index) => (
          <motion.div key={metric.label} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.25, delay: index * 0.05 }}>
            <Card className="p-5">
              <p className="text-sm text-slate-500 dark:text-slate-400">{metric.label}</p>
              <p className="mt-3 text-3xl font-semibold text-slate-900 dark:text-white">{metric.value}</p>
              <p className={`mt-2 text-sm ${metric.tone === 'danger' ? 'text-danger' : metric.tone === 'warning' ? 'text-warning' : metric.tone === 'success' ? 'text-success' : 'text-primary'}`}>{metric.detail}</p>
            </Card>
          </motion.div>
        ))}
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <Card className="p-6" data-tour="monitors">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-600 dark:text-slate-400">observability grid</p>
              <h2 className="mt-1 text-xl font-semibold text-slate-900 dark:text-white">System monitors</h2>
            </div>
            <Badge>{isDemo ? 'Demo data' : 'Live telemetry'}</Badge>
          </div>
          
          {monitors.length === 0 ? (
            <div className="mt-8 flex flex-col items-center justify-center rounded-2xl border border-dashed border-slate-200 dark:border-white/10 p-10 text-center text-slate-500">
              <Activity size={32} className="text-slate-400 mb-2" />
              <p className="text-sm">No monitors configured yet.</p>
              {!isDemo ? (
                <Button variant="secondary" className="mt-4" onClick={handleOpenCreate}>
                  <Plus size={16} className="mr-2" /> Configure first monitor
                </Button>
              ) : null}
            </div>
          ) : (
            <div className="mt-6 grid gap-3 md:grid-cols-2">
              {monitors.map((monitor) => (
                <MonitorCard
                  key={monitor.id}
                  monitor={monitor}
                  onConfigure={handleOpenConfigure}
                  onTogglePin={handleTogglePin}
                  onViewActivity={handleViewActivity}
                  isPinned={isDemo ? preferences.pinnedMonitors.includes(String(monitor.id)) : Boolean(monitor.pinned)}
                />
              ))}
            </div>
          )}
        </Card>

        <div className="space-y-6">
          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-slate-600 dark:text-slate-400">Recent incidents</p>
                <h2 className="mt-1 text-xl font-semibold text-slate-900 dark:text-white">Operational pulse</h2>
              </div>
              <AlertTriangle className="text-warning" size={18} />
            </div>
            <div className="mt-5 space-y-3">
              {incidents.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-950/20 py-8 px-4 text-center">
                  <ShieldCheck className="text-success mb-2" size={24} />
                  <p className="text-sm font-medium text-slate-900 dark:text-white">All systems operational</p>
                  <p className="mt-1 text-xs text-slate-500">No active incidents detected.</p>
                </div>
              ) : (
                incidents.map((incident) => (
                  <div key={incident.id} className="rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/70 p-4">
                    <div className="flex items-center justify-between">
                      <p className="font-medium text-slate-900 dark:text-white">{incident.title}</p>
                      <Badge className={incident.severity === 'CRITICAL' ? 'border-danger/30 bg-danger/10 text-danger' : 'border-warning/30 bg-warning/10 text-warning'}>
                        {incident.severity}
                      </Badge>
                    </div>
                    <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">{incident.summary}</p>
                    <p className="mt-3 text-xs uppercase tracking-[0.28em] text-slate-400 dark:text-slate-500">{incident.time || new Date(incident.startedAt).toLocaleString()}</p>
                  </div>
                ))
              )}
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-3">
              <div className="rounded-full border border-primary/20 bg-primary/10 p-2 text-primary">
                <Cpu size={18} />
              </div>
              <div>
                <p className="text-sm text-slate-600 dark:text-slate-400">Recent activity</p>
                <h2 className="text-xl font-semibold text-slate-900 dark:text-white">Signals and context</h2>
              </div>
            </div>
            <div className="mt-5 space-y-3">
              {demoActivity.slice(0, 3).map((entry) => (
                <div key={entry} className="flex items-start gap-3 rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/70 p-4">
                  <Activity size={16} className="mt-0.5 text-success shrink-0" />
                  <p className="text-sm text-slate-600 dark:text-slate-400">{entry}</p>
                </div>
              ))}
            </div>
          </Card>
        </div>
      </div>

      {isDemo ? <OnboardingPanel /> : null}

      <Card className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-slate-600 dark:text-slate-400">Incident intelligence</p>
            <h2 className="mt-1 text-xl font-semibold text-slate-900 dark:text-white">A flagship experience for response teams</h2>
          </div>
          <div className="flex items-center gap-2 text-success">
            <ShieldCheck size={16} />
            <span className="text-sm">Deterministic evidence</span>
          </div>
        </div>
        <div className="mt-6 grid gap-4 md:grid-cols-3">
          {['Evidence', 'Confidence', 'Recommendations'].map((item) => (
            <div key={item} className="rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/70 p-4">
              <div className="flex items-center gap-2 text-primary">
                <Sparkles size={16} />
                <p className="font-medium text-slate-900 dark:text-white">{item}</p>
              </div>
              <p className="mt-3 text-sm text-slate-600 dark:text-slate-400">Professional, explainable insight that keeps teams aligned during incidents.</p>
            </div>
          ))}
        </div>
      </Card>

      {/* Monitor Configure/Create Dialog Modal */}
      {configuredMonitor ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 px-4 backdrop-blur-sm" onClick={handleCloseModal}>
          <div className="w-full max-w-lg rounded-[28px] border border-slate-200 dark:border-white/10 bg-white dark:bg-slate-900 p-6 shadow-2xl transition-all duration-300" onClick={(event) => event.stopPropagation()}>
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {configuredMonitor.id ? 'Configure Monitor' : 'Create Monitor'}
                </p>
                <h2 className="mt-1 text-xl font-semibold text-slate-900 dark:text-white">
                  {configuredMonitor.id ? (configuredMonitor.displayName || configuredMonitor.name) : 'New target URL'}
                </h2>
              </div>
              <button type="button" onClick={handleCloseModal} className="rounded-full border border-slate-200 dark:border-white/10 bg-slate-100 dark:bg-white/5 p-2 text-slate-500 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-white/10">
                <X size={16} />
              </button>
            </div>

            <form onSubmit={handleSaveMonitor} className="mt-6 space-y-4">
              {modalError ? (
                <div className="rounded-2xl border border-danger/20 bg-danger/10 p-4 text-sm text-danger">
                  {modalError}
                </div>
              ) : null}

              {modalSuccess ? (
                <div className="rounded-2xl border border-success/20 bg-success/10 p-4 text-sm text-success">
                  {modalSuccess}
                </div>
              ) : null}

              <label className="block">
                <span className="mb-2 block text-sm text-slate-600 dark:text-slate-400">Display Name</span>
                <input 
                  type="text"
                  value={monName}
                  onChange={(e) => setMonName(e.target.value)}
                  placeholder="e.g. Authentication API Gateway"
                  className="w-full rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/80 px-4 py-3 text-sm text-slate-900 dark:text-white outline-none focus:border-primary/50"
                />
              </label>

              <label className="block">
                <span className="mb-2 block text-sm text-slate-600 dark:text-slate-400">Endpoint URL (required)</span>
                <input 
                  type="text"
                  required
                  value={monUrl}
                  onChange={(e) => setMonUrl(e.target.value)}
                  placeholder="e.g. https://auth.company.com/health"
                  className="w-full rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/80 px-4 py-3 text-sm text-slate-900 dark:text-white outline-none focus:border-primary/50"
                />
              </label>

              <div className="grid grid-cols-2 gap-4">
                <label className="block">
                  <span className="mb-2 block text-sm text-slate-600 dark:text-slate-400">Interval (seconds)</span>
                  <input 
                    type="number"
                    min="10"
                    value={monInterval}
                    onChange={(e) => setMonInterval(e.target.value)}
                    className="w-full rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/80 px-4 py-3 text-sm text-slate-900 dark:text-white outline-none focus:border-primary/50"
                  />
                </label>

                <label className="block">
                  <span className="mb-2 block text-sm text-slate-600 dark:text-slate-400">Failure Threshold</span>
                  <input 
                    type="number"
                    min="1"
                    value={monThreshold}
                    onChange={(e) => setMonThreshold(e.target.value)}
                    className="w-full rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/80 px-4 py-3 text-sm text-slate-900 dark:text-white outline-none focus:border-primary/50"
                  />
                </label>
              </div>

              <div className="mt-8 flex flex-wrap gap-2 justify-between items-center">
                {configuredMonitor.id ? (
                  <div className="flex gap-2">
                    <Button type="button" variant="secondary" onClick={handlePauseToggle} disabled={pauseMutation.isPending || resumeMutation.isPending}>
                      <Pause size={14} className="mr-1.5" />
                      {configuredMonitor.status === 'PAUSED' ? 'Resume' : 'Pause'}
                    </Button>
                    <Button type="button" variant="secondary" onClick={handleDelete} className="hover:border-danger hover:text-danger" disabled={deleteMutation.isPending}>
                      <Trash2 size={14} className="mr-1.5" />
                      Delete
                    </Button>
                  </div>
                ) : <div />}

                <div className="flex gap-2">
                  <Button type="button" variant="ghost" onClick={handleCloseModal}>Cancel</Button>
                  <Button type="submit" variant="primary" disabled={createMutation.isPending || updateMutation.isPending}>
                    Save Changes
                  </Button>
                </div>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  );
}
