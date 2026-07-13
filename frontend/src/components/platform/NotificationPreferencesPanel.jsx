import { Bell, CheckCircle2, Info, Mail, Smartphone } from 'lucide-react';
import { useMemo, useState } from 'react';
import { usePlatformPreferences } from '../../context/PlatformPreferencesContext';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';

const options = [
  { key: 'browserNotifications', label: 'Browser notifications', description: 'Desktop and browser alerts for active incidents.', icon: Bell, local: true },
  { key: 'emailNotifications', label: 'Email notifications', description: 'Email routing is available per monitor where backend preferences exist.', icon: Mail, local: false },
  { key: 'incidentAlerts', label: 'Incident alerts', description: 'Critical event notifications across the ARGUS UI.', icon: Smartphone, local: true },
  { key: 'dailySummary', label: 'Daily summary', description: 'Global digest preferences are prepared for a future backend endpoint.', icon: Mail, local: false },
  { key: 'maintenanceAlerts', label: 'Maintenance alerts', description: 'Scheduled work and release summary preferences.', icon: Bell, local: true },
];

export function NotificationPreferencesPanel() {
  const { preferences, setPreferences } = usePlatformPreferences();
  const [feedback, setFeedback] = useState(null);
  const notificationPrefs = preferences.notifications || {};

  const browserPermission = useMemo(() => {
    if (typeof window === 'undefined' || !('Notification' in window)) {
      return 'unavailable';
    }
    return window.Notification.permission;
  }, [notificationPrefs.browserNotifications]);

  const toggle = async (option) => {
    if (option.key === 'browserNotifications' && !notificationPrefs.browserNotifications && 'Notification' in window) {
      const permission = await window.Notification.requestPermission();
      if (permission === 'denied') {
        setFeedback({ type: 'error', message: 'Browser notifications were blocked by the browser.' });
        return;
      }
    }

    setPreferences((current) => ({
      ...current,
      notifications: {
        ...current.notifications,
        [option.key]: !current.notifications?.[option.key],
      },
    }));
    setFeedback({ type: 'success', message: `${option.label} updated.` });
  };

  return (
    <Card className="p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm text-slate-400">Notifications</p>
          <h2 className="mt-1 text-xl font-semibold text-white">Flexible delivery preferences</h2>
        </div>
        <Badge>Saved locally</Badge>
      </div>

      {feedback ? (
        <div className={`mt-4 flex items-center gap-2 rounded-2xl border px-4 py-3 text-sm ${feedback.type === 'success' ? 'border-success/20 bg-success/10 text-success' : 'border-danger/20 bg-danger/10 text-danger'}`}>
          {feedback.type === 'success' ? <CheckCircle2 size={16} /> : <Info size={16} />}
          <span>{feedback.message}</span>
        </div>
      ) : null}

      <div className="mt-6 space-y-3">
        {options.map((option) => {
          const Icon = option.icon;
          const enabled = Boolean(notificationPrefs[option.key]);
          return (
            <div key={option.key} className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-white/10 bg-slate-950/70 px-4 py-4">
              <div className="flex items-start gap-3">
                <div className="rounded-full border border-primary/20 bg-primary/10 p-2 text-primary">
                  <Icon size={16} />
                </div>
                <div>
                  <p className="text-sm font-medium text-white">{option.label}</p>
                  <p className="mt-1 text-sm text-slate-400">{option.description}</p>
                  {!option.local ? <p className="mt-2 text-xs text-warning">Global backend endpoint unavailable. Integration point prepared.</p> : null}
                  {option.key === 'browserNotifications' ? <p className="mt-2 text-xs text-slate-500">Browser permission: {browserPermission}</p> : null}
                </div>
              </div>
              <button type="button" onClick={() => toggle(option)} className={`relative h-6 w-11 rounded-full transition ${enabled ? 'bg-primary' : 'bg-white/10'}`} aria-pressed={enabled} aria-label={option.label}>
                <span className={`absolute left-1 top-1 h-4 w-4 rounded-full bg-white transition ${enabled ? 'translate-x-5' : ''}`} />
              </button>
            </div>
          );
        })}
      </div>

      <div className="mt-6 rounded-2xl border border-dashed border-white/10 bg-white/5 p-4 text-sm leading-7 text-slate-400">
        Email downtime and recovery notifications remain monitor-level settings in ARGUS. Settings does not contain monitor configuration.
      </div>
    </Card>
  );
}
