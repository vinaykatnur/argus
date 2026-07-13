export const demoMonitors = [
  { id: 1, name: 'API Gateway', region: 'us-east-1', status: 'healthy', latency: 82, uptime: 99.97 },
  { id: 2, name: 'Authentication', region: 'eu-west-1', status: 'warning', latency: 141, uptime: 99.42 },
  { id: 3, name: 'Payments', region: 'us-west-2', status: 'healthy', latency: 67, uptime: 99.98 },
  { id: 4, name: 'Inventory', region: 'ap-south-1', status: 'critical', latency: 312, uptime: 97.8 },
  { id: 5, name: 'Search', region: 'us-east-1', status: 'healthy', latency: 95, uptime: 99.94 },
  { id: 6, name: 'Notification Service', region: 'eu-central-1', status: 'healthy', latency: 71, uptime: 99.96 },
];

export const demoIncidents = [
  {
    id: 'INC-2048',
    title: 'Elevated auth latency triggered by upstream validation drift',
    severity: 'warning',
    time: '12 min ago',
    summary: 'Latency climbed above threshold while login success rates remained stable.',
  },
  {
    id: 'INC-2047',
    title: 'Inventory queue backlog introduced delayed sync',
    severity: 'critical',
    time: '47 min ago',
    summary: 'A retry storm caused partial fulfillment delays across the catalog.',
  },
];

export const demoActivity = [
  'Latency normalized on the payments edge after connection pooling was restored.',
  'A new incident intelligence recommendation was generated for Authentication.',
  'Availability remained above 99.9% across all production regions.',
];

export const demoTimeline = [
  { time: '09:12', title: 'Ingress spike noticed', detail: 'A burst in requests reached the gateway on the east edge.' },
  { time: '09:16', title: 'Authentication degraded', detail: 'Validation latency exceeded the 200ms threshold.' },
  { time: '09:24', title: 'Recommendation generated', detail: 'ARGUS highlighted an upstream dependency mismatch and a retry policy drift.' },
];
