import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up
    { duration: '1m', target: 100 },  // Sustained load
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], // 95% of webhook posts under 300ms
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const uniqueId = `${Date.now()}_${Math.floor(Math.random() * 10000)}`;
  const statusChoice = Math.random() > 0.1 ? 'approved' : 'rejected';

  const webhookPayload = JSON.stringify({
    action: 'payment.updated',
    data: {
      id: `MP-STRESS-${uniqueId}`,
    },
    status: statusChoice,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(`${BASE_URL}/api/v1/payments/webhook`, webhookPayload, params);
  check(res, {
    'webhook processed status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
