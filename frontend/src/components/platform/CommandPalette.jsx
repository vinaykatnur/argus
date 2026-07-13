import { AnimatePresence, motion } from 'framer-motion';
import { ArrowRight, BarChart3, Command, Gauge, LifeBuoy, Pin, Search, Settings, ShieldCheck, Sparkles, Wrench } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate, useLocation } from 'react-router-dom';
import { usePlatformPreferences } from '../../context/PlatformPreferencesContext';
import { demoIncidents, demoMonitors } from '../../data/demo';
import { api, hasAuthToken } from '../../lib/api';

const pages = [
  { label: 'Dashboard', description: 'Operations overview', to: '/demo', icon: Gauge, category: 'Pages', keywords: 'overview home monitors' },
  { label: 'Analytics', description: 'Signal trends and telemetry', to: '/demo/analytics', icon: BarChart3, category: 'Pages', keywords: 'charts metrics trends' },
  { label: 'Incident Intelligence', description: 'Evidence, confidence, recommendations', to: '/demo/incident', icon: Sparkles, category: 'Pages', keywords: 'incident deterministic intelligence' },
  { label: 'Settings', description: 'Appearance, security, notifications, AI providers', to: '/demo/settings', icon: Settings, category: 'Pages', keywords: 'theme notification ai providers security' },
  { label: 'Diagnostics', description: 'Runtime health and environment', to: '/demo/diagnostics', icon: LifeBuoy, category: 'Pages', keywords: 'health runtime java database cpu memory disk' },
];

const quickActions = [
  { label: 'Open AI Providers', description: 'Configure optional narrative enrichment', to: '/demo/settings', icon: Sparkles, category: 'Quick Actions', keywords: 'openai gemini claude provider api key' },
  { label: 'Replay Product Tour', description: 'Start the guided ARGUS walkthrough', action: 'replay-tour', icon: Wrench, category: 'Quick Actions', keywords: 'onboarding welcome guide' },
  { label: 'View Diagnostics', description: 'Check runtime health', to: '/demo/diagnostics', icon: LifeBuoy, category: 'Quick Actions', keywords: 'backend health database scheduler' },
];

function matches(item, term) {
  return `${item.label} ${item.description} ${item.keywords || ''}`.toLowerCase().includes(term);
}

export function CommandPalette({ open, onClose }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { preferences, replayTour, addRecentlyVisited } = usePlatformPreferences();
  const [query, setQuery] = useState('');
  const [activeIndex, setActiveIndex] = useState(0);

  const authenticated = hasAuthToken();

  const monitorsQuery = useQuery({
    queryKey: ['palette-monitors'],
    queryFn: async () => (await api.get('/api/v1/monitors', { params: { size: 50 } })).data,
    enabled: open && authenticated,
  });

  const incidentsQuery = useQuery({
    queryKey: ['palette-incidents'],
    queryFn: async () => (await api.get('/api/v1/incidents', { params: { size: 20 } })).data,
    enabled: open && authenticated,
  });

  useEffect(() => {
    if (!open) {
      setQuery('');
      setActiveIndex(0);
    }
  }, [open]);

  const groupedResults = useMemo(() => {
    const term = query.toLowerCase().trim();
    const monitorSource = monitorsQuery.data?.content?.length ? monitorsQuery.data.content : demoMonitors;
    const incidentSource = incidentsQuery.data?.content?.length ? incidentsQuery.data.content : demoIncidents;

    const pinnedIds = new Set(preferences.pinnedMonitors);
    const monitorEntries = monitorSource
      .map((monitor) => ({
        label: monitor.displayName || monitor.name || monitor.url || `Monitor ${monitor.id}`,
        description: monitor.url || monitor.region || `${monitor.status} monitor`,
        to: '/demo',
        icon: pinnedIds.has(String(monitor.id)) || monitor.pinned ? Pin : ShieldCheck,
        category: pinnedIds.has(String(monitor.id)) || monitor.pinned ? 'Pinned Monitors' : 'Monitors',
        keywords: `${monitor.status || ''} ${monitor.id || ''}`,
        pinned: pinnedIds.has(String(monitor.id)) || monitor.pinned,
      }))
      .sort((first, second) => Number(second.pinned) - Number(first.pinned));

    const incidentEntries = incidentSource.map((incident) => ({
      label: incident.title || `Incident ${incident.id}`,
      description: incident.summary || incident.monitorName || incident.status,
      to: '/demo/incident',
      icon: Sparkles,
      category: 'Incidents',
      keywords: `${incident.id || ''} ${incident.severity || ''} ${incident.status || ''}`,
    }));

    const recentEntries = (preferences.recentlyVisited || []).map((entry) => ({
      ...entry,
      icon: pages.find((page) => page.to === entry.to)?.icon || ArrowRight,
      category: 'Recently Visited',
      keywords: 'recent history',
    }));

    const all = [...quickActions, ...pages, ...monitorEntries, ...incidentEntries, ...recentEntries]
      .filter((item) => !term || matches(item, term));

    return all.reduce((groups, item) => {
      const category = item.category || 'Results';
      groups[category] = [...(groups[category] || []), item];
      return groups;
    }, {});
  }, [incidentsQuery.data, monitorsQuery.data, preferences.pinnedMonitors, preferences.recentlyVisited, query]);

  const flatResults = useMemo(() => Object.values(groupedResults).flat(), [groupedResults]);

  useEffect(() => {
    setActiveIndex(0);
  }, [query, open]);

  useEffect(() => {
    if (!open) return undefined;
    const onKeyDown = (event) => {
      if (event.key === 'Escape') {
        event.preventDefault();
        onClose();
      }
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        setActiveIndex((current) => Math.min(flatResults.length - 1, current + 1));
      }
      if (event.key === 'ArrowUp') {
        event.preventDefault();
        setActiveIndex((current) => Math.max(0, current - 1));
      }
      if (event.key === 'Enter' && flatResults[activeIndex]) {
        event.preventDefault();
        handleSelect(flatResults[activeIndex]);
      }
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [activeIndex, flatResults, onClose, open]);

  const handleSelect = (item) => {
    if (item.action === 'replay-tour') {
      replayTour();
    }
    if (item.to) {
      const isLive = !location.pathname.startsWith('/demo');
      const targetPath = isLive ? (item.to === '/demo' ? '/dashboard' : item.to.replace(/^\/demo/, '')) : item.to;
      navigate(targetPath);
      addRecentlyVisited({ label: item.label, description: item.description, to: targetPath });
    }
    onClose();
  };

  return (
    <AnimatePresence>
      {open ? (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-start justify-center bg-slate-950/80 px-4 pt-24 backdrop-blur"
          onClick={onClose}
        >
          <motion.div
            initial={{ opacity: 0, y: -8, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.98 }}
            transition={{ duration: 0.2 }}
            className="w-full max-w-2xl rounded-[28px] border border-white/10 bg-slate-900/95 p-3 shadow-2xl"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="flex items-center gap-3 rounded-2xl border border-white/10 bg-slate-950/80 px-4 py-3">
              <Command size={18} className="text-primary" />
              <input
                autoFocus
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Search pages, monitors, incidents, and actions"
                className="w-full bg-transparent text-sm text-white outline-none placeholder:text-slate-500"
              />
              <span className="rounded-full border border-white/10 px-2 py-1 text-[11px] uppercase tracking-[0.24em] text-slate-400">Esc</span>
            </div>

            <div className="mt-3 max-h-[60vh] overflow-y-auto pr-1">
              {flatResults.length ? (
                Object.entries(groupedResults).map(([category, items]) => (
                  <div key={category} className="mb-3">
                    <p className="mb-2 px-2 text-xs uppercase tracking-[0.24em] text-slate-500">{category}</p>
                    <div className="space-y-2">
                      {items.map((action) => {
                        const Icon = action.icon || Search;
                        const absoluteIndex = flatResults.indexOf(action);
                        const active = absoluteIndex === activeIndex;
                        return (
                          <button
                            key={`${category}-${action.label}-${action.to || action.action}`}
                            onMouseEnter={() => setActiveIndex(absoluteIndex)}
                            onClick={() => handleSelect(action)}
                            className={`flex w-full items-center justify-between rounded-2xl border px-4 py-3 text-left transition ${active ? 'border-primary/30 bg-primary/10' : 'border-white/10 bg-white/5 hover:bg-white/10'}`}
                          >
                            <div className="flex min-w-0 items-center gap-3">
                              <div className="rounded-full border border-primary/20 bg-primary/10 p-2 text-primary">
                                <Icon size={16} />
                              </div>
                              <div className="min-w-0">
                                <p className="truncate text-sm font-medium text-white">{action.label}</p>
                                <p className="truncate text-sm text-slate-400">{action.description}</p>
                              </div>
                            </div>
                            <ArrowRight size={16} className="ml-3 shrink-0 text-slate-500" />
                          </button>
                        );
                      })}
                    </div>
                  </div>
                ))
              ) : (
                <div className="rounded-2xl border border-white/10 bg-white/5 p-6 text-center text-sm text-slate-400">
                  No results found.
                </div>
              )}
            </div>
          </motion.div>
        </motion.div>
      ) : null}
    </AnimatePresence>
  );
}
