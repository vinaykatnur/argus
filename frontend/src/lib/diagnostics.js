import { useQuery } from '@tanstack/react-query';
import { api, hasAuthToken } from './api';

export function useRuntimeDiagnostics() {
  const hasToken = hasAuthToken();
  return useQuery({
    queryKey: ['runtime-diagnostics', hasToken],
    queryFn: async () => {
      if (!hasToken) {
        return {
          status: 'UP',
          environment: 'demo',
          applicationVersion: '0.0.1-SNAPSHOT-DEMO',
          javaVersion: '21',
          uptime: '1d 4h 12m',
          memory: { value: '256 MB / 1.0 GB' },
          disk: { value: '18.4 GB / 100 GB' },
          cpu: { value: '0.8%' },
          responseTimeMillis: 12,
          services: [
            { name: "Backend Health", status: "UP", detail: "API request completed.", responseTimeMillis: 12 },
            { name: "Database", status: "UP", detail: "Connectivity verified.", responseTimeMillis: 5 },
            { name: "Scheduler", status: "UP", detail: "Monitoring scheduler bean is available.", responseTimeMillis: 2 },
            { name: "Analytics Engine", status: "UP", detail: "Analytics aggregation bean is available.", responseTimeMillis: 3 },
            { name: "Incident Intelligence Engine", status: "UP", detail: "Deterministic analysis bean is available.", responseTimeMillis: 1 },
            { name: "AI Narrative Engine", status: "OPTIONAL", detail: "Optional AI provider is not configured.", responseTimeMillis: 0 }
          ]
        };
      }
      const started = performance.now();
      const response = await api.get('/api/v1/diagnostics/runtime');
      return { ...response.data, responseTimeMillis: Math.round(performance.now() - started) };
    },
    refetchInterval: hasToken ? 30000 : false,
  });
}

export function useNotificationHealth() {
  const hasToken = hasAuthToken();
  return useQuery({
    queryKey: ['notification-health', hasToken],
    queryFn: async () => {
      if (!hasToken) {
        return {
          notificationQueueSize: 0,
          retryQueueSize: 0
        };
      }
      return (await api.get('/api/v1/notifications/health')).data;
    },
    refetchInterval: hasToken ? 30000 : false,
  });
}

export function statusTone(status) {
  if (status === 'UP' || status === 'VALID') return 'success';
  if (status === 'OPTIONAL' || status === 'UNAVAILABLE') return 'warning';
  return 'danger';
}
