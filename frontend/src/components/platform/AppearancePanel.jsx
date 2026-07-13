import { useEffect, useMemo, useState } from 'react';
import { Moon, Palette, Sparkles, Sun, Zap } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { usePlatformPreferences } from '../../context/PlatformPreferencesContext';

const themes = [
  { value: 'dark', label: 'Dark' },
  { value: 'light', label: 'Light' },
  { value: 'system', label: 'System' },
];

const accents = [
  { value: '#4F8CFF', label: 'Blue' },
  { value: '#A78BFA', label: 'Violet' },
  { value: '#22C55E', label: 'Green' },
  { value: '#F59E0B', label: 'Amber' },
];

export function AppearancePanel() {
  const { preferences, setPreferences } = usePlatformPreferences();
  const [mounted, setMounted] = useState(false);

  useEffect(() => setMounted(true), []);

  const densityLabel = useMemo(() => (preferences.density === 'compact' ? 'Compact' : 'Comfortable'), [preferences.density]);

  return (
    <Card className="p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm text-slate-400">Appearance</p>
          <h2 className="mt-1 text-xl font-semibold text-white">Shape the visual tone</h2>
        </div>
        <Badge>{mounted ? 'Live' : 'Preparing'}</Badge>
      </div>

      <div className="mt-6 grid gap-4 xl:grid-cols-[1.1fr_0.9fr]">
        <div className="space-y-4">
          <label className="block">
            <span className="mb-2 block text-sm text-slate-400">Theme</span>
            <select value={preferences.theme} onChange={(event) => setPreferences((current) => ({ ...current, theme: event.target.value }))} className="w-full rounded-2xl border border-white/10 bg-slate-950/80 px-4 py-3 text-sm text-white outline-none">
              {themes.map((item) => (
                <option key={item.value} value={item.value}>{item.label}</option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="mb-2 block text-sm text-slate-400">Accent color</span>
            <div className="flex flex-wrap gap-3">
              {accents.map((accent) => (
                <button key={accent.value} type="button" onClick={() => setPreferences((current) => ({ ...current, accentColor: accent.value }))} className={`flex items-center gap-2 rounded-full border px-3 py-2 text-sm transition ${preferences.accentColor === accent.value ? 'border-primary/30 bg-primary/10 text-white' : 'border-white/10 bg-white/5 text-slate-400'}`}>
                  <span className="h-3.5 w-3.5 rounded-full" style={{ backgroundColor: accent.value }} />
                  {accent.label}
                </button>
              ))}
            </div>
          </label>

          <label className="block">
            <span className="mb-2 block text-sm text-slate-400">Density</span>
            <select value={preferences.density} onChange={(event) => setPreferences((current) => ({ ...current, density: event.target.value }))} className="w-full rounded-2xl border border-white/10 bg-slate-950/80 px-4 py-3 text-sm text-white outline-none">
              <option value="comfortable">Comfortable</option>
              <option value="compact">Compact</option>
            </select>
          </label>
        </div>

        <div className="space-y-4">
          <div className="rounded-2xl border border-white/10 bg-slate-950/70 p-5">
            <div className="flex items-center gap-2 text-primary">
              <Palette size={16} />
              <p className="font-medium text-white">Interaction preferences</p>
            </div>
            <div className="mt-4 space-y-3 text-sm text-slate-400">
              <label className="flex items-center justify-between gap-3 rounded-2xl border border-white/10 bg-white/5 px-3 py-3">
                <span>Reduced motion</span>
                <input type="checkbox" checked={preferences.reducedMotion} onChange={(event) => setPreferences((current) => ({ ...current, reducedMotion: event.target.checked }))} className="h-4 w-4 rounded border-white/10 bg-slate-900" />
              </label>
              <label className="flex items-center justify-between gap-3 rounded-2xl border border-white/10 bg-white/5 px-3 py-3">
                <span>Chart animation</span>
                <input type="checkbox" checked={preferences.chartAnimation} onChange={(event) => setPreferences((current) => ({ ...current, chartAnimation: event.target.checked }))} className="h-4 w-4 rounded border-white/10 bg-slate-900" />
              </label>
            </div>
          </div>

          <div className="rounded-2xl border border-dashed border-white/10 bg-white/5 p-5 text-sm leading-7 text-slate-400">
            <div className="flex items-center gap-2 text-accent">
              <Sparkles size={16} />
              <span className="font-medium text-white">Preview</span>
            </div>
            <p className="mt-3">Theme and density changes apply instantly with smooth transitions. No page refresh is required.</p>
          </div>
        </div>
      </div>
    </Card>
  );
}
