import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowLeft, ArrowRight, X, Play, ShieldCheck, Sparkles, AlertTriangle, Workflow, BrainCircuit } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { Card } from '../ui/Card';
import { usePlatformPreferences } from '../../context/PlatformPreferencesContext';
import { hasAuthToken } from '../../lib/api';

export function PresentationOverlay({ onClose }) {
  const navigate = useNavigate();
  const { preferences } = usePlatformPreferences();
  const [currentScene, setCurrentScene] = useState(0);
  const loggedIn = hasAuthToken();

  const totalScenes = 7;

  // Key navigation handlers
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose();
      } else if (event.key === 'ArrowRight') {
        handleNext();
      } else if (event.key === 'ArrowLeft') {
        handlePrev();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [currentScene]);

  const handleNext = () => {
    if (currentScene < totalScenes - 1) {
      setCurrentScene(currentScene + 1);
    }
  };

  const handlePrev = () => {
    if (currentScene > 0) {
      setCurrentScene(currentScene - 1);
    }
  };

  // Check reduced motion settings
  const isReduced = preferences.reducedMotion;

  const slideVariants = {
    enter: (direction) => ({
      x: isReduced ? 0 : direction > 0 ? 100 : -100,
      opacity: 0,
    }),
    center: {
      x: 0,
      opacity: 1,
    },
    exit: (direction) => ({
      x: isReduced ? 0 : direction < 0 ? 100 : -100,
      opacity: 0,
    }),
  };

  const [[page, direction], setPage] = useState([0, 0]);

  const paginate = (newDirection) => {
    const nextScene = currentScene + newDirection;
    if (nextScene >= 0 && nextScene < totalScenes) {
      setPage([nextScene, newDirection]);
      setCurrentScene(nextScene);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex flex-col bg-slate-950 text-white select-none">
      {/* Background Mesh Gradient */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_70%_20%,_rgba(79,140,255,0.15),_transparent_45%)]" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_80%,_rgba(139,92,246,0.1),_transparent_40%)]" />
      </div>

      {/* Top Header */}
      <header className="relative flex items-center justify-between px-6 py-4 border-b border-white/5 bg-slate-950/40 backdrop-blur-md">
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-full border border-primary/30 bg-primary/10 text-primary">
            <ShieldCheck size={16} />
          </div>
          <div>
            <p className="text-xs font-semibold tracking-[0.2em] text-white">ARGUS</p>
            <p className="text-[10px] text-slate-500">Presentation Console</p>
          </div>
        </div>

        <button 
          type="button" 
          onClick={onClose}
          className="flex items-center gap-1.5 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs text-slate-400 hover:text-white transition"
          aria-label="Exit Presentation"
        >
          <span>Exit</span>
          <X size={14} />
        </button>
      </header>

      {/* Slide Container */}
      <main className="flex-1 relative flex items-center justify-center px-6 py-8 overflow-y-auto">
        <AnimatePresence initial={false} custom={direction} mode="wait">
          <motion.div
            key={currentScene}
            custom={direction}
            variants={slideVariants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: isReduced ? 0 : 0.3, ease: 'easeInOut' }}
            className="w-full max-w-4xl grid gap-8 lg:grid-cols-2 items-center"
          >
            {/* Left Content Column */}
            <div className="space-y-6">
              <Badge>Scene {currentScene + 1} of {totalScenes}</Badge>
              
              {currentScene === 0 && (
                <div className="space-y-4">
                  <h2 className="text-4xl sm:text-5xl font-semibold tracking-tight text-white leading-tight">
                    Deterministic Incident Intelligence.
                  </h2>
                  <p className="text-lg text-slate-400 leading-relaxed">
                    ARGUS is a flagship incident monitoring platform built to replace guesswork with clarity. We synthesize telemetry signals into trustworthy operational insights.
                  </p>
                </div>
              )}

              {currentScene === 1 && (
                <div className="space-y-4">
                  <h2 className="text-3xl sm:text-4xl font-semibold tracking-tight text-white">
                    The Cost of Chaos.
                  </h2>
                  <p className="text-lg text-slate-400 leading-relaxed">
                    Traditional monitoring tools alert you *that* a service failed, but raw graphs and noisy channels leave your team in the dark. Finding *why* it failed consumes precious operational time.
                  </p>
                </div>
              )}

              {currentScene === 2 && (
                <div className="space-y-4">
                  <h2 className="text-3xl sm:text-4xl font-semibold tracking-tight text-white">
                    Calm, Unified Workspace.
                  </h2>
                  <p className="text-lg text-slate-400 leading-relaxed">
                    Operators work from a single dashboard showing real-time latency distributions, edge availability, and system metrics. Built-in pins keep your critical path services front and center.
                  </p>
                </div>
              )}

              {currentScene === 3 && (
                <div className="space-y-4">
                  <h2 className="text-3xl sm:text-4xl font-semibold tracking-tight text-white">
                    The Deterministic Pipeline.
                  </h2>
                  <p className="text-lg text-slate-400 leading-relaxed">
                    We run telemetry through a strict, deterministic incident intelligence flow. No speculative algorithms or black-box predictions—every summary is derived directly from empirical, structured rules.
                  </p>
                </div>
              )}

              {currentScene === 4 && (
                <div className="space-y-4">
                  <h2 className="text-3xl sm:text-4xl font-semibold tracking-tight text-white">
                    Explainable Analysis.
                  </h2>
                  <p className="text-lg text-slate-400 leading-relaxed">
                    ARGUS is built on complete transparency. We expose the exact evidence, confidence metrics, and historical similarity correlations so your response team understands *why* a suggestion was generated.
                  </p>
                </div>
              )}

              {currentScene === 5 && (
                <div className="space-y-4">
                  <h2 className="text-3xl sm:text-4xl font-semibold tracking-tight text-white">
                    Robust Core, Optional AI.
                  </h2>
                  <p className="text-lg text-slate-400 leading-relaxed">
                    The core engine remains completely independent and reliable. Optional AI providers can be connected to generate narrative summaries, but an AI failure will never break or compromise the underlying deterministic engine.
                  </p>
                </div>
              )}

              {currentScene === 6 && (
                <div className="space-y-4">
                  <h2 className="text-3xl sm:text-4xl font-semibold tracking-tight text-white">
                    Step Into ARGUS.
                  </h2>
                  <p className="text-lg text-slate-400 leading-relaxed">
                    The presentation is complete. Go ahead and explore the workspace Console to see the live metrics and deterministic intelligence reports.
                  </p>
                </div>
              )}
            </div>

            {/* Right Visual Graphic Column */}
            <div className="flex items-center justify-center">
              <Card className="w-full border-white/5 bg-slate-900/40 p-6 backdrop-blur-md min-h-[300px] flex items-center justify-center">
                {currentScene === 0 && (
                  <div className="text-center space-y-4">
                    <ShieldCheck size={72} className="text-primary mx-auto animate-pulse" />
                    <p className="text-sm uppercase tracking-[0.2em] text-slate-500">Know Before It Breaks</p>
                  </div>
                )}

                {currentScene === 1 && (
                  <div className="w-full space-y-4">
                    <div className="rounded-xl border border-danger/20 bg-danger/5 p-4 flex gap-3 items-center">
                      <AlertTriangle className="text-danger shrink-0 animate-bounce" size={20} />
                      <div className="text-left">
                        <p className="text-xs font-semibold uppercase text-danger">Alert storm</p>
                        <p className="text-sm text-slate-400">14 alerts active across 3 datacenters</p>
                      </div>
                    </div>
                    <div className="rounded-xl border border-warning/20 bg-warning/5 p-4 flex gap-3 items-center opacity-85">
                      <AlertTriangle className="text-warning shrink-0" size={20} />
                      <div className="text-left">
                        <p className="text-xs font-semibold uppercase text-warning">Metrics drift</p>
                        <p className="text-sm text-slate-400">Database queue saturation climbing</p>
                      </div>
                    </div>
                  </div>
                )}

                {currentScene === 2 && (
                  <div className="w-full grid gap-4 grid-cols-2">
                    {['API Gateway', 'Authentication', 'Inventory', 'Payments'].map((name, i) => (
                      <div key={name} className="rounded-xl border border-white/10 bg-slate-950/60 p-4 text-left">
                        <div className="flex items-center gap-1.5">
                          <span className={`h-2 w-2 rounded-full ${i === 2 ? 'bg-danger animate-ping' : i === 1 ? 'bg-warning' : 'bg-success'}`}></span>
                          <span className="text-xs font-medium text-white">{name}</span>
                        </div>
                        <p className="mt-2 text-xl font-semibold">{i === 2 ? '312ms' : i === 1 ? '141ms' : '67ms'}</p>
                      </div>
                    ))}
                  </div>
                )}

                {currentScene === 3 && (
                  <div className="w-full flex flex-col gap-2.5 text-xs font-medium">
                    {[
                      { step: 'Incident Triggered', detail: 'Edge telemetry monitors failure' },
                      { step: 'Evidence Extraction', detail: 'Isolate error ratios and logs' },
                      { step: 'Historical Correlation', detail: 'Map signature against past runs' },
                      { step: 'Deterministic Logic', detail: 'Evaluate patterns and confidence' },
                      { step: 'Actionable Advice', detail: 'Generate operational playbooks' },
                    ].map((item, index) => (
                      <div key={item.step} className="flex items-center gap-3 rounded-lg border border-white/5 bg-slate-950/50 p-2.5 text-left">
                        <div className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-primary/20 text-primary font-bold">
                          {index + 1}
                        </div>
                        <div>
                          <p className="font-semibold text-white">{item.step}</p>
                          <p className="text-[10px] text-slate-400">{item.detail}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {currentScene === 4 && (
                  <div className="w-full space-y-4">
                    <div className="rounded-xl border border-primary/20 bg-primary/5 p-4 text-left">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold text-primary uppercase">Evidence Profile</span>
                        <span className="text-xs font-bold text-white bg-primary/25 px-2 py-0.5 rounded-full">92% Match</span>
                      </div>
                      <p className="mt-2 text-sm text-slate-300 leading-relaxed">
                        API Gateway errors increased 3x. Latency matches signature INC-2048 from June 24.
                      </p>
                    </div>
                  </div>
                )}

                {currentScene === 5 && (
                  <div className="w-full space-y-4 text-left">
                    <div className="rounded-xl border border-success/20 bg-success/5 p-4">
                      <div className="flex items-center gap-2 text-success">
                        <Workflow size={16} />
                        <span className="text-xs font-semibold uppercase">Deterministic engine</span>
                      </div>
                      <p className="mt-2 text-xs text-slate-300 leading-relaxed">
                        Calculates exact confidence intervals, checks strict thresholds, and maps exact evidence. Runs locally, instantly, and with 100% predictability.
                      </p>
                    </div>
                    <div className="rounded-xl border border-primary/20 bg-primary/5 p-4">
                      <div className="flex items-center gap-2 text-primary">
                        <BrainCircuit size={16} />
                        <span className="text-xs font-semibold uppercase">Optional AI Enrichment</span>
                      </div>
                      <p className="mt-2 text-xs text-slate-300 leading-relaxed">
                        Reads structured results and creates a plain-language briefing for the operator. If connection fails, the core dashboard operations remain unaffected.
                      </p>
                    </div>
                  </div>
                )}

                {currentScene === 6 && (
                  <div className="flex flex-col gap-3 w-full max-w-xs">
                    <Button 
                      variant="primary" 
                      onClick={() => { onClose(); navigate('/demo'); }}
                      className="py-3 text-sm font-semibold"
                    >
                      Explore Demo Workspace
                    </Button>
                    
                    {loggedIn ? (
                      <Button 
                        variant="secondary" 
                        onClick={() => { onClose(); navigate('/dashboard'); }}
                        className="py-3 text-sm font-semibold border-white/10"
                      >
                        Enter Live Workspace
                      </Button>
                    ) : null}
                  </div>
                )}
              </Card>
            </div>
          </motion.div>
        </AnimatePresence>
      </main>

      {/* Bottom Footer Controls */}
      <footer className="relative flex items-center justify-between px-6 py-5 border-t border-white/5 bg-slate-950/40 backdrop-blur-md">
        <div className="flex items-center gap-1.5">
          {Array.from({ length: totalScenes }).map((_, index) => (
            <button
              type="button"
              key={index}
              onClick={() => {
                setPage([index, index > currentScene ? 1 : -1]);
                setCurrentScene(index);
              }}
              className={`h-2 rounded-full transition-all duration-300 ${
                index === currentScene 
                  ? 'w-6 bg-primary' 
                  : 'w-2 bg-slate-700 hover:bg-slate-500'
              }`}
              aria-label={`Go to slide ${index + 1}`}
            />
          ))}
        </div>

        <div className="flex gap-2">
          <Button
            variant="ghost"
            onClick={() => paginate(-1)}
            disabled={currentScene === 0}
            className="px-4 py-2 hover:bg-white/5 border border-transparent disabled:opacity-35"
          >
            <ArrowLeft size={16} className="mr-1.5" />
            Previous
          </Button>

          <Button
            variant="secondary"
            onClick={currentScene === totalScenes - 1 ? onClose : () => paginate(1)}
            className="px-4 py-2 border-white/10"
          >
            {currentScene === totalScenes - 1 ? 'Finish' : 'Next'}
            <ArrowRight size={16} className="ml-1.5" />
          </Button>
        </div>
      </footer>
    </div>
  );
}
