import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 30 },  // Ramp up
    { duration: '1m', target: 80 },   // Sustained load
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<700'], // Distributed choreography under 700ms
    http_req_failed: ['rate<0.02'],
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';
const AUTH_TOKEN = __ENV.AUTH_TOKEN || 'Bearer mock_saga_jwt_token';

export default function () {
  const uniqueId = `${Date.now()}_${Math.floor(Math.random() * 10000)}`;

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': AUTH_TOKEN,
    },
  };

  // 1. Open Work Order
  const openPayload = JSON.stringify({
    customerDocument: '52998224725',
    licensePlate: 'BRA2E19',
    description: `Choreographed Saga Stress Test ${uniqueId}`,
  });
  const openRes = http.post(`${BASE_URL}/api/v1/work-orders`, openPayload, params);
  check(openRes, {
    'saga 1. open work order status is 201': (r) => r.status === 201,
  });

  if (openRes.status === 201) {
    const workOrderId = JSON.parse(openRes.body).id;

    // 2. Diagnose & Budget Waiting Approval
    http.patch(`${BASE_URL}/api/v1/work-orders/${workOrderId}`, JSON.stringify({ status: 'DIAGNOSING' }), params);
    http.patch(`${BASE_URL}/api/v1/work-orders/${workOrderId}`, JSON.stringify({ status: 'WAITING_APPROVAL' }), params);

    // 3. Customer Approves Work Order
    const approvePayload = JSON.stringify({ status: 'APPROVED' });
    const approveRes = http.patch(`${BASE_URL}/api/v1/work-orders/${workOrderId}`, approvePayload, params);
    check(approveRes, {
      'saga 2. approve work order status is 200': (r) => r.status === 200,
    });

    // 4. Mercado Pago Webhook Payment Confirmation
    const webhookPayload = JSON.stringify({
      action: 'payment.updated',
      data: { id: `MOCK-PAY-${uniqueId}` },
    });
    const webhookRes = http.post(`${BASE_URL}/api/v1/payments/mercadopago/webhook`, webhookPayload, params);
    check(webhookRes, {
      'saga 3. payment webhook accepted': (r) => r.status === 200,
    });

    // 5. Query Work Order Status
    const getRes = http.get(`${BASE_URL}/api/v1/work-orders/${workOrderId}`, params);
    check(getRes, {
      'saga 4. work order query is 200': (r) => r.status === 200,
    });
  }

  sleep(1);
}
