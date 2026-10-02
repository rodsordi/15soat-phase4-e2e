import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up to 50 VUs
    { duration: '1m', target: 100 },  // Maintain 100 VUs
    { duration: '30s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<400'], // 95% of requests under 400ms
    http_req_failed: ['rate<0.01'],    // <1% failure rate
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const uniqueId = `${Date.now()}_${Math.floor(Math.random() * 10000)}`;
  const doc = `5299822${Math.floor(1000 + Math.random() * 9000)}`;
  const plate = `BRA${Math.floor(1000 + Math.random() * 9000)}`;

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 1. Create Customer
  const custPayload = JSON.stringify({
    document: doc,
    name: `Customer Stress ${uniqueId}`,
    email: `stress_${uniqueId}@fiap.com.br`,
  });

  const custRes = http.post(`${BASE_URL}/api/v1/customers`, custPayload, params);
  check(custRes, {
    'create customer status is 201 or 200': (r) => r.status === 201 || r.status === 200,
  });

  // 2. Query Customer
  const getCustRes = http.get(`${BASE_URL}/api/v1/customers/${doc}`);
  check(getCustRes, {
    'get customer status is 200': (r) => r.status === 200,
  });

  // 3. Create Vehicle Linked to Customer
  const vehPayload = JSON.stringify({
    licensePlate: plate,
    customerDocument: doc,
    brand: 'Toyota',
    model: 'Corolla',
    year: 2023,
  });

  const vehRes = http.post(`${BASE_URL}/api/v1/vehicles`, vehPayload, params);
  check(vehRes, {
    'create vehicle status is 201 or 200': (r) => r.status === 201 || r.status === 200,
  });

  // 4. Query Vehicle
  const getVehRes = http.get(`${BASE_URL}/api/v1/vehicles/${plate}`);
  check(getVehRes, {
    'get vehicle status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
