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

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 1. Enqueue (QUEUED)
  const createPayload = JSON.stringify({
    workOrderId: `wo-queue-${uniqueId}`,
    technicianId: 'TECH-101',
    notes: 'Queue progression test',
  });

  const createRes = http.post(`${BASE_URL}/api/v1/executions`, createPayload, params);
  check(createRes, {
    'enqueue status is 201': (r) => r.status === 201,
  });

  if (createRes.status === 201) {
    const id = JSON.parse(createRes.body).id;

    // 2. Start Repair (IN_REPAIR)
    const repairPayload = JSON.stringify({
      status: 'IN_REPAIR',
      notes: 'Repair started by technician',
    });
    const repairRes = http.patch(`${BASE_URL}/api/v1/executions/${id}/status`, repairPayload, params);
    check(repairRes, {
      'in_repair status is 200': (r) => r.status === 200,
    });

    // 3. Complete (COMPLETED)
    const completePayload = JSON.stringify({
      status: 'COMPLETED',
      notes: 'Repair completed with all checks passed',
    });
    const completeRes = http.patch(`${BASE_URL}/api/v1/executions/${id}/status`, completePayload, params);
    check(completeRes, {
      'complete status is 200': (r) => r.status === 200,
    });
  }

  sleep(1);
}
