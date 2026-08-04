# Tasks do projeto fipe-api

Documento reconstruído a partir do histórico do Git (ago/2025 – nov/2025).  
Cada item corresponde a um commit ou a um conjunto próximo de commits.

---

## Fase 1 — Bootstrap e domínio (13–14 ago 2025)

- [x] **Scaffold inicial do projeto Quarkus**  
  Maven Wrapper, Dockerfiles, LICENSE, README e recursos de exemplo.  
  `6208f89` — *Initial commit*

- [x] **Configurar base da aplicação e documentar entidades FIPE**  
  `application.properties`, chaves JWT (PEM) e rascunho `FIPE-ENTIDADES.md`.  
  `c0c9745` — *iniciando*

- [x] **Implementar entidades do domínio FIPE**  
  `VehicleType`, `Brand`, `Model`, `ModelYear`, `Price`.  
  `c5d4303` — *feat: entidades implementadas*

- [x] **Implementar autenticação JWT**  
  `AuthController`, `AuthService`, `JwtKeyService`, `ApiClient`, `ApiAccessLog`, `JwksResource`, filtro de access log e enums de segurança.  
  `3a7cff8` — *feat: implementando autenticação*

- [x] **Expor API GraphQL protegida com JWT**  
  `FipeGraphQLApi` e `SecurityRoles`.  
  `4c199d6` — *feat: Implementa API GraphQL protegida com JWT*

- [x] **Remover classes de exemplo do scaffold**  
  `GreetingResource`, `HelloGraphQLResource`, `MyEntity`.  
  `e958aa7` — *remoção de classes inúteis*

---

## Fase 2 — Ingestão de dados FIPE (set 2025)

- [x] **Adicionar dataset FIPE de referência**  
  `fipe-data-2025-09-02.json`.  
  `1b2496e` — *Create fipe-data-2025-09-02.json*

- [x] **Implementar atualização do banco a partir dos dados FIPE**  
  `FipeDataController`, `FipeDataService`, DTOs, documentação `API_FIPE_UPDATE.md` e `modelo.json`.  
  `2041bd6` — *feat: implementação da ia para a atualização de banco da fipe*

---

## Fase 3 — Modelo, auth admin e buscas (20 nov 2025)

- [x] **Separar modelo e versão na entidade `Model`**  
  Parser `ModelVersionParser`, ajustes no `FipeDataService` e script de migração.  
  `eda6325` — *feat: adiciona separação de modelo e versão na entidade Model*

- [x] **Adicionar exemplo de payload FIPE**  
  `fipe-example.json`.  
  `94887da` — *Create fipe-example.json*

- [x] **Implementar autenticação admin e rastreamento de sessões**  
  `AdminUser`, `Session`, `AdminController`, `SessionService` e evolução do `AuthService`/access log.  
  `f437a50` — *feat(auth): implementa sistema de autenticação admin e rastreamento de sessões*

- [x] **Busca paginada de tipos de veículo (hierarquia completa)**  
  `FipeSearchController` + `FipeSearchService` + DTOs.  
  `47e887c` — *feat: adiciona busca paginada de tipos de veículo com dados hierárquicos completos*

- [x] **Busca paginada de marcas (hierarquia completa)**  
  `f8daf43` — *feat: adiciona busca paginada de marcas com dados hierárquicos completos*

- [x] **Busca paginada de modelos (hierarquia completa)**  
  `78452a3` — *feat: adiciona busca paginada de modelos com dados hierárquicos completos*

- [x] **Busca flexível GraphQL com filtros opcionais e paginação**  
  `GraphQLDtos` e expansão de `FipeGraphQLApi`.  
  `2633e03` — *feat: implementa busca flexível GraphQL com filtros opcionais e paginação*

- [x] **Refatorar autenticação para email e senha**  
  Ajustes em controllers, DTOs, `ApiClient` e `AuthService`.  
  `4dcdece` — *refactor: alterar autenticação para usar email e senha*

- [x] **Remover endpoint JWKS legado**  
  `de17bf1` — *Delete JwksResource.java*

- [x] **Criar seeder do usuário admin padrão**  
  `AdminSeeder`.  
  `91805eb` — *feat: adicionar seeder para criar usuário admin padrão*

- [x] **Corrigir processamento FIPE (transações, constraints e timeout)**  
  Melhorias em `FipeDataService`, `AccessLogService`, `JwtKeySeeder`, `JwksController` e configs.  
  `875e35e` — *fix: corrige erros de transação, constraint violations e timeout no processamento FIPE*

- [x] **Atualizar `.gitignore`**  
  `ccfa87d` — *Update .gitignore*

---

## Fase 4 — Testes (21 nov 2025)

- [x] **Testes unitários dos controllers**  
  Admin, Auth, FipeData, FipeSearch, Jwks.  
  `d7004a5`

- [x] **Testes do `AuthService` (QuarkusTest)**  
  `d968b1d`

- [x] **Testes do `SessionService` e remoção dos testes de exemplo**  
  `f5f1f7a`

- [x] **Testes de `FipeDataService`, `FipeSearchService` e `JwtKeyService`**  
  `bc46fbd`

- [x] **Testes de `AccessLogService`, `ModelVersionParser` e `PemUtils`**  
  `9f43d0d`

- [x] **Testes do `AccessLogFilter`**  
  `88f066f`

- [x] **Testes das Entities**  
  `c6f0b7e`

- [x] **Testes dos Seeders**  
  `481f643`

- [x] **Corrigir inicialização de chaves JWT nos testes**  
  `JwtKeyCleanupTestResource`.  
  `986c2fe`

- [x] **Testes de `SecurityRoles`**  
  `45bdac8`

- [x] **Testes dos DTOs**  
  `45b83bb`

- [x] **Testes de tratamento de erros**  
  `80d4052`

- [x] **Testes de integração para cenários completos**  
  `d846b3c`

---

## Fase 5 — Observabilidade (21 nov 2025)

- [x] **Implementar métricas e documentação de observabilidade**  
  `MetricsService`, instrumentação nos controllers, `METRICAS_GUIA.md`, `OBSERVABILIDADE_OPCOES.md` e ajuste dos testes quebrados.  
  `b81ee1c` — *test: corrige testes quebrados após implementação de observabilidade*

---

## Resumo por tipo

| Tipo | Quantidade (aprox.) |
|------|---------------------|
| Features | 12 |
| Fixes / refactors | 3 |
| Testes | 13 |
| Docs / dados / limpeza | 6 |

---

## Como este arquivo foi gerado

```bash
git log --reverse --pretty=format:'%h|%ad|%s' --date=short
```

As checkboxes estão marcadas porque as tasks já foram concluídas no histórico.  
Commits de merge foram omitidos da lista de tasks.
