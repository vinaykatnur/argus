import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { hasAuthToken } from '../lib/api';

const defaultPreferences = {
  theme: 'dark',
  accentColor: '#4F8CFF',
  density: 'comfortable',
  reducedMotion: false,
  chartAnimation: true,
  notifications: {
    browserNotifications: true,
    emailNotifications: true,
    incidentAlerts: true,
    dailySummary: false,
    maintenanceAlerts: false,
  },
  pinnedMonitors: [],
  recentlyVisited: [],
  tourCompleted: false,
  tourDismissed: false,
  tourStep: 0,
  tourOpen: false,
  isDemoMode: true,
};

const PlatformPreferencesContext = createContext(null);

function readStoredPreferences() {
  const hasToken = hasAuthToken();
  const base = { ...defaultPreferences, isDemoMode: !hasToken };
  if (typeof window === 'undefined') {
    return base;
  }

  try {
    const raw = window.localStorage.getItem('argus-platform-preferences');
    if (raw) {
      const parsed = JSON.parse(raw);
      // If we are logged in, we default to live mode unless they explicitly switched
      return { 
        ...base, 
        ...parsed, 
        isDemoMode: parsed.isDemoMode !== undefined ? parsed.isDemoMode : !hasToken 
      };
    }
    return base;
  } catch {
    return base;
  }
}

export function PlatformPreferencesProvider({ children }) {
  const [preferences, setPreferences] = useState(readStoredPreferences);
  const [initializedTour, setInitializedTour] = useState(false);

  useEffect(() => {
    window.localStorage.setItem('argus-platform-preferences', JSON.stringify(preferences));
  }, [preferences]);

  useEffect(() => {
    const resolveTheme = () => {
      const theme = preferences.theme === 'system'
        ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
        : preferences.theme;

      document.documentElement.classList.toggle('dark', theme === 'dark');
      document.documentElement.dataset.theme = theme;
      document.documentElement.dataset.density = preferences.density;
      document.documentElement.dataset.reducedMotion = String(preferences.reducedMotion);
      document.documentElement.dataset.chartAnimation = String(preferences.chartAnimation);
      document.documentElement.style.setProperty('--accent-color', preferences.accentColor);
      document.documentElement.style.setProperty('--primary-color', preferences.accentColor);
      document.documentElement.style.setProperty('--surface-color', theme === 'dark' ? '#060b17' : '#f8fafc');
    };

    resolveTheme();

    const media = window.matchMedia('(prefers-color-scheme: dark)');
    media.addEventListener?.('change', resolveTheme);
    return () => media.removeEventListener?.('change', resolveTheme);
  }, [preferences.theme, preferences.accentColor, preferences.density, preferences.reducedMotion, preferences.chartAnimation]);

  useEffect(() => {
    if (!initializedTour && !preferences.tourCompleted && !preferences.tourDismissed && !preferences.tourOpen) {
      setInitializedTour(true);
      setPreferences((current) => ({ ...current, tourOpen: true, tourStep: -1 }));
    }
  }, [initializedTour, preferences.tourCompleted, preferences.tourDismissed, preferences.tourOpen]);

  const value = useMemo(() => ({
    preferences,
    setPreferences,
    togglePin: (monitorId) => {
      setPreferences((current) => {
        const pinnedMonitors = current.pinnedMonitors.includes(monitorId)
          ? current.pinnedMonitors.filter((item) => item !== monitorId)
          : [...current.pinnedMonitors, monitorId];

        return { ...current, pinnedMonitors };
      });
    },
    setTourOpen: (tourOpen) => setPreferences((current) => ({ ...current, tourOpen })),
    setTourStep: (tourStep) => setPreferences((current) => ({ ...current, tourStep })),
    skipTour: () => setPreferences((current) => ({ ...current, tourCompleted: true, tourOpen: false, tourStep: 0 })),
    dismissTour: () => setPreferences((current) => ({ ...current, tourDismissed: true, tourCompleted: true, tourOpen: false, tourStep: 0 })),
    completeTour: () => setPreferences((current) => ({ ...current, tourCompleted: true, tourOpen: false, tourStep: 0 })),
    replayTour: () => setPreferences((current) => ({ ...current, tourCompleted: false, tourDismissed: false, tourOpen: true, tourStep: -1 })),
    addRecentlyVisited: (entry) => setPreferences((current) => {
      const next = [
        entry,
        ...(current.recentlyVisited || []).filter((item) => item.to !== entry.to),
      ].slice(0, 6);
      return { ...current, recentlyVisited: next };
    }),
  }), [preferences]);

  return (
    <PlatformPreferencesContext.Provider value={value}>
      {children}
    </PlatformPreferencesContext.Provider>
  );
}

export function usePlatformPreferences() {
  const context = useContext(PlatformPreferencesContext);
  if (!context) {
    throw new Error('usePlatformPreferences must be used within PlatformPreferencesProvider');
  }
  return context;
}
