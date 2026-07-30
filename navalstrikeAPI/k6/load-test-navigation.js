import http from 'k6/http';
import { check, sleep, group } from 'k6';

// ============================================================
// Configuração
// ============================================================
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USER_EMAIL = __ENV.USER_EMAIL || 'teste@email.com';
const USER_PASSWORD = __ENV.USER_PASSWORD || 'senha123';

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
    http_req_duration: ['p(95)<300', 'p(99)<1000'],
    http_req_failed: ['rate<0.05'],
    'http_req_duration{group:::History}': ['p(95)<200'],
    'http_req_duration{group:::Ranking}': ['p(95)<200'],
    'http_req_duration{group:::Available Matches}': ['p(95)<150'],
    'http_req_duration{group:::Create Match}': ['p(95)<300'],
  },
};

// ============================================================
// Setup — login
// ============================================================
export function setup() {
  const res = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    email: USER_EMAIL,
    password: USER_PASSWORD,
  }), { headers: { 'Content-Type': 'application/json' } });

  check(res, { 'login 200': (r) => r.status === 200 });

  return { token: JSON.parse(res.body).token };
}

// ============================================================
// Teste — simula navegação entre páginas + criar partida
// ============================================================
export default function (data) {
  const headers = {
    headers: {
      Authorization: `Bearer ${data.token}`,
      'Content-Type': 'application/json',
    },
  };

  // Usuário abre a lista de partidas disponíveis
  group('Available Matches', () => {
    const res = http.get(`${BASE_URL}/matches`, headers);
    check(res, { 'status 200': (r) => r.status === 200 });
  });

  sleep(1);

  // Usuário navega para o histórico
  group('History', () => {
    const res = http.get(`${BASE_URL}/matches/history?page=0&size=10`, headers);
    check(res, { 'status 200': (r) => r.status === 200 });
  });

  sleep(1);

  // Usuário olha o ranking
  group('Ranking', () => {
    const res = http.get(`${BASE_URL}/ranking`, headers);
    check(res, { 'status 200': (r) => r.status === 200 });
  });

  sleep(1);

  // Usuário cria uma partida
  group('Create Match', () => {
    const res = http.post(`${BASE_URL}/matches`, null, headers);
    check(res, { 'status 200': (r) => r.status === 200 });
  });

  sleep(2);
}
