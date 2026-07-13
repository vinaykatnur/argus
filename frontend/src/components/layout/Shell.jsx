import { motion } from 'framer-motion';
import { Command, Menu, ShieldCheck, Sparkles, SunMoon } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { usePlatformPreferences } from '../../context/PlatformPreferencesContext';
import { CommandPalette } from '../platform/CommandPalette';
import { TourGuide } from '../platform/TourGuide';
import { PresentationOverlay } from '../platform/PresentationOverlay';
import { Button } from '../ui/Button';
import { hasAuthToken } from '../../lib/api';

export function Shell({ children, showNav = true }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [paletteOpen, setPaletteOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { preferences, setPreferences, addRecentlyVisited } = usePlatformPreferences();

  const isPresenting = searchParams.get('present') === 'true';

  const handleExitPresent = () => {
    const next = new URLSearchParams(searchParams);
    next.delete('present');
    setSearchParams(next);
  };

  const isDemoRoute = location.pathname.startsWith('/demo');
  const loggedIn = hasAuthToken();

  const navItems = isDemoRoute
    ? [
        { label: 'Overview', to: '/demo' },
        { label: 'Incident Intelligence', to: '/demo/incident' },
        { label: 'Analytics', to: '/demo/analytics' },
        { label: 'Settings', to: '/demo/settings' },
      ]
    : [
        { label: 'Overview', to: '/dashboard' },
        { label: 'Incident Intelligence', to: '/incident' },
        { label: 'Analytics', to: '/analytics' },
        { label: 'Settings', to: '/settings' },
      ];

  useEffect(() => {
    const onKeyDown = (event) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault();
        setPaletteOpen((current) => !current);
      }

      if (event.key === 'Escape') {
        setPaletteOpen(false);
      }
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, []);

  useEffect(() => {
    const activeItem = navItems.find((item) => item.to === location.pathname);
    if (activeItem) {
      addRecentlyVisited({ label: activeItem.label, description: 'Recently opened page', to: activeItem.to });
    }
  }, [location.pathname, isDemoRoute]);

  const cycleTheme = () => {
    const order = ['dark', 'light', 'system'];
    const index = order.indexOf(preferences.theme);
    setPreferences((current) => ({ ...current, theme: order[(index + 1) % order.length] }));
  };

  const handleDemoToggle = () => {
    if (isDemoRoute) {
      if (loggedIn) {
        setPreferences((curr) => ({ ...curr, isDemoMode: false }));
        navigate('/dashboard');
      } else {
        navigate('/signin');
      }
    } else {
      setPreferences((curr) => ({ ...curr, isDemoMode: true }));
      navigate('/demo');
    }
  };

  return (
    <div className="argus-shell min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(79,140,255,0.08),_transparent_35%),linear-gradient(135deg,_#f1f5f9_0%,_#e2e8f0_45%,_#f1f5f9_100%)] dark:bg-[radial-gradient(circle_at_top_left,_rgba(79,140,255,0.18),_transparent_35%),linear-gradient(135deg,_#050816_0%,_#0b1120_45%,_#050816_100%)] text-slate-900 dark:text-slate-100 transition-colors duration-300">
      <div className="mx-auto flex min-h-screen max-w-7xl flex-col px-4 py-4 sm:px-6 lg:px-8">
        <motion.header
          initial={{ opacity: 0, y: -8 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-4 flex flex-col rounded-3xl border border-slate-200 dark:border-white/10 bg-white/70 dark:bg-slate-900/70 px-4 py-3 shadow-soft backdrop-blur-xl transition-all duration-300"
        >
          <div className="flex w-full items-center justify-between">
            <Link to="/" className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-full border border-primary/30 bg-primary/10 text-primary">
                <ShieldCheck size={18} />
              </div>
              <div>
                <p className="text-sm font-semibold tracking-[0.28em] text-slate-900 dark:text-white">ARGUS</p>
                <p className="text-xs text-slate-500 dark:text-slate-400">Operational intelligence</p>
              </div>
            </Link>

            {showNav ? (
              <nav className="hidden items-center gap-2 md:flex">
                {navItems.map((item) => (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    className={({ isActive }) =>
                      `rounded-full px-3 py-2 text-sm transition ${isActive ? 'bg-slate-200/50 dark:bg-white/10 text-slate-900 dark:text-white font-medium' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-200/30 dark:hover:bg-white/5 hover:text-slate-900 dark:hover:text-white'}`
                    }
                  >
                    {item.label}
                  </NavLink>
                ))}
              </nav>
            ) : null}

            <div className="flex items-center gap-2">
              <Button variant="ghost" className="hidden sm:flex" onClick={cycleTheme}>
                <SunMoon size={16} className="mr-2" />
                {preferences.theme === 'system' ? 'System' : preferences.theme === 'light' ? 'Light' : 'Dark'}
              </Button>
              
              {loggedIn && !isDemoRoute ? (
                <div className="hidden items-center gap-2 rounded-full border border-success/20 bg-success/10 px-3 py-1.5 text-xs text-success sm:flex">
                  <span className="relative flex h-2 w-2">
                    <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-success opacity-75"></span>
                    <span className="relative inline-flex h-2 w-2 rounded-full bg-success"></span>
                  </span>
                  Live Workspace
                </div>
              ) : null}

              <Button variant="secondary" className="hidden sm:flex" onClick={handleDemoToggle}>
                <Sparkles size={16} className="mr-2 text-primary" />
                {isDemoRoute ? (loggedIn ? 'Return to Live' : 'Sign In') : 'Demo Mode'}
              </Button>
              
              <button
                type="button"
                onClick={() => setPaletteOpen(true)}
                className="hidden items-center gap-2 rounded-full border border-slate-200 dark:border-white/10 bg-slate-100 dark:bg-white/5 px-3 py-2 text-sm text-slate-600 dark:text-slate-300 sm:flex"
              >
                <Command size={16} />
                <span>Search</span>
                <span className="rounded border border-slate-200 dark:border-white/10 px-1.5 py-0.5 text-[10px] uppercase tracking-[0.24em] text-slate-400 dark:text-slate-500">Ctrl K</span>
              </button>
              
              <button 
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="rounded-full border border-slate-200 dark:border-white/10 bg-slate-100 dark:bg-white/5 p-2 text-slate-600 dark:text-slate-300 md:hidden" 
                aria-label="Open navigation"
              >
                <Menu size={16} />
              </button>
            </div>
          </div>

          {/* MOBILE NAV SLIDEOUT */}
          {mobileMenuOpen && showNav ? (
            <div className="mt-3 flex flex-col gap-1 border-t border-slate-100 dark:border-white/5 pt-3 md:hidden">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={() => setMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    `rounded-2xl px-4 py-2.5 text-sm transition ${isActive ? 'bg-slate-200/50 dark:bg-white/10 text-slate-900 dark:text-white font-medium' : 'text-slate-600 dark:text-slate-400 hover:bg-slate-200/30 dark:hover:bg-white/5'}`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
              <div className="flex items-center justify-between border-t border-slate-100 dark:border-white/5 mt-2 pt-2">
                <Button variant="ghost" className="py-2.5" onClick={cycleTheme}>
                  <SunMoon size={16} className="mr-2" />
                  Theme: {preferences.theme}
                </Button>
                <Button variant="secondary" className="py-2.5" onClick={() => { setMobileMenuOpen(false); handleDemoToggle(); }}>
                  <Sparkles size={16} className="mr-2 text-primary" />
                  {isDemoRoute ? (loggedIn ? 'Return to Live' : 'Sign In') : 'Demo Mode'}
                </Button>
              </div>
            </div>
          ) : null}
        </motion.header>

        <main className="flex-1">{children}</main>
      </div>

      <CommandPalette open={paletteOpen} onClose={() => setPaletteOpen(false)} />
      <TourGuide />
      {isPresenting && <PresentationOverlay onClose={handleExitPresent} />}
    </div>
  );
}
