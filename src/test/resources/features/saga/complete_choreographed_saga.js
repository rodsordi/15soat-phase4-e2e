import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 30 },  // Ramp up
    { duration: '1m', target: 80 },   // Sustained load
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<700'], // Distributed orchestration under 700ms
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
    licensePlate: 'BRA2E19',
    description: `Complete Saga Stress Test ${uniqueId}`,
  });
  const openRes = http.post(`${BASE_URL}/api/v1/work-orders`, openPayload, params);
  check(openRes, {
    'saga 1. open work order status is 201': (r) => r.status === 201,
  });

  if (openRes.status === 201) {
    const workOrderId = JSON.parse(openRes.body).id;

    // 2. Approve Work Order
    const approvePayload = JSON.stringify({ status: 'APPROVED', totalAmount: 500.0 });
    const approveRes = http.patch(`${BASE_URL}/api/v1/work-orders/${workOrderId}/status`, approvePayload, params);
    check(approveRes, {
      'saga 2. approve status is 200': (r) => r.status === 200,
    });

    // 3. Billing creates Invoice
    const invoicePayload = JSON.stringify({
      workOrderId: workOrderId,
      customerDocument: '52998224725',
      amount: 500.0,
    });
    const invRes = http.post(`${BASE_URL}/api/v1/invoices`, invoicePayload, params);
    check(invRes, {
      'saga 3. create invoice status is 201': (r) => r.status === 201,
    });

    // 4. Mercado Pago confirms Payment
    const webhookPayload = JSON.stringify({
      action: 'payment.updated',
      data: { id: `MP-${uniqueId}` },
      status: 'approved',
    });
    const webRes = http.post(`${BASE_URL}/api/v1/payments/webhook`, webhookPayload, params);
    check(webRes, {
      'saga 4. webhook confirm status is 200': (r) => r.status === 200,
    });

    // 5. Execution enqueues & completes
    const execPayload = JSON.stringify({
      workOrderId: workOrderId,
      technicianId: 'TECH-MASTER',
      notes: 'Saga stress test execution',
    });
    const execRes = http.post(`${BASE_URL}/api/v1/executions`, execPayload, params);
    check(execRes, {
      'saga 5. exec enqueue status is 201': (r) => r.status === 201,
    });

    if (execRes.status === 201) {
      const execId = JSON.parse(execRes.body).id;
      http.patch(`${BASE_URL}/api/v1/executions/${execId}/status`, JSON.stringify({ status: 'COMPLETED' }), params);
    }

    // 6. Verify final status
    const finalRes = http.get(`${BASE_URL}/api/v1/work-orders/${workOrderId}`);
    check(finalRes, {
      'saga 6. get work order status is 200': (r) => r.status === 200,
    });
  }

  sleep(1);
}
