import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    load_test: {
      executor: 'constant-arrival-rate',
      rate: 250,
      timeUnit: '1s',
      duration: '4000s',
      preAllocatedVUs: 500,
      maxVUs: 1500,
      exec: 'load',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const endpoint = `${BASE_URL}/api/v1/payments`;
const currencies = ['USD', 'EUR', 'GBP', 'JPY', 'INR'];

export function load() {
  const senderId = Math.floor(Math.random() * 900000) + 100000;
  let receiverId = Math.floor(Math.random() * 900000) + 100000;
  if (receiverId === senderId) {
    receiverId += 1;
  }

  const payload = JSON.stringify({
    senderId,
    receiverId,
    amount: Number((Math.random() * 500 + 1).toFixed(2)),
    currency: currencies[Math.floor(Math.random() * currencies.length)],
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(endpoint, payload, params);
  check(res, {
    'status is 201': (r) => r.status === 201,
  });
}
