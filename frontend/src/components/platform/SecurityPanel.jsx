import { ShieldCheck, LogOut, KeyRound, LockKeyhole } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';

const tokenKeys = ['argus-access-token', 'argus-auth-token', 'accessToken', 'token', 'refreshToken'];

export function SecurityPanel() {
  const navigate = useNavigate();

  const logout = () => {
    for (const storage of [window.sessionStorage, window.localStorage]) {
      tokenKeys.forEach((key) => storage.removeItem(key));
    }
    navigate('/signin');
  };

  return (
    <Card className="p-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm text-slate-400">Security</p>
          <h2 className="mt-1 text-xl font-semibold text-white">Session and access controls</h2>
        </div>
        <Badge>Protected</Badge>
      </div>

      <div className="mt-6 grid gap-4 lg:grid-cols-[1fr_0.9fr]">
        <div className="rounded-2xl border border-white/10 bg-slate-950/70 p-5">
          <div className="flex items-center gap-2 text-success">
            <ShieldCheck size={16} />
            <span className="font-medium text-white">Current session</span>
          </div>
          <p className="mt-3 text-sm text-slate-400">ARGUS uses token-backed API sessions. Logging out clears local session tokens on this device.</p>
          <div className="mt-4 flex flex-wrap gap-3">
            <Button variant="secondary" onClick={logout}>
              <LogOut size={16} className="mr-2" />
              Logout
            </Button>
          </div>
        </div>

        <div className="rounded-2xl border border-white/10 bg-slate-950/70 p-5">
          <div className="flex items-center gap-2 text-primary">
            <KeyRound size={16} />
            <span className="font-medium text-white">Credential management</span>
          </div>
          <p className="mt-3 text-sm text-slate-400">Password reset is available through the authentication flow. Inline password management is reserved for a future authenticated account endpoint.</p>
          <div className="mt-4 rounded-2xl border border-white/10 bg-white/5 p-4 text-sm text-slate-400">
            <div className="flex items-center gap-2 text-slate-300">
              <LockKeyhole size={16} />
              <span>Future password management layout</span>
            </div>
            <p className="mt-2 text-xs text-slate-500">No placeholder action is exposed until a backend contract exists.</p>
          </div>
        </div>
      </div>
    </Card>
  );
}
