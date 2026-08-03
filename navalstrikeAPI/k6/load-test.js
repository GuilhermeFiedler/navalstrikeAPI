import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
  iterations: 17,
  vus: 5,
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.5'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const id = randomString(8);
  const email = `loadtest_${id}@test.com`;
  const password = 'LoadTest@123';

  const headers = { 'Content-Type': 'application/json' };

  const registerRes = http.post(
    `${BASE_URL}/auth/register`,
    JSON.stringify({
      name: `User${id.substring(0, 5)}`,
      email: email,
      password: password,
      passwordConfirmation: password,
    }),
    { headers, tags: { endpoint: 'register' } }
  );
  check(registerRes, {
    'register: status 200': (r) => r.status === 200,
  });

  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({
      email: email,
      password: password,
    }),
    { headers, tags: { endpoint: 'login' } }
  );
  check(loginRes, {
    'login: status 200': (r) => r.status === 200,
  });

  sleep(0.2);
}
