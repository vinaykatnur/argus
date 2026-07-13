import { motion } from 'framer-motion';
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { usePlatformPreferences } from '../context/PlatformPreferencesContext';
import { useSearchParams } from 'react-router-dom';
import { demoMonitors } from '../data/demo';

const chartData = [
  { label: 'Jan', availability: 99.8, response: 118 },
  { label: 'Feb', availability: 99.9, response: 112 },
  { label: 'Mar', availability: 99.6, response: 124 },
  { label: 'Apr', availability: 99.95, response: 103 },
  { label: 'May', availability: 99.97, response: 96 },
  { label: 'Jun', availability: 99.95, response: 82 },
];

export default function AnalyticsPage() {
  const { preferences } = usePlatformPreferences();
  const isAnim = preferences.chartAnimation;
  const [searchParams] = useSearchParams();
  const monitorId = searchParams.get('monitorId');

  const selectedMonitor = monitorId
    ? demoMonitors.find((m) => String(m.id) === String(monitorId))
    : null;

  return (
    <div className="space-y-6 py-4" data-tour="analytics">
      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }} className="rounded-[28px] border border-slate-200 dark:border-white/10 bg-white/70 dark:bg-slate-900/70 p-6 shadow-soft backdrop-blur-xl">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <Badge>Analytics</Badge>
              {selectedMonitor ? (
                <Badge className="bg-primary/20 text-primary border-primary/30">
                  Target: {selectedMonitor.name}
                </Badge>
              ) : monitorId ? (
                <Badge className="bg-primary/20 text-primary border-primary/30">
                  Target ID: {monitorId}
                </Badge>
              ) : null}
            </div>
            <h1 className="mt-4 text-3xl font-semibold text-slate-900 dark:text-white">Operational trends</h1>
            <p className="mt-2 max-w-2xl text-slate-600 dark:text-slate-400">Professional, insightful views into availability, response time, and incident frequency.</p>
          </div>
          <div className="rounded-full border border-slate-200 dark:border-white/10 bg-slate-100 dark:bg-white/5 px-4 py-2 text-sm text-slate-700 dark:text-slate-300">Last 6 months</div>
        </div>
      </motion.div>

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-slate-600 dark:text-slate-400">Availability</p>
              <h2 className="mt-1 text-xl font-semibold text-slate-900 dark:text-white">Healthy trend line</h2>
            </div>
            <Badge>98.7%</Badge>
          </div>
          <div className="mt-6 h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="availability" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="var(--primary-color, #4F8CFF)" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="var(--primary-color, #4F8CFF)" stopOpacity={0.02} />
                  </linearGradient>
                </defs>
                <CartesianGrid stroke="rgba(128,128,128,0.1)" strokeDasharray="3 3" />
                <XAxis dataKey="label" stroke="#64748b" />
                <YAxis stroke="#64748b" />
                <Tooltip contentStyle={{ backgroundColor: 'var(--surface-color, #0f172a)', border: '1px solid rgba(128, 128, 128, 0.2)' }} />
                <Area type="monotone" dataKey="availability" stroke="var(--primary-color, #4F8CFF)" fill="url(#availability)" isAnimationActive={isAnim} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <div className="space-y-6">
          <Card className="p-6">
            <p className="text-sm text-slate-600 dark:text-slate-400">Response time</p>
            <h2 className="mt-1 text-xl font-semibold text-slate-900 dark:text-white">Steady improvement</h2>
            <div className="mt-6 h-48">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData}>
                  <CartesianGrid stroke="rgba(128,128,128,0.1)" strokeDasharray="3 3" />
                  <XAxis dataKey="label" stroke="#64748b" />
                  <YAxis stroke="#64748b" />
                  <Tooltip contentStyle={{ backgroundColor: 'var(--surface-color, #0f172a)', border: '1px solid rgba(128, 128, 128, 0.2)' }} />
                  <Area type="monotone" dataKey="response" stroke="#22C55E" fill="rgba(34,197,94,0.16)" isAnimationActive={isAnim} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </Card>

          <Card className="p-6">
            <p className="text-sm text-slate-600 dark:text-slate-400">Key highlights</p>
            <div className="mt-4 space-y-3 text-sm text-slate-600 dark:text-slate-400">
              <p>• Mean time between failures improved by 18%.</p>
              <p>• Incident frequency is down across the production footprint.</p>
              <p>• Response time stays comfortably below regional targets.</p>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
