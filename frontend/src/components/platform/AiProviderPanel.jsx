import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AlertCircle, CheckCircle2, Cpu, Loader2, ShieldCheck, Sparkles, Trash2, Wifi } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { api, apiErrorMessage } from '../../lib/api';

const providers = [
  { value: 'DISABLED', label: 'Disabled', description: 'Use only deterministic intelligence.' },
  { value: 'OPENAI', label: 'OpenAI', description: 'Optional narrative enrichment.' },
  { value: 'GEMINI', label: 'Google Gemini', description: 'Optional narrative enrichment.' },
  { value: 'CLAUDE', label: 'Claude', description: 'Optional narrative enrichment.' },
];

const defaultModels = {
  OPENAI: 'gpt-4o-mini',
  GEMINI: 'gemini-1.5-flash',
  CLAUDE: 'claude-3-5-haiku-20241022',
};

export function AiProviderPanel() {
  const queryClient = useQueryClient();
  const [provider, setProvider] = useState('DISABLED');
  const [modelName, setModelName] = useState(defaultModels.OPENAI);
  const [apiKey, setApiKey] = useState('');
  const [feedback, setFeedback] = useState(null);
  const [lastValidation, setLastValidation] = useState(null);

  const configQuery = useQuery({
    queryKey: ['ai-provider-config'],
    queryFn: async () => (await api.get('/api/v1/users/me/ai-provider')).data,
  });

  useEffect(() => {
    if (!configQuery.data) {
      return;
    }

    const nextProvider = configQuery.data.configured ? configQuery.data.providerName : 'DISABLED';
    setProvider(nextProvider);
    setModelName(configQuery.data.modelName || defaultModels[nextProvider] || defaultModels.OPENAI);
  }, [configQuery.data]);

  useEffect(() => {
    if (provider !== 'DISABLED' && !modelName) {
      setModelName(defaultModels[provider] || '');
    }
  }, [provider, modelName]);

  const configured = Boolean(configQuery.data?.configured);
  const updatedAt = configQuery.data?.updatedAt ? new Date(configQuery.data.updatedAt).toLocaleString() : 'Not configured';

  const status = useMemo(() => {
    if (configQuery.isLoading) return { label: 'Loading', tone: 'text-slate-400', icon: Loader2 };
    if (configQuery.isError) return { label: 'Unavailable', tone: 'text-warning', icon: AlertCircle };
    if (provider === 'DISABLED') return { label: 'Disabled', tone: 'text-slate-400', icon: AlertCircle };
    if (configured) return { label: 'Configured', tone: 'text-success', icon: CheckCircle2 };
    return { label: 'Needs API key', tone: 'text-warning', icon: AlertCircle };
  }, [configQuery.isError, configQuery.isLoading, configured, provider]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      if (provider === 'DISABLED') {
        await api.delete('/api/v1/users/me/ai-provider');
        return { configured: false };
      }
      return (await api.put('/api/v1/users/me/ai-provider', {
        providerName: provider,
        modelName,
        apiKey,
      })).data;
    },
    onSuccess: (data) => {
      setApiKey('');
      setFeedback({ type: 'success', message: provider === 'DISABLED' ? 'AI provider disabled.' : 'AI provider configuration saved.' });
      queryClient.setQueryData(['ai-provider-config'], data);
      queryClient.invalidateQueries({ queryKey: ['ai-provider-config'] });
    },
    onError: (error) => setFeedback({ type: 'error', message: apiErrorMessage(error, 'Unable to save AI provider configuration.') }),
  });

  const validateMutation = useMutation({
    mutationFn: async () => (await api.post('/api/v1/users/me/ai-provider/validate', {
      providerName: provider,
      modelName,
      apiKey,
    })).data,
    onSuccess: (data) => {
      setLastValidation(data);
      setFeedback({
        type: data.valid ? 'success' : 'error',
        message: data.valid ? 'Connection validated successfully.' : data.message,
      });
    },
    onError: (error) => setFeedback({ type: 'error', message: apiErrorMessage(error, 'Unable to validate this provider.') }),
  });

  const removeMutation = useMutation({
    mutationFn: async () => api.delete('/api/v1/users/me/ai-provider'),
    onSuccess: () => {
      setProvider('DISABLED');
      setModelName(defaultModels.OPENAI);
      setApiKey('');
      setLastValidation(null);
      setFeedback({ type: 'success', message: 'AI provider configuration removed.' });
      queryClient.setQueryData(['ai-provider-config'], { providerName: null, modelName: null, configured: false, updatedAt: null });
    },
    onError: (error) => setFeedback({ type: 'error', message: apiErrorMessage(error, 'Unable to remove AI provider configuration.') }),
  });

  const canSubmitProvider = provider === 'DISABLED' || (provider && modelName && apiKey);
  const canValidate = provider !== 'DISABLED' && modelName && apiKey;
  const StatusIcon = status.icon;

  return (
    <Card className="p-6" data-tour="ai-providers">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <p className="text-sm text-slate-400">AI providers</p>
          <h2 className="mt-1 text-xl font-semibold text-white">Optional enrichment, deterministic core</h2>
        </div>
        <Badge>Optional</Badge>
      </div>

      <div className="mt-6 rounded-2xl border border-white/10 bg-slate-950/70 p-5 text-sm leading-7 text-slate-400">
        <p>ARGUS deterministic intelligence always works.</p>
        <p>AI is optional.</p>
        <p>AI only enhances explanations.</p>
        <p>AI never changes deterministic analysis.</p>
      </div>

      {feedback ? (
        <div className={`mt-4 rounded-2xl border px-4 py-3 text-sm ${feedback.type === 'success' ? 'border-success/20 bg-success/10 text-success' : 'border-danger/20 bg-danger/10 text-danger'}`}>
          {feedback.message}
        </div>
      ) : null}

      <div className="mt-6 grid gap-4 lg:grid-cols-[0.95fr_1.05fr]">
        <div className="space-y-4">
          <label className="block">
            <span className="mb-2 block text-sm text-slate-400">Provider</span>
            <select
              value={provider}
              onChange={(event) => {
                const nextProvider = event.target.value;
                setProvider(nextProvider);
                setModelName(defaultModels[nextProvider] || '');
                setFeedback(null);
              }}
              className="w-full rounded-2xl border border-white/10 bg-slate-950/80 px-4 py-3 text-sm text-white outline-none"
            >
              {providers.map((item) => (
                <option key={item.value} value={item.value}>{item.label}</option>
              ))}
            </select>
            <span className="mt-2 block text-xs text-slate-500">{providers.find((item) => item.value === provider)?.description}</span>
          </label>

          {provider !== 'DISABLED' ? (
            <>
              <label className="block">
                <span className="mb-2 block text-sm text-slate-400">Model name</span>
                <input value={modelName} onChange={(event) => setModelName(event.target.value)} placeholder="e.g. gpt-4o-mini" className="w-full rounded-2xl border border-white/10 bg-slate-950/80 px-4 py-3 text-sm text-white outline-none" />
              </label>

              <label className="block">
                <span className="mb-2 block text-sm text-slate-400">API key</span>
                <input value={apiKey} onChange={(event) => setApiKey(event.target.value)} type="password" autoComplete="off" placeholder={configured ? 'Masked key configured. Enter a new key to replace it.' : 'Enter provider API key'} className="w-full rounded-2xl border border-white/10 bg-slate-950/80 px-4 py-3 text-sm text-white outline-none" />
                {configured ? <span className="mt-2 block text-xs text-slate-500">Masked API key: ••••••••••••</span> : null}
              </label>
            </>
          ) : null}

          <div className="flex flex-wrap gap-3">
            <Button variant="primary" onClick={() => saveMutation.mutate()} disabled={!canSubmitProvider || saveMutation.isPending}>
              {saveMutation.isPending ? <Loader2 size={16} className="mr-2 animate-spin" /> : <Sparkles size={16} className="mr-2" />}
              Save
            </Button>
            <Button variant="secondary" onClick={() => validateMutation.mutate()} disabled={!canValidate || validateMutation.isPending}>
              {validateMutation.isPending ? <Loader2 size={16} className="mr-2 animate-spin" /> : <Wifi size={16} className="mr-2" />}
              Test connection
            </Button>
            <Button variant="secondary" onClick={() => removeMutation.mutate()} disabled={removeMutation.isPending}>
              {removeMutation.isPending ? <Loader2 size={16} className="mr-2 animate-spin" /> : <Trash2 size={16} className="mr-2" />}
              Remove
            </Button>
          </div>
        </div>

        <div className="space-y-4">
          <div className="rounded-2xl border border-white/10 bg-slate-950/70 p-5">
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-2 text-primary">
                <Cpu size={16} />
                <span className="font-medium text-white">Provider status</span>
              </div>
              <Badge>{provider === 'DISABLED' ? 'Disabled' : configured ? 'Configured' : 'Pending'}</Badge>
            </div>
            <div className={`mt-4 flex items-center gap-2 text-sm ${status.tone}`}>
              <StatusIcon size={16} className={configQuery.isLoading ? 'animate-spin' : ''} />
              <span>{status.label}</span>
            </div>
            <p className="mt-2 text-sm text-slate-400">Provider health: {lastValidation?.status || (configured ? 'Saved' : 'Not validated')}</p>
            <p className="mt-2 text-sm text-slate-400">Last validation: {lastValidation?.validatedAt ? new Date(lastValidation.validatedAt).toLocaleString() : updatedAt}</p>
          </div>

          <div className="rounded-2xl border border-white/10 bg-slate-950/70 p-5">
            <div className="flex items-center gap-2 text-success">
              <ShieldCheck size={16} />
              <span className="font-medium text-white">Security note</span>
            </div>
            <p className="mt-3 text-sm leading-7 text-slate-400">Plaintext API keys are sent only to the backend for save or connection validation. The UI never receives plaintext keys from ARGUS.</p>
          </div>
        </div>
      </div>
    </Card>
  );
}
