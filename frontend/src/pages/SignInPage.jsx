import { useState } from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, Lock, Mail, ShieldCheck, AlertCircle } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { usePlatformPreferences } from '../context/PlatformPreferencesContext';
import { api, apiErrorMessage } from '../lib/api';

export default function SignInPage() {
  const navigate = useNavigate();
  const { setPreferences } = usePlatformPreferences();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!email || !password) {
      setError('Please fill in all fields.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await api.post('/api/v1/auth/login', { email, password });
      const { accessToken, refreshToken, user } = response.data;
      
      window.localStorage.setItem('argus-access-token', accessToken);
      window.localStorage.setItem('refreshToken', refreshToken);
      if (user) {
        window.localStorage.setItem('argus-user', JSON.stringify(user));
      }

      setPreferences((current) => ({ ...current, isDemoMode: false }));
      
      navigate('/dashboard');
    } catch (err) {
      setError(apiErrorMessage(err, 'Authentication failed. Please verify credentials.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid min-h-[80vh] items-center py-8 lg:grid-cols-[0.9fr_1.1fr]">
      <motion.div initial={{ opacity: 0, x: -8 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.3 }} className="pr-0 lg:pr-10">
        <Badge>Secure access</Badge>
        <h1 className="mt-5 text-4xl font-semibold text-slate-900 dark:text-white sm:text-5xl">Keep your operations close at hand.</h1>
        <p className="mt-4 max-w-xl text-lg leading-8 text-slate-600 dark:text-slate-400">A premium authentication experience with thoughtful motion and polished validation.</p>
      </motion.div>

      <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.35 }}>
        <Card className="p-6 sm:p-8">
          <form onSubmit={handleSubmit}>
            <div className="flex items-center gap-3">
              <div className="flex h-11 w-11 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary">
                <ShieldCheck size={20} />
              </div>
              <div>
                <p className="text-lg font-semibold text-slate-900 dark:text-white">Welcome back</p>
                <p className="text-sm text-slate-600 dark:text-slate-400">Sign in to your ARGUS workspace.</p>
              </div>
            </div>

            {error ? (
              <div className="mt-6 flex flex-col gap-2 rounded-2xl border border-danger/20 bg-danger/10 p-4 text-sm text-danger animate-shake">
                <div className="flex items-center gap-2">
                  <AlertCircle size={16} className="shrink-0" />
                  <span className="font-semibold">{error}</span>
                </div>
                {error.toLowerCase().includes('verification') || error.toLowerCase().includes('verify') ? (
                  <p className="mt-1 text-xs opacity-90 leading-relaxed text-slate-600 dark:text-slate-300">
                    Check your development server console/logs for the local verification link, or use the link sent to your registered email.
                  </p>
                ) : null}
              </div>
            ) : null}

            <div className="mt-8 space-y-4">
              <label className="block">
                <span className="mb-2 block text-sm text-slate-600 dark:text-slate-400">Email</span>
                <div className="flex items-center rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/70 px-4 py-3 focus-within:border-primary/50 transition">
                  <Mail size={16} className="mr-3 text-slate-400 dark:text-slate-500" />
                  <input 
                    required
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    disabled={loading}
                    className="w-full bg-transparent text-sm text-slate-900 dark:text-white outline-none" 
                    placeholder="name@company.com" 
                  />
                </div>
              </label>

              <label className="block">
                <span className="mb-2 block text-sm text-slate-600 dark:text-slate-400">Password</span>
                <div className="flex items-center rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/70 px-4 py-3 focus-within:border-primary/50 transition">
                  <Lock size={16} className="mr-3 text-slate-400 dark:text-slate-500" />
                  <input 
                    required
                    type="password" 
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    disabled={loading}
                    className="w-full bg-transparent text-sm text-slate-900 dark:text-white outline-none" 
                    placeholder="••••••••" 
                  />
                </div>
              </label>
            </div>

            <Button type="submit" disabled={loading} className="mt-8 w-full py-3">
              {loading ? 'Signing in...' : 'Continue'} <ArrowRight size={16} className="ml-2" />
            </Button>
          </form>

          <p className="mt-5 text-center text-sm text-slate-600 dark:text-slate-400">
            New here? <Link to="/signup" className="text-primary hover:underline">Create an account</Link>
          </p>
        </Card>
      </motion.div>
    </div>
  );
}
