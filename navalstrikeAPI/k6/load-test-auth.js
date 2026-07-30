import http from 'k6/http';
import { check, sleep, group } from 'k6';
import exec from 'k6/execution';

// ============================================================
// Configuração
// ============================================================
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// ============================================================
// Cenários de carga
// ============================================================
export const options = {
  stages: [
    { duration: '10s', target: 5 },
    { duration: '30s', target: 5 },
    { duration: '10s', target: 10 },
    { duration: '30s', target: 10 },
    { duration: '10s', target: 15 },
    { duration: '30s', target: 15 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1500'],
    http_req_failed: ['rate<0.05'],
    'http_req_duration{group:::Register}': ['p(95)<500'],
    'http_req_duration{group:::Login}': ['p(95)<300'],
  },
};

// ============================================================
// Teste — criar conta e fazer login
// ============================================================
export default function () {
  const uniqueId = `${exec.vu.idInTest}-${exec.vu.iterationInInstance}-${Date.now()}`;
  const email = `teste_${uniqueId}@teste.com`;
  const name = `teste_${exec.vu.idInTest}`;
  const password = 'teste12345';

  const headers = { headers: { 'Content-Type': 'application/json' } };

  // 1. Criar conta
  group('Register', () => {
    const res = http.post(`${BASE_URL}/auth/register`, JSON.stringify({
      name: name.substring(0, 15),
      email: email,
      password: password,
      passwordConfirmation: password,
    }), headers);

    check(res, {
      'register 200': (r) => r.status === 200,
      'register has token': (r) => {
        const body = JSON.parse(r.body);
        return body.token !== undefined;
      },
    });
  });

  sleep(1);

  // 2. Login com a conta criada
  group('Login', () => {
    const res = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
      email: email,
      password: password,
    }), headers);

    check(res, {
      'login 200': (r) => r.status === 200,
      'login has token': (r) => {
        const body = JSON.parse(r.body);
        return body.token !== undefined;
      },
    });
  });

  sleep(1);
}
