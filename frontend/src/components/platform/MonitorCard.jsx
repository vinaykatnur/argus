import { motion } from 'framer-motion';
import { Cog, Pin, PinOff, Sparkles } from 'lucide-react';
import { usePlatformPreferences } from '../../context/PlatformPreferencesContext';
import { Badge } from '../ui/Badge';

export function MonitorCard({ monitor, onConfigure, onTogglePin, onViewActivity, isPinned }) {
  const { preferences, togglePin } = usePlatformPreferences();
  const pinned = typeof isPinned === 'boolean' ? isPinned : preferences.pinnedMonitors.includes(String(monitor.id)) || Boolean(monitor.pinned);
  const status = String(monitor.status || '').toLowerCase();
  const latency = monitor.latency ?? monitor.currentResponseTimeMillis ?? monitor.lastResponseTimeMillis ?? '--';
  const uptime = monitor.uptime ?? (monitor.paused ? 'Paused' : 'Tracked');
  const name = monitor.name || monitor.displayName || monitor.url || `Monitor ${monitor.id}`;
  const region = monitor.region || monitor.url || 'ARGUS monitor';

  const handlePin = () => {
    if (onTogglePin) {
      onTogglePin(monitor, pinned);
      return;
    }
    togglePin(String(monitor.id));
  };

  return (
    <motion.div layout whileHover={{ y: -2, scale: 1.01 }} className={`rounded-2xl border p-4 transition ${pinned ? 'border-primary/20 bg-primary/10' : 'border-white/10 bg-slate-950/70'}`}>
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <div className={`h-2.5 w-2.5 rounded-full ${status === 'critical' || status === 'down' ? 'bg-danger' : status === 'warning' || status === 'degraded' ? 'bg-warning' : 'bg-success'}`} />
            <p className="font-medium text-white">{name}</p>
          </div>
          <p className="mt-2 text-sm text-slate-400">{region}</p>
        </div>
        <button type="button" onClick={handlePin} className="rounded-full border border-white/10 bg-white/5 p-2 text-slate-300 transition hover:text-primary" aria-label={pinned ? 'Unpin monitor' : 'Pin monitor'}>
          {pinned ? <PinOff size={14} /> : <Pin size={14} />}
        </button>
      </div>

      <div className="mt-4 flex items-center justify-between text-sm text-slate-400">
        <span>{typeof latency === 'number' ? `${latency} ms` : latency}</span>
        <span>{typeof uptime === 'number' ? `${uptime}% uptime` : uptime}</span>
      </div>

      <div className="mt-4 flex items-center justify-between gap-3">
        <Badge>{monitor.status}</Badge>
        <div className="flex gap-2">
          <button 
            type="button" 
            onClick={() => onViewActivity?.(monitor)} 
            className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs text-slate-300 transition hover:bg-white/10 hover:text-white"
          >
            View Activity
          </button>
          <button 
            type="button" 
            onClick={() => onConfigure?.(monitor)} 
            className="inline-flex items-center justify-center rounded-full border border-white/10 bg-white/5 p-2 text-slate-300 transition hover:bg-white/10 hover:text-white"
            aria-label="Configure"
          >
            <Cog size={14} />
          </button>
        </div>
      </div>

      <div className="mt-4 rounded-2xl border border-white/10 bg-slate-900/70 px-3 py-2 text-sm text-slate-400">
        <div className="flex items-center gap-2 text-primary">
          <Sparkles size={14} />
          <span>Deterministic evidence available</span>
        </div>
      </div>
    </motion.div>
  );
}
