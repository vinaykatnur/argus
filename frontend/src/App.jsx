import { lazy, Suspense } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { Shell } from './components/layout/Shell';
import { hasAuthToken } from './lib/api';

const LandingPage = lazy(() => import('./pages/LandingPage'));
const SignInPage = lazy(() => import('./pages/SignInPage'));
const SignUpPage = lazy(() => import('./pages/SignUpPage'));
const VerifyEmailPage = lazy(() => import('./pages/VerifyEmailPage'));
const DemoDashboard = lazy(() => import('./pages/DemoDashboard'));
const IncidentPage = lazy(() => import('./pages/IncidentPage'));
const AnalyticsPage = lazy(() => import('./pages/AnalyticsPage'));
const SettingsPage = lazy(() => import('./pages/SettingsPage'));
const DiagnosticsPage = lazy(() => import('./pages/DiagnosticsPage'));

function RequireAuth({ children }) {
  return hasAuthToken() ? children : <Navigate to="/signin" replace />;
}

export default function App() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-primary/20 border-t-primary" />
      </div>
    }>
      <Routes>
        {/* PUBLIC ROUTES */}
        <Route path="/" element={<Shell showNav={false}><LandingPage /></Shell>} />
        <Route path="/signin" element={<Shell showNav={false}><SignInPage /></Shell>} />
        <Route path="/signup" element={<Shell showNav={false}><SignUpPage /></Shell>} />
        <Route path="/verify-email" element={<Shell showNav={false}><VerifyEmailPage /></Shell>} />

        {/* DEMO WORKSPACE ROUTES */}
        <Route path="/demo" element={<Shell><DemoDashboard /></Shell>} />
        <Route path="/demo/incident" element={<Shell><IncidentPage /></Shell>} />
        <Route path="/demo/analytics" element={<Shell><AnalyticsPage /></Shell>} />
        <Route path="/demo/settings" element={<Shell><SettingsPage /></Shell>} />
        <Route path="/demo/diagnostics" element={<Shell><DiagnosticsPage /></Shell>} />

        {/* LIVE WORKSPACE ROUTES (AUTHENTICATED) */}
        <Route path="/dashboard" element={<RequireAuth><Shell><DemoDashboard /></Shell></RequireAuth>} />
        <Route path="/incident" element={<RequireAuth><Shell><IncidentPage /></Shell></RequireAuth>} />
        <Route path="/analytics" element={<RequireAuth><Shell><AnalyticsPage /></Shell></RequireAuth>} />
        <Route path="/settings" element={<RequireAuth><Shell><SettingsPage /></Shell></RequireAuth>} />
        <Route path="/diagnostics" element={<RequireAuth><Shell><DiagnosticsPage /></Shell></RequireAuth>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}
