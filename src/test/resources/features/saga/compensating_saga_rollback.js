import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 30 },  // Ramp up
    { duration: '1m', target: 80 },   // Sustained load
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<600'], // Rollback under 600ms
    http_req_failed: ['rate<0.02'],
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const uniqueId = `${Date.now()}_${Math.floor(Math.random() * 10000)}`;

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 1. Open Work Order
  const openPayload = JSON.stringify({
    customerDocument: '52998224725',
    licensePlate: 'ABC1D23',
    description: `Compensating Rollback Stress ${uniqueId}`,
  });
  const openRes = http.post(`${BASE_URL}/api/v1/work-orders`, openPayload, params);
  check(openRes, {
    'rollback 1. open work order status is 201': (r) => r.status === 201,
  });

  if (openRes.status === 201) {
    const workOrderId = JSON.parse(openRes.body).id;

    // 2. Approve Work Order
    const approvePayload = JSON.stringify({ status: 'APPROVED', totalAmount: 500.0 });
    http.patch(`${BASE_URL}/api/v1/work-orders/${workOrderId}/status`, approvePayload, params);

    // 3. Billing creates Invoice
    const invoicePayload = JSON.stringify({
      workOrderId: workOrderId,
      customerDocument: '52998224725',
      amount: 500.0,
    });
    http.post(`${BASE_URL}/api/v1/invoices`, invoicePayload, params);

    // 4. Payment Declined Webhook
    const webhookPayload = JSON.stringify({
      action: 'payment.updated',
      data: { id: `MP-DECLINED-${uniqueId}` },
      status: 'rejected',
    });
    const webRes = http.post(`${BASE_URL}/api/v1/payments/webhook`, webhookPayload, params);
    check(webRes, {
      'rollback 4. webhook reject status is 200': (r) => r.status === 200,
    });

    // 5. Verify Rollback Execution
    const finalRes = http.get(`${BASE_URL}/api/v1/work-orders/${workOrderId}`);
    check(finalRes, {
      'rollback 5. verify work order status is 200': (r) => r.status === 200,
    });
  }

  sleep(1);
}
