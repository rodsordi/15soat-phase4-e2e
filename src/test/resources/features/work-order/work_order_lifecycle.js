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
const AUTH_TOKEN = __ENV.AUTH_TOKEN || 'Bearer mock_employee_jwt_token';

export default function () {
  const uniqueNum = Math.floor(1000 + Math.random() * 9000);
  const doc = `5299822${uniqueNum}`;
  const plate = `BRA${uniqueNum}`;

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': AUTH_TOKEN,
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
    const getRes = http.get(`${BASE_URL}/api/v1/work-orders/${id}`, params);
    check(getRes, {
      'get work order status is 200': (r) => r.status === 200,
    });

    // 3. Diagnose
    const diagPayload = JSON.stringify({ status: 'DIAGNOSING' });
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}`, diagPayload, params);

    // 4. Budget Waiting Approval
    const budgetPayload = JSON.stringify({ status: 'WAITING_APPROVAL' });
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}`, budgetPayload, params);

    // 5. Customer Approves
    const approvePayload = JSON.stringify({ status: 'APPROVED' });
    const approveRes = http.patch(`${BASE_URL}/api/v1/work-orders/${id}`, approvePayload, params);
    check(approveRes, {
      'approve work order status is 200': (r) => r.status === 200,
    });

    // 6. Maintenance Executing
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}`, JSON.stringify({ status: 'EXECUTING' }), params);

    // 7. Repairs Completed
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}`, JSON.stringify({ status: 'COMPLETED' }), params);

    // 8. Settlement Confirmed (PAID)
    http.patch(`${BASE_URL}/api/v1/work-orders/${id}`, JSON.stringify({ status: 'PAID' }), params);

    // 9. Delivery Released
    const deliverRes = http.patch(`${BASE_URL}/api/v1/work-orders/${id}`, JSON.stringify({ status: 'DELIVERED' }), params);
    check(deliverRes, {
      'deliver work order status is 200': (r) => r.status === 200,
    });
  }

  // 10. Metrics Query
  const metricRes = http.get(`${BASE_URL}/api/v1/work-orders/metrics`, params);
  check(metricRes, {
    'metrics query returns 200 or 204': (r) => r.status === 200 || r.status === 204,
  });

  sleep(1);
}
