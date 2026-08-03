# Observabilidade — Setup no Ubuntu

Guia para subir a stack de monitoramento (Prometheus, Grafana e Jaeger) com Docker.

---

## Subindo a Stack

### 1. Acessar a raiz do projeto

```bash
cd navalstrikeAPI
```

### 2. Subir todos os containers

```bash
docker compose up -d
```

Isso sobe os seguintes serviços:

| Serviço | Porta | URL de acesso |
|---------|-------|---------------|
| PostgreSQL | 5432 | `localhost:5432` |
| API (Naval Strike) | 8080 | `http://localhost:8080` |
| Prometheus | 9090 | `http://localhost:9090` |
| Grafana | 3000 | `http://localhost:3000` |
| Jaeger | 16686 | `http://localhost:16686` |

### 3. Verificar se está tudo rodando

```bash
docker compose ps
```

Todos os containers devem estar com status `Up` ou `running`.

---

## Acessando os Dashboards

### Grafana

1. Acesse `http://localhost:3000`
2. Login padrão:
   - **Usuário:** `admin`
   - **Senha:** `admin`
3. O datasource do Prometheus e do Jaeger já estão pré-configurados via provisioning
4. O dashboard **Naval Strike Observability** já está importado automaticamente (pasta `grafana/provisioning/dashboards/json/`)

### Prometheus

1. Acesse `http://localhost:9090`
2. Em **Status > Targets**, verifique se o target `navalstrike-apilocal` está `UP`
3. Métricas disponíveis em `http://localhost:8080/actuator/prometheus` (auth básica: `prometheus` / `prometheus`)

### Jaeger (Tracing)

1. Acesse `http://localhost:16686`
2. Selecione o serviço da API no dropdown
3. Visualize traces de requests individuais (latência, spans, chamadas ao banco)

---

## Comandos Úteis

```bash
# Ver logs de um serviço específico
docker compose logs -f grafana
docker compose logs -f prometheus
docker compose logs -f api

# Reiniciar um serviço
docker compose restart prometheus

# Parar tudo
docker compose down

# Parar e remover volumes (reset completo)
docker compose down -v

# Rebuild da API após alterações no código
docker compose up -d --build api
```

---

## Estrutura dos arquivos de configuração

```
navalstrikeAPI/
├── docker-compose.yml              # Orquestração de todos os serviços
├── prometheus.yml                  # Config do Prometheus (targets de scrape)
├── Dockerfile                      # Build da API (multi-stage)
└── grafana/
    └── provisioning/
        ├── datasources/
        │   └── prometheus.yml      # Datasources pré-configurados (Prometheus + Jaeger)
        └── dashboards/
            ├── dashboards.yml      # Provider de dashboards
            └── json/
                └── navalstrike-observability.json  # Dashboard exportado
```

---

## Troubleshooting

| Problema | Solução |
|----------|---------|
| Prometheus target `DOWN` | Verificar se a API subiu: `docker compose logs api` |
| Grafana sem dados | Esperar ~15s (scrape interval) e verificar se o Prometheus está coletando |
| Porta já em uso | `sudo lsof -i :PORTA` para identificar o processo e `kill` se necessário |
| Permissão negada no Docker | Rodar `sudo usermod -aG docker $USER` e relogar |
| API não conecta no banco | Verificar healthcheck do postgres: `docker compose logs db` |
| Jaeger sem traces | Confirmar que a API tem dependência OpenTelemetry configurada e `OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger:4318` |

---

## Rodando os testes de carga (k6) com a stack ativa

Com a stack rodando, você pode executar os testes k6 e observar o impacto em tempo real no Grafana:

```bash
# Instalar k6
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt update
sudo apt install -y k6

# Rodar teste de auth
k6 run navalstrikeAPI/k6/load-test-auth.js

# Rodar teste de navegação (precisa de um usuário existente)
k6 run -e USER_EMAIL=teste@email.com -e USER_PASSWORD=senha123 navalstrikeAPI/k6/load-test-navigation.js
```

Enquanto o k6 roda, acompanhe as métricas no Grafana (`http://localhost:3000`) para visualizar latência, throughput e erros em tempo real.
