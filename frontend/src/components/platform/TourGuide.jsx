import { AnimatePresence, motion } from 'framer-motion';
import { ArrowLeft, ArrowRight, CheckCircle2, Sparkles, X } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { usePlatformPreferences } from '../../context/PlatformPreferencesContext';
import { Button } from '../ui/Button';

const steps = [
  { title: 'Dashboard', path: '/demo', target: 'dashboard', description: 'Start with the operational overview and the signals that need attention.' },
  { title: 'Monitors', path: '/demo', target: 'monitors', description: 'Pin critical monitors so they stay first in the dashboard, search, and command palette.' },
  { title: 'Analytics', path: '/demo/analytics', target: 'analytics', description: 'Review trends, response behavior, and operational history.' },
  { title: 'Incident Intelligence', path: '/demo/incident', target: 'incident-intelligence', description: 'Use deterministic evidence, confidence, and recommendations during response.' },
  { title: 'AI Providers', path: '/demo/settings', target: 'ai-providers', description: 'Connect optional AI explanations while deterministic analysis remains unchanged.' },
  { title: 'Settings', path: '/demo/settings', target: 'settings', description: 'Tune appearance, notifications, diagnostics, security, and tour replay.' },
];

export function TourGuide() {
  const navigate = useNavigate();
  const location = useLocation();
  const { preferences, setTourOpen, setTourStep, completeTour, skipTour, dismissTour } = usePlatformPreferences();
  const currentStep = preferences.tourStep ?? -1;
  const [highlight, setHighlight] = useState(null);
  const step = currentStep >= 0 && currentStep < steps.length ? steps[currentStep] : null;
  const progressLabel = useMemo(() => `${Math.min(currentStep + 1, steps.length)} / ${steps.length}`, [currentStep]);

  useEffect(() => {
    if (!preferences.tourOpen || !step || location.pathname === step.path) {
      return;
    }
    navigate(step.path);
  }, [location.pathname, navigate, preferences.tourOpen, step]);

  useEffect(() => {
    if (!preferences.tourOpen || !step) {
      setHighlight(null);
      return undefined;
    }

    let retries = 0;
    let timerId = null;

    const updateHighlight = () => {
      const element = document.querySelector(`[data-tour="${step.target}"]`);
      if (!element) {
        if (retries < 30) {
          retries++;
          timerId = window.setTimeout(updateHighlight, 100);
        } else {
          setHighlight(null);
        }
        return;
      }
      const rect = element.getBoundingClientRect();
      setHighlight({
        top: Math.max(12, rect.top - 8),
        left: Math.max(12, rect.left - 8),
        width: rect.width + 16,
        height: rect.height + 16,
      });
      element.scrollIntoView({ behavior: preferences.reducedMotion ? 'auto' : 'smooth', block: 'center' });
    };

    timerId = window.setTimeout(updateHighlight, 100);
    window.addEventListener('resize', updateHighlight);
    return () => {
      if (timerId) window.clearTimeout(timerId);
      window.removeEventListener('resize', updateHighlight);
    };
  }, [location.pathname, preferences.reducedMotion, preferences.tourOpen, step]);

  if (!preferences.tourOpen) {
    return null;
  }

  const next = () => setTourStep(currentStep + 1);
  const previous = () => setTourStep(Math.max(0, currentStep - 1));
  const showBlur = currentStep < 0;

  return (
    <AnimatePresence>
      <motion.div 
        initial={{ opacity: 0 }} 
        animate={{ opacity: 1 }} 
        exit={{ opacity: 0 }} 
        className={`fixed inset-0 z-[60] px-4 transition-all duration-200 ${
          highlight 
            ? 'pointer-events-none bg-slate-950/20' 
            : `bg-slate-950/60 ${showBlur ? 'backdrop-blur-[2px]' : ''} pointer-events-auto`
        }`}
      >
        {highlight ? (
          <motion.div
            aria-hidden="true"
            className="pointer-events-none fixed rounded-[28px] border-2 border-primary shadow-[0_0_0_9999px_rgba(2,6,23,0.5),0_0_40px_rgba(79,140,255,0.35)]"
            animate={highlight}
            transition={{ duration: preferences.reducedMotion ? 0 : 0.2 }}
          />
        ) : null}

        <div className="flex min-h-screen items-center justify-center pointer-events-none">
          <motion.div 
            initial={{ opacity: 0, y: 10, scale: 0.98 }} 
            animate={{ opacity: 1, y: 0, scale: 1 }} 
            exit={{ opacity: 0, y: 10, scale: 0.98 }} 
            className="relative w-full max-w-xl rounded-[28px] border border-slate-200 dark:border-white/10 bg-white/95 dark:bg-slate-900/95 p-6 shadow-2xl pointer-events-auto"
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="text-sm text-slate-400">{currentStep < 0 ? 'Welcome' : 'Product tour'}</p>
                <h2 className="mt-1 text-2xl font-semibold text-white">{currentStep < 0 ? 'Welcome to ARGUS' : currentStep >= steps.length ? 'Congratulations' : step?.title}</h2>
              </div>
              <button type="button" onClick={skipTour} className="rounded-full border border-white/10 bg-white/5 p-2 text-slate-400" aria-label="Skip tour">
                <X size={16} />
              </button>
            </div>

            <div className="mt-6 rounded-2xl border border-white/10 bg-slate-950/70 p-5">
              <p className="text-sm leading-7 text-slate-400">
                {currentStep < 0
                  ? 'Take a quick guided tour through the core ARGUS operator workflow. Experienced users can skip it and keep working.'
                  : currentStep >= steps.length ? 'You are ready to operate from the ARGUS dashboard.' : step?.description}
              </p>
            </div>

            <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2 text-sm text-slate-400">
                <Sparkles size={16} className="text-primary" />
                <span>{currentStep < 0 ? 'First launch' : progressLabel}</span>
              </div>

              {currentStep < 0 ? (
                <div className="flex flex-wrap items-center gap-2">
                  <Button variant="ghost" onClick={dismissTour}>Don't show again</Button>
                  <Button variant="secondary" onClick={skipTour}>Skip</Button>
                  <Button variant="primary" onClick={() => setTourStep(0)}>
                    Start tour
                    <ArrowRight size={16} className="ml-2" />
                  </Button>
                </div>
              ) : currentStep >= steps.length ? (
                <div className="flex items-center gap-2">
                  <Button variant="primary" onClick={() => { completeTour(); navigate('/demo'); }}>
                    <CheckCircle2 size={16} className="mr-2" />
                    Go To Dashboard
                  </Button>
                </div>
              ) : (
                <div className="flex flex-wrap items-center gap-2">
                  <Button variant="ghost" onClick={skipTour}>Skip</Button>
                  <Button variant="ghost" onClick={previous} disabled={currentStep === 0}>
                    <ArrowLeft size={16} className="mr-2" />
                    Previous
                  </Button>
                  <Button variant="secondary" onClick={currentStep === steps.length - 1 ? () => setTourStep(steps.length) : next}>
                    {currentStep === steps.length - 1 ? 'Finish' : 'Next'}
                    <ArrowRight size={16} className="ml-2" />
                  </Button>
                </div>
              )}
            </div>

            {currentStep >= steps.length ? (
              <div className="mt-5 rounded-2xl border border-success/20 bg-success/10 p-4 text-sm text-success">
                Congratulations. Your ARGUS tour is complete.
              </div>
            ) : null}
          </motion.div>
        </div>
      </motion.div>
    </AnimatePresence>
  );
}
