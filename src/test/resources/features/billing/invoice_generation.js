import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up
    { duration: '1m', target: 100 },  // Sustained load
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<450'], // 95% of requests under 450ms
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const uniqueId = `${Date.now()}_${Math.floor(Math.random() * 10000)}`;

  const payload = JSON.stringify({
    workOrderId: `wo-${uniqueId}`,
    customerDocument: '52998224725',
    amount: 350.00,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const createRes = http.post(`${BASE_URL}/api/v1/invoices`, payload, params);
  check(createRes, {
    'create invoice status is 201': (r) => r.status === 201,
  });

  if (createRes.status === 201) {
    const invoiceId = JSON.parse(createRes.body).id;
    const getRes = http.get(`${BASE_URL}/api/v1/invoices/${invoiceId}`);
    check(getRes, {
      'get invoice status is 200': (r) => r.status === 200,
    });
  }

  sleep(1);
}
