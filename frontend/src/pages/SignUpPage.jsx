import { useState } from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, Lock, Mail, UserRound, CheckCircle2, AlertCircle } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { api, apiErrorMessage } from '../lib/api';

export default function SignUpPage() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!name || !email || !password) {
      setError('Please fill in all fields.');
      return;
    }
    if (password.length < 12) {
      setError('Password must be at least 12 characters.');
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const response = await api.post('/api/v1/auth/register', { name, email, password });
      setSuccess(response.data?.message || 'Registration successful. Verify your email before signing in.');
      setName('');
      setEmail('');
      setPassword('');
      setTimeout(() => {
        navigate('/signin');
      }, 4000);
    } catch (err) {
      setError(apiErrorMessage(err, 'Registration failed. Please check validation rules.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid min-h-[80vh] items-center py-8 lg:grid-cols-[0.9fr_1.1fr]">
      <motion.div initial={{ opacity: 0, x: -8 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.3 }} className="pr-0 lg:pr-10">
        <Badge>Launch your workspace</Badge>
        <h1 className="mt-5 text-4xl font-semibold text-slate-900 dark:text-white sm:text-5xl">Start with clarity, not clutter.</h1>
        <p className="mt-4 max-w-xl text-lg leading-8 text-slate-600 dark:text-slate-400">Create an account to explore a premium monitoring experience built for serious operators.</p>
      </motion.div>

      <motion.div initial={{ opacity: 0, x: 10 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.35 }}>
        <Card className="p-6 sm:p-8">
          <form onSubmit={handleSubmit} className="space-y-4">
            {error ? (
              <div className="flex items-center gap-2 rounded-2xl border border-danger/20 bg-danger/10 p-4 text-sm text-danger animate-shake">
                <AlertCircle size={16} className="shrink-0" />
                <span>{error}</span>
              </div>
            ) : null}

            {success ? (
              <div className="flex items-start gap-2 rounded-2xl border border-success/20 bg-success/10 p-4 text-sm text-success">
                <CheckCircle2 size={16} className="mt-0.5 shrink-0" />
                <div>
                  <p className="font-semibold">Account created</p>
                  <p className="mt-1 text-xs opacity-90">{success}</p>
                </div>
              </div>
            ) : null}

            <label className="block">
              <span className="mb-2 block text-sm text-slate-600 dark:text-slate-400">Name</span>
              <div className="flex items-center rounded-2xl border border-slate-200 dark:border-white/10 bg-slate-100/50 dark:bg-slate-950/70 px-4 py-3 focus-within:border-primary/50 transition">
                <UserRound size={16} className="mr-3 text-slate-400 dark:text-slate-500" />
                <input 
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  disabled={loading}
                  className="w-full bg-transparent text-sm text-slate-900 dark:text-white outline-none" 
                  placeholder="Taylor Nguyen" 
                />
              </div>
            </label>

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
              <span className="mt-1 block text-xs text-slate-500">Must be at least 12 characters, with uppercase, lowercase, numbers, and symbols.</span>
            </label>

            <Button type="submit" disabled={loading} className="mt-8 w-full py-3">
              {loading ? 'Creating account...' : 'Create account'} <ArrowRight size={16} className="ml-2" />
            </Button>
          </form>

          <p className="mt-5 text-center text-sm text-slate-600 dark:text-slate-400">
            Already have an account? <Link to="/signin" className="text-primary hover:underline">Sign in</Link>
          </p>
        </Card>
      </motion.div>
    </div>
  );
}
