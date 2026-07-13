import { motion } from 'framer-motion';
import { ArrowRight, BarChart3, Bot, ShieldCheck, Sparkles, Workflow } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Section } from '../components/ui/Section';
import { Badge } from '../components/ui/Badge';

const featureCards = [
  {
    title: 'Monitoring',
    description: 'Track uptime, latency, and edge health from a single place with calm, reliable visibility.',
    icon: BarChart3,
  },
  {
    title: 'Incident Intelligence',
    description: 'Correlate evidence, confidence, and recommendations into a story your team can trust.',
    icon: ShieldCheck,
  },
  {
    title: 'Optional AI Narratives',
    description: 'Pair deterministic analysis with richer summaries when a provider is enabled.',
    icon: Bot,
  },
];

export default function LandingPage() {
  return (
    <div className="pb-16">
      <section className="relative overflow-hidden rounded-[32px] border border-white/10 bg-slate-900/70 px-6 py-16 shadow-soft backdrop-blur-xl sm:px-8 lg:px-12">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,_rgba(79,140,255,0.24),_transparent_32%)]" />
        <div className="relative grid items-center gap-10 lg:grid-cols-[1.15fr_0.85fr]">
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.45 }}>
            <Badge>Premium monitoring platform</Badge>
            <h1 className="mt-6 text-4xl font-semibold tracking-tight text-white sm:text-5xl lg:text-6xl">
              Trust the signal. Move faster when it matters.
            </h1>
            <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-400">
              ARGUS turns infrastructure health, incident intelligence, and operational context into a calm, polished experience for modern teams.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/demo">
                <Button className="px-6 py-3">
                  Try Live Demo <ArrowRight size={16} className="ml-2" />
                </Button>
              </Link>
              <Link to="/signin">
                <Button variant="secondary" className="px-6 py-3">
                  Sign In
                </Button>
              </Link>
              <Link to="/demo?present=true">
                <Button variant="secondary" className="px-6 py-3 border-primary/30 text-primary hover:bg-primary/5">
                  Present ARGUS
                </Button>
              </Link>
            </div>
          </motion.div>

          <motion.div initial={{ opacity: 0, x: 16 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.5 }}>
            <Card className="p-6 sm:p-8">
              <div className="rounded-2xl border border-white/10 bg-slate-950/80 p-5">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-slate-400">Global health</p>
                    <p className="mt-2 text-4xl font-semibold text-white">99.95%</p>
                  </div>
                  <div className="rounded-full border border-success/20 bg-success/10 px-3 py-1 text-sm text-success">
                    Healthy
                  </div>
                </div>
                <div className="mt-8 grid gap-4 sm:grid-cols-2">
                  <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <p className="text-sm text-slate-400">Active incidents</p>
                    <p className="mt-2 text-2xl font-semibold text-white">1</p>
                  </div>
                  <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <p className="text-sm text-slate-400">Response time</p>
                    <p className="mt-2 text-2xl font-semibold text-white">82ms</p>
                  </div>
                </div>
              </div>
            </Card>
          </motion.div>
        </div>
      </section>

      <Section eyebrow="Experience" title="A premium product story from first glance" description="Every interaction is designed to feel calm, deliberate, and enterprise-ready.">
        <div className="grid gap-5 md:grid-cols-3">
          {featureCards.map((feature, index) => {
            const Icon = feature.icon;
            return (
              <motion.div key={feature.title} initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3, delay: index * 0.08 }}>
                <Card className="p-6">
                  <div className="flex h-11 w-11 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary">
                    <Icon size={20} />
                  </div>
                  <h3 className="mt-5 text-xl font-semibold text-white">{feature.title}</h3>
                  <p className="mt-3 text-sm leading-7 text-slate-400">{feature.description}</p>
                </Card>
              </motion.div>
            );
          })}
        </div>
      </Section>

      <Section eyebrow="Architecture" title="Designed for real operational workflows" description="The experience balances polished storytelling with the rigor of incident response and analysis.">
        <div className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr]">
          <Card className="p-6">
            <div className="flex items-center gap-3">
              <div className="rounded-full border border-accent/20 bg-accent/10 p-2 text-accent">
                <Workflow size={18} />
              </div>
              <div>
                <p className="text-sm font-semibold text-white">Operational architecture</p>
                <p className="text-sm text-slate-400">A single, coherent story for monitoring and response.</p>
              </div>
            </div>
            <div className="mt-6 space-y-4 text-sm text-slate-400">
              <p>• Edge telemetry flows into a modern observability layer.</p>
              <p>• Evidence and confidence are combined into actionable recommendations.</p>
              <p>• Incident intelligence surfaces the most relevant historical patterns.</p>
            </div>
          </Card>
          <Card className="p-6">
            <div className="rounded-2xl border border-white/10 bg-slate-950/70 p-5">
              <div className="flex items-center justify-between">
                <p className="text-sm text-slate-400">Live signal quality</p>
                <Badge>AI-ready</Badge>
              </div>
              <div className="mt-6 grid gap-4 sm:grid-cols-3">
                {['Gateway', 'Auth', 'Payments'].map((name) => (
                  <div key={name} className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <p className="text-sm text-slate-400">{name}</p>
                    <p className="mt-2 text-xl font-semibold text-white">Stable</p>
                  </div>
                ))}
              </div>
            </div>
          </Card>
        </div>
      </Section>
    </div>
  );
}
