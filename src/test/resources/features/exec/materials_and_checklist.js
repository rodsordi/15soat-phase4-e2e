import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up
    { duration: '1m', target: 100 },  // Sustained load
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<400'], // 95% of requests under 400ms
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

  // 1. Create Execution Order
  const execPayload = JSON.stringify({
    workOrderId: `wo-${uniqueId}`,
    technicianId: 'TECH-101',
    notes: 'Materials and checklist stress test',
  });

  const execRes = http.post(`${BASE_URL}/api/v1/executions`, execPayload, params);
  check(execRes, {
    'create execution status is 201': (r) => r.status === 201,
  });

  if (execRes.status === 201) {
    const execId = JSON.parse(execRes.body).id;

    // 2. Post Material
    const matPayload = JSON.stringify({
      materialCode: `PART-${Math.floor(100 + Math.random() * 900)}`,
      description: 'Oleo Motor 5W30 Sintetico',
      quantity: 4,
    });
    const matRes = http.post(`${BASE_URL}/api/v1/executions/${execId}/materials`, matPayload, params);
    check(matRes, {
      'post material status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    });

    // 3. Post Checklist Item
    const checkPayload = JSON.stringify({
      task: 'Verificar nivel do oleo e filtro',
      completed: true,
    });
    const checkRes = http.post(`${BASE_URL}/api/v1/executions/${execId}/checklist`, checkPayload, params);
    check(checkRes, {
      'post checklist status is 200': (r) => r.status === 200,
    });
  }

  sleep(1);
}
