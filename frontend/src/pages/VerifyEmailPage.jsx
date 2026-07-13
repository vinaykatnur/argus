import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { CheckCircle2, AlertCircle, Loader2, ShieldCheck, ArrowRight } from 'lucide-react';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { api, apiErrorMessage } from '../lib/api';
import { motion } from 'framer-motion';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [status, setStatus] = useState('verifying'); // verifying, success, error
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('No verification token provided. Please check your verification link.');
      return;
    }

    const verifyToken = async () => {
      try {
        const response = await api.get('/api/v1/auth/verify-email', {
          params: { token },
        });
        setStatus('success');
        setMessage(response.data?.message || 'Your email has been successfully verified.');
      } catch (err) {
        setStatus('error');
        setMessage(apiErrorMessage(err, 'Verification failed. The token may be invalid or expired.'));
      }
    };

    // Wait a brief moment to show smooth transition
    const timer = setTimeout(verifyToken, 1200);
    return () => clearTimeout(timer);
  }, [token]);

  return (
    <div className="flex min-h-[70vh] items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <motion.div
        initial={{ opacity: 0, scale: 0.96 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        className="w-full max-w-md"
      >
        <Card className="p-6 sm:p-8 text-center space-y-6">
          <div className="flex justify-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl border border-primary/20 bg-primary/10 text-primary">
              <ShieldCheck size={24} />
            </div>
          </div>

          <div>
            <Badge>Account verification</Badge>
            <h1 className="mt-4 text-2xl font-semibold text-slate-900 dark:text-white">
              Confirm your identity
            </h1>
          </div>

          <div className="rounded-2xl border border-white/10 bg-slate-950/70 p-6 flex flex-col items-center justify-center min-h-[140px]">
            {status === 'verifying' ? (
              <div className="space-y-4">
                <Loader2 className="h-8 w-8 animate-spin text-primary mx-auto" />
                <p className="text-sm text-slate-400">Verifying your token with ARGUS...</p>
              </div>
            ) : status === 'success' ? (
              <motion.div
                initial={{ opacity: 0, y: 5 }}
                animate={{ opacity: 1, y: 0 }}
                className="space-y-3 text-success"
              >
                <CheckCircle2 className="h-8 w-8 mx-auto" />
                <p className="text-sm font-semibold">Verification Complete</p>
                <p className="text-xs text-slate-400">{message}</p>
              </motion.div>
            ) : (
              <motion.div
                initial={{ opacity: 0, y: 5 }}
                animate={{ opacity: 1, y: 0 }}
                className="space-y-3 text-danger"
              >
                <AlertCircle className="h-8 w-8 mx-auto" />
                <p className="text-sm font-semibold">Verification Failed</p>
                <p className="text-xs text-slate-400">{message}</p>
              </motion.div>
            )}
          </div>

          <div className="pt-2">
            {status === 'verifying' ? (
              <Button disabled className="w-full py-2.5">
                Please wait...
              </Button>
            ) : (
              <Link to="/signin" className="block w-full">
                <Button className="w-full py-2.5">
                  Continue to Sign In <ArrowRight size={16} className="ml-2" />
                </Button>
              </Link>
            )}
          </div>
        </Card>
      </motion.div>
    </div>
  );
}
