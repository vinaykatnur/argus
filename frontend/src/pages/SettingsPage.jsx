import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Bell, Cpu, ShieldCheck, Sparkles, Telescope, UserCircle2 } from 'lucide-react';
import { AppearancePanel } from '../components/platform/AppearancePanel';
import { AiProviderPanel } from '../components/platform/AiProviderPanel';
import { AboutArgusPanel } from '../components/platform/AboutArgusPanel';
import { NotificationPreferencesPanel } from '../components/platform/NotificationPreferencesPanel';
import { SecurityPanel } from '../components/platform/SecurityPanel';
import { SystemDiagnosticsPanel } from '../components/platform/SystemDiagnosticsPanel';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';

const sections = [
  { title: 'Profile', description: 'Operator identity and preferences', icon: UserCircle2 },
  { title: 'Appearance', description: 'Theme, accents, motion, density', icon: Sparkles },
  { title: 'Notifications', description: 'Delivery channels and escalation settings', icon: Bell },
  { title: 'AI Providers', description: 'Optional narrative enrichment', icon: Sparkles },
  { title: 'Security', description: 'Session and credential controls', icon: ShieldCheck },
  { title: 'Diagnostics', description: 'Read-only health indicators', icon: Telescope },
  { title: 'About ARGUS', description: 'Version, architecture, build details', icon: Cpu },
];

function ProfilePanel() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    try {
      const stored = window.localStorage.getItem('argus-user');
      if (stored) {
        setUser(JSON.parse(stored));
      }
    } catch (e) {
      console.error('Failed to parse user details', e);
    }
  }, []);

  const name = user?.name || 'Taylor Nguyen';
  const email = user?.email || 'taylor@company.com';
  const role = user?.role || 'USER';

  return (
    <Card className="p-6">
      <div className="flex items-center gap-3">
        <div className="flex h-11 w-11 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary">
          <UserCircle2 size={20} />
        </div>
        <div>
          <p className="text-lg font-semibold text-slate-900 dark:text-white">Operator Profile</p>
          <p className="text-sm text-slate-600 dark:text-slate-400">View and verify your operator credentials.</p>
        </div>
      </div>

      <div className="mt-6 max-w-xl space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <span className="block text-xs text-slate-500 uppercase tracking-wider">Full Name</span>
            <span className="text-sm font-medium text-slate-900 dark:text-white">{name}</span>
          </div>
          <div>
            <span className="block text-xs text-slate-500 uppercase tracking-wider">Email Address</span>
            <span className="text-sm font-medium text-slate-900 dark:text-white">{email}</span>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4 pt-2">
          <div>
            <span className="block text-xs text-slate-500 uppercase tracking-wider">Assigned Role</span>
            <span className="text-sm font-medium text-slate-900 dark:text-white">{role}</span>
          </div>
          <div>
            <span className="block text-xs text-slate-500 uppercase tracking-wider">Workspace Status</span>
            <span className="inline-flex items-center gap-1.5 rounded-full border border-success/20 bg-success/10 px-2 py-0.5 text-xs text-success font-medium">
              <span className="h-1.5 w-1.5 rounded-full bg-success"></span>
              Active & Verified
            </span>
          </div>
        </div>

        <div className="mt-6 rounded-2xl border border-dashed border-white/10 bg-white/5 p-4 text-xs text-slate-400">
          Profile modification is disabled in local development mode. User credentials and roles are managed via secure database authentication.
        </div>
      </div>
    </Card>
  );
}

export default function SettingsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const activeSection = searchParams.get('section') || 'Appearance';

  return (
    <div className="space-y-6 py-4" data-tour="settings">
      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }} className="rounded-[28px] border border-slate-200 dark:border-white/10 bg-white/70 dark:bg-slate-900/70 p-6 shadow-soft backdrop-blur-xl">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <Badge>Platform settings</Badge>
            <h1 className="mt-4 text-3xl font-semibold text-slate-900 dark:text-white">Shape the operator experience</h1>
            <p className="mt-2 max-w-2xl text-slate-600 dark:text-slate-400">Fine-tune the product experience, diagnostics, and notification flow without changing the core monitoring engine.</p>
          </div>
        </div>
      </motion.div>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {sections.map((section) => {
          const Icon = section.icon;
          const isActive = activeSection.toLowerCase() === section.title.toLowerCase();
          return (
            <button
              type="button"
              key={section.title}
              onClick={() => setSearchParams({ section: section.title })}
              className={`text-left rounded-2xl border p-5 transition hover:-translate-y-1 hover:border-primary/30 flex flex-col justify-between h-full w-full ${
                isActive 
                  ? 'border-primary bg-primary/10 ring-1 ring-primary' 
                  : 'border-slate-200 dark:border-white/10 bg-white/70 dark:bg-slate-900/70'
              }`}
            >
              <div className="flex items-center gap-2 text-primary">
                <Icon size={16} />
                <p className="text-sm font-medium text-slate-900 dark:text-white">{section.title}</p>
              </div>
              <p className="mt-3 text-sm text-slate-500 dark:text-slate-400">{section.description}</p>
            </button>
          );
        })}
      </div>

      <div className="mt-6">
        <AnimatePresence mode="wait">
          <motion.div
            key={activeSection}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.15 }}
          >
            {activeSection.toLowerCase() === 'profile' && <ProfilePanel />}
            {activeSection.toLowerCase() === 'appearance' && <AppearancePanel />}
            {activeSection.toLowerCase() === 'notifications' && <NotificationPreferencesPanel />}
            {activeSection.toLowerCase() === 'ai providers' && <AiProviderPanel />}
            {activeSection.toLowerCase() === 'security' && <SecurityPanel />}
            {activeSection.toLowerCase() === 'diagnostics' && <SystemDiagnosticsPanel />}
            {activeSection.toLowerCase() === 'about argus' && <AboutArgusPanel />}
          </motion.div>
        </AnimatePresence>
      </div>
    </div>
  );
}
