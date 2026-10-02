import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up
    { duration: '1m', target: 100 },  // Sustained load
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests under 500ms
    http_req_failed: ['rate<0.01'],    // <1% failure rate
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const uniqueNum = Math.floor(1000 + Math.random() * 9000);
  const doc = `5299822${uniqueNum}`;
  const plate = `BRA${uniqueNum}`;

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 1. Open Work Order (RECEIVED)
  const openPayload = JSON.stringify({
    customerDocument: doc,
    licensePlate: plate,
    description: 'k6 Lifecycle stress test order',
  });

  const openRes = http.post(`${BASE_URL}/api/v1/work-orders`, openPayload, params);
  check(openRes, {
    'open work order status is 201': (r) => r.status === 201,
  });

  if (openRes.status === 201) {
    const id = JSON.parse(openRes.body).id;

    // 2. Query Work Order
    const getRes = http.get(`${BASE_URL}/api/v1/work-orders/${id}`);
    check(getRes, {
      'get work order status is 200': (r) => r.status === 200,
    });

    // 3. Diagnose
    const diagPayload = JSON.stringify({ status: 'DIAGNOSING', totalAmount: 0.0 });
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}/status`, diagPayload, params);

    // 4. Budget
    const budgetPayload = JSON.stringify({ status: 'WAITING_FOR_APPROVAL', totalAmount: 450.0 });
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}/status`, budgetPayload, params);

    // 5. Approve
    const approvePayload = JSON.stringify({ status: 'APPROVED', totalAmount: 450.0 });
    const approveRes = http.patch(`${BASE_URL}/api/v1/work-orders/${id}/status`, approvePayload, params);
    check(approveRes, {
      'approve work order status is 200': (r) => r.status === 200,
    });

    // 6. Finish & Release
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}/status`, JSON.stringify({ status: 'FINISHED' }), params);
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}/status`, JSON.stringify({ status: 'RELEASED' }), params);
  }

  // 7. Metrics Query
  const metricRes = http.get(`${BASE_URL}/api/v1/work-orders/metrics/average-time`);
  check(metricRes, {
    'metrics query returns 200 or 204': (r) => r.status === 200 || r.status === 204,
  });

  sleep(1);
}
