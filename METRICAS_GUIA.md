# Guia de Métricas - SmallRye Metrics

Este documento explica como usar e acessar as métricas implementadas na API FIPE usando SmallRye Metrics.

## 📊 Endpoint de Métricas

As métricas estão disponíveis no endpoint:

```
GET /q/metrics
```

### Formatos Disponíveis

- **Prometheus** (padrão): `GET /q/metrics` ou `GET /q/metrics?format=prometheus`
- **JSON**: `GET /q/metrics?format=json`

## 🔍 Métricas Implementadas

### Métricas Automáticas do Quarkus

O Quarkus automaticamente expõe métricas JVM e de aplicação:

- **Métricas JVM**: CPU, memória, threads, garbage collection
- **Métricas HTTP**: Requisições REST (contadores e timers automáticos)
- **Métricas de Base de Dados**: Pool de conexões, transações

### Métricas Customizadas de Negócio

#### Contadores (Counters)

1. **`fipe_api_tokens_issued_total`**
   - Descrição: Total de tokens JWT emitidos para clientes da API
   - Incrementado: Quando um cliente faz login com sucesso

2. **`fipe_api_admin_tokens_issued_total`**
   - Descrição: Total de tokens JWT emitidos para administradores
   - Incrementado: Quando um admin faz login com sucesso

3. **`fipe_api_logins_success_total`**
   - Descrição: Total de logins bem-sucedidos (clientes e admins)
   - Incrementado: Em qualquer login bem-sucedido

4. **`fipe_api_logins_failed_total`**
   - Descrição: Total de tentativas de login falhadas
   - Incrementado: Quando credenciais inválidas são fornecidas

5. **`fipe_api_data_updates_total`**
   - Descrição: Total de atualizações de dados FIPE realizadas
   - Incrementado: Quando `/fipe-data/update` é executado com sucesso

6. **`fipe_api_searches_total`**
   - Descrição: Total de buscas realizadas na API
   - Incrementado: Em qualquer busca bem-sucedida (tipos de veículo, marcas, modelos)

#### Gauge (Valor Atual)

1. **`fipe_api_active_sessions`**
   - Descrição: Número de sessões ativas no momento
   - Tipo: Gauge (valor atual, não acumulativo)
   - Atualizado: Automaticamente quando consultado

#### Timers (Duração)

1. **`fipe_api_oauth_token_duration`**
   - Descrição: Duração das requisições ao endpoint `/oauth/token`
   - Unidade: Milissegundos
   - Estatísticas: Média, mínimo, máximo, percentis

2. **`fipe_api_admin_login_duration`**
   - Descrição: Duração das requisições de login de administradores
   - Unidade: Milissegundos

3. **`fipe_api_data_update_duration`**
   - Descrição: Duração das atualizações de dados FIPE
   - Unidade: Milissegundos

4. **`fipe_api_vehicle_types_search_duration`**
   - Descrição: Duração das buscas de tipos de veículo
   - Unidade: Milissegundos

5. **`fipe_api_brands_search_duration`**
   - Descrição: Duração das buscas de marcas
   - Unidade: Milissegundos

6. **`fipe_api_models_search_duration`**
   - Descrição: Duração das buscas de modelos
   - Unidade: Milissegundos

#### Contadores de Requisições por Endpoint

1. **`fipe_api_oauth_token_requests_total`**
   - Total de requisições ao endpoint `/oauth/token`

2. **`fipe_api_admin_login_requests_total`**
   - Total de requisições ao endpoint `/admin/login`

3. **`fipe_api_data_update_requests_total`**
   - Total de requisições ao endpoint `/fipe-data/update`

4. **`fipe_api_vehicle_types_search_requests_total`**
   - Total de buscas de tipos de veículo

5. **`fipe_api_brands_search_requests_total`**
   - Total de buscas de marcas

6. **`fipe_api_models_search_requests_total`**
   - Total de buscas de modelos

## 📖 Exemplos de Uso

### 1. Visualizar Todas as Métricas (Formato Prometheus)

```bash
curl http://localhost:8080/q/metrics
```

### 2. Visualizar Métricas em JSON

```bash
curl http://localhost:8080/q/metrics?format=json
```

### 3. Filtrar Métricas Específicas

```bash
# Apenas métricas customizadas da API
curl http://localhost:8080/q/metrics | grep "fipe_api_"

# Apenas métricas JVM
curl http://localhost:8080/q/metrics | grep "jvm_"
```

### 4. Exemplo de Resposta (Formato Prometheus)

```
# HELP fipe_api_tokens_issued_total Total de tokens JWT emitidos para clientes da API
# TYPE fipe_api_tokens_issued_total counter
fipe_api_tokens_issued_total 42

# HELP fipe_api_active_sessions Número de sessões ativas no momento
# TYPE fipe_api_active_sessions gauge
fipe_api_active_sessions 5.0

# HELP fipe_api_oauth_token_duration Duração das requisições ao endpoint /oauth/token
# TYPE fipe_api_oauth_token_duration summary
fipe_api_oauth_token_duration_count 100
fipe_api_oauth_token_duration_sum 5000.0
fipe_api_oauth_token_duration{quantile="0.5"} 45.0
fipe_api_oauth_token_duration{quantile="0.75"} 60.0
fipe_api_oauth_token_duration{quantile="0.95"} 120.0
fipe_api_oauth_token_duration{quantile="0.98"} 150.0
fipe_api_oauth_token_duration{quantile="0.99"} 200.0
fipe_api_oauth_token_duration{quantile="0.999"} 300.0
```

## 🔧 Integração com Prometheus

### Configuração do Prometheus

Adicione ao `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'fipe-api'
    scrape_interval: 15s
    metrics_path: '/q/metrics'
    static_configs:
      - targets: ['localhost:8080']
```

### Queries Úteis no Prometheus

```promql
# Taxa de requisições por segundo
rate(fipe_api_oauth_token_requests_total[5m])

# Taxa de erros (logins falhados / total de logins)
rate(fipe_api_logins_failed_total[5m]) / rate(fipe_api_logins_success_total[5m])

# Latência média (p95)
histogram_quantile(0.95, rate(fipe_api_oauth_token_duration_bucket[5m]))

# Sessões ativas
fipe_api_active_sessions
```

## 📈 Integração com Grafana

### Dashboard Recomendado

Crie um dashboard no Grafana com os seguintes painéis:

1. **Taxa de Requisições por Endpoint**
   - Query: `rate(fipe_api_*_requests_total[5m])`
   - Tipo: Graph

2. **Taxa de Sucesso de Logins**
   - Query: `rate(fipe_api_logins_success_total[5m]) / (rate(fipe_api_logins_success_total[5m]) + rate(fipe_api_logins_failed_total[5m]))`
   - Tipo: Gauge

3. **Latência P95 por Endpoint**
   - Query: `histogram_quantile(0.95, rate(fipe_api_*_duration_bucket[5m]))`
   - Tipo: Graph

4. **Sessões Ativas**
   - Query: `fipe_api_active_sessions`
   - Tipo: Stat

5. **Tokens Emitidos**
   - Query: `fipe_api_tokens_issued_total`
   - Tipo: Counter

## 🎯 Adicionando Novas Métricas

### Exemplo: Adicionar Contador

```java
@Counted(
    name = "fipe_api_custom_metric_total",
    description = "Descrição da métrica",
    absolute = true
)
public void incrementCustomMetric() {
    // Método vazio - a anotação faz o trabalho
}
```

### Exemplo: Adicionar Timer

```java
@Timed(
    name = "fipe_api_custom_operation_duration",
    description = "Duração da operação customizada",
    unit = MetricUnits.MILLISECONDS,
    absolute = true
)
public void customOperation() {
    // Sua lógica aqui
}
```

### Exemplo: Adicionar Gauge

```java
@Gauge(
    name = "fipe_api_custom_value",
    description = "Valor customizado atual",
    unit = MetricUnits.NONE,
    absolute = true
)
public long getCustomValue() {
    return /* seu valor aqui */;
}
```

## 🔒 Segurança

**Nota**: O endpoint `/q/metrics` está acessível publicamente por padrão. Em produção, considere:

1. Adicionar autenticação ao endpoint
2. Restringir acesso por IP
3. Usar um proxy reverso com autenticação

## 📚 Referências

- [Quarkus Metrics Guide](https://quarkus.io/guides/smallrye-metrics)
- [MicroProfile Metrics Specification](https://github.com/eclipse/microprofile-metrics)
- [Prometheus Documentation](https://prometheus.io/docs/)

