# Task Hive API — guia de integração

Documentação prática da comunicação com o Task Hive em `http://orangepi.local:8080`, baseada no que funcionou ao criar o projeto **fipe-api**.

Use este arquivo como referência para popular outros repositórios (projetos, colunas Kanban e tarefas).

---

## Arquitetura (importante)

Há **três superfícies** distintas. Só uma serve para CRUD pelo cliente externo:

| Superfície | URL | Uso |
|---|---|---|
| UI / BFF (Next.js) | `http://orangepi.local:8080` | **Usar esta** para login e CRUD |
| Swagger / OpenAPI “rica” | `http://orangepi.local:8080/swagger` e `/api-json` | Documentação dos contratos Nest |
| Nest exposto | `http://orangepi.local:3001` | **Não usar** para CRUD externo (login 500; rotas protegidas 403; OpenAPI incompleta) |

O frontend **não** chama `/projects` nem `/tasks` direto no browser. Ele usa o BFF:

```text
/api/bff/*
```

O BFF autentica via cookie de sessão e faz proxy para a API Nest interna (mais nova que a de `:3001`).

O OpenAPI em `/api-json` descreve os paths Nest (`/projects`, `/project-stages`, `/tasks`, …). No BFF, o prefixo efetivo é:

```text
/api/bff/<mesmo-path-do-swagger>
```

Ex.: Swagger `POST /projects` → cliente `POST /api/bff/projects`.

---

## Autenticação

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "seu@email.com",
  "password": "sua-senha"
}
```

Resposta JSON (exemplo):

```json
{
  "user": {
    "id": "...",
    "name": "...",
    "email": "...",
    "role": "CLIENT"
  }
}
```

**O token não vem no body.** Ele vem no cookie:

```http
Set-Cookie: th_session=<JWT>; Path=/; HttpOnly; SameSite=lax
```

### Chamadas autenticadas

Enviar o cookie (e, se quiser, o mesmo JWT como Bearer):

```http
Cookie: th_session=<JWT>
Authorization: Bearer <JWT>
Accept: application/json
Content-Type: application/json
```

### Outros endpoints de auth do BFF

| Método | Path | Notas |
|---|---|---|
| `POST` | `/api/auth/login` | Cria sessão |
| `GET` | `/api/auth/me` | Usuário atual (401 se sem cookie) |
| `POST` | `/api/auth/logout` | Encerra sessão |

---

## Fluxo recomendado para documentar um repositório

1. Login → obter `th_session`
2. `POST /api/bff/projects` → criar projeto
3. `POST /api/bff/project-stages` (×3) → colunas Kanban
4. `PATCH /api/bff/project-stages/:id` → encadear `prevStageId` / `nextStageId`
5. `POST /api/bff/tasks` → criar tarefas na coluna desejada
6. `PATCH /api/bff/tasks/:id` → descrição / ordem
7. `POST /api/bff/tasks/:id/completions` → marcar como concluída

---

## Endpoints usados

Base: `http://orangepi.local:8080`

### Projetos

| Método | Path | Body |
|---|---|---|
| `GET` | `/api/bff/projects` | — |
| `POST` | `/api/bff/projects` | `{ "name": "...", "description": "..." }` |
| `PATCH` | `/api/bff/projects/:id` | parcial |
| `DELETE` | `/api/bff/projects/:id` | — |
| `GET` | `/api/bff/projects/:id/participants` | — |
| `POST` | `/api/bff/projects/:id/participants` | `{ "userId": "<uuid>" }` |
| `DELETE` | `/api/bff/projects/:id/participants/:userId` | — |

`name` é obrigatório. `companyOwnerId` é opcional (ver Swagger).

### Colunas (stages)

| Método | Path | Body |
|---|---|---|
| `GET` | `/api/bff/project-stages` | — |
| `GET` | `/api/bff/project-stages/project/:projectId` | — |
| `POST` | `/api/bff/project-stages` | `{ "name", "projectId", "order" }` |
| `PATCH` | `/api/bff/project-stages/:id` | `{ "name"?, "order"?, "nextStageId"?, "prevStageId"? }` |
| `DELETE` | `/api/bff/project-stages/:id` | — |

Kanban básico sugerido:

| name | order |
|---|---|
| A Fazer | 0 |
| Em Progresso | 1 |
| Concluído | 2 |

Depois encadear:

- A Fazer → `nextStageId = Em Progresso`
- Em Progresso → `prevStageId = A Fazer`, `nextStageId = Concluído`
- Concluído → `prevStageId = Em Progresso`

IDs de projeto/coluna/tarefa são **bigint em string** (ex.: `"739896941068029952"`).

### Tarefas

| Método | Path | Body |
|---|---|---|
| `GET` | `/api/bff/tasks` | — |
| `GET` | `/api/bff/tasks/stage/:stageId` | — |
| `POST` | `/api/bff/tasks` | `{ "name", "stageId" }` |
| `PATCH` | `/api/bff/tasks/:id` | `{ "name"?, "description"?, "order"?, "stageId"?, "finishDate"? }` |
| `POST` | `/api/bff/tasks/:id/completions` | — (marca concluída) |
| `DELETE` | `/api/bff/tasks/:id` | — |
| `PATCH` | `/api/bff/tasks/nextStage/:id` | move para próxima coluna |
| `PATCH` | `/api/bff/tasks/previousStage/:id` | move para coluna anterior |

`CreateTaskDto` só aceita `name` + `stageId`. Descrição e ordem vão no `PATCH` em seguida.

### Outros BFF observados no frontend

| Path | Uso |
|---|---|
| `/api/bff/users` | listar usuários |
| `/api/bff/to-do` | tarefas avulsas (não do quadro do projeto) |
| `/api/bff/tasks/:id/timetrack` | timer da tarefa |

---

## Exemplo mínimo (Python)

```python
import json, re, urllib.request, urllib.error

BASE = "http://orangepi.local:8080"

def login(email: str, password: str) -> str:
    req = urllib.request.Request(
        f"{BASE}/api/auth/login",
        data=json.dumps({"email": email, "password": password}).encode(),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req) as resp:
        cookie = resp.headers.get("Set-Cookie", "")
    return re.search(r"th_session=([^;]+)", cookie).group(1)

def api(method: str, path: str, token: str, body=None):
    data = None if body is None else json.dumps(body).encode()
    headers = {
        "Cookie": f"th_session={token}",
        "Authorization": f"Bearer {token}",
        "Accept": "application/json",
        "Content-Type": "application/json",
    }
    req = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req) as resp:
        raw = resp.read().decode()
        return json.loads(raw) if raw else None

token = login("seu@email.com", "sua-senha")

project = api("POST", "/api/bff/projects", token, {
    "name": "meu-repo",
    "description": "Descrição do projeto",
})
project_id = str(project["id"])

stages = []
for name, order in [("A Fazer", 0), ("Em Progresso", 1), ("Concluído", 2)]:
    stages.append(api("POST", "/api/bff/project-stages", token, {
        "name": name,
        "projectId": project_id,
        "order": order,
    }))

for i, stage in enumerate(stages):
    body = {}
    if i > 0:
        body["prevStageId"] = str(stages[i - 1]["id"])
    if i < len(stages) - 1:
        body["nextStageId"] = str(stages[i + 1]["id"])
    if body:
        api("PATCH", f"/api/bff/project-stages/{stage['id']}", token, body)

done_id = str(stages[2]["id"])
task = api("POST", "/api/bff/tasks", token, {
    "name": "Primeira tarefa",
    "stageId": done_id,
})
api("PATCH", f"/api/bff/tasks/{task['id']}", token, {
    "description": "Detalhes / commit / fase",
    "order": 0,
})
api("POST", f"/api/bff/tasks/{task['id']}/completions", token)
```

## Exemplo mínimo (curl)

```bash
# 1) Login e extrair cookie
curl -sS -c cookies.txt -X POST 'http://orangepi.local:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"seu@email.com","password":"sua-senha"}'

# 2) Criar projeto
curl -sS -b cookies.txt -X POST 'http://orangepi.local:8080/api/bff/projects' \
  -H 'Content-Type: application/json' \
  -d '{"name":"meu-repo","description":"Descrição"}'

# 3) Criar coluna
curl -sS -b cookies.txt -X POST 'http://orangepi.local:8080/api/bff/project-stages' \
  -H 'Content-Type: application/json' \
  -d '{"name":"A Fazer","projectId":"<PROJECT_ID>","order":0}'

# 4) Criar tarefa
curl -sS -b cookies.txt -X POST 'http://orangepi.local:8080/api/bff/tasks' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Minha task","stageId":"<STAGE_ID>}'
```

---

## Armadilhas

1. **Não usar `:3001` diretamente** para CRUD — API antiga/quebrada no host.
2. **Não usar `/api/projects`** — 404 no Next; o path certo é `/api/bff/projects`.
3. **Login do BFF não devolve `token` no JSON** — ler `Set-Cookie: th_session`.
4. **Swagger `servers.url` aponta para `http://localhost:3001`** — enganoso fora do container; ignore e use o BFF em `:8080`.
5. **IDs são bigint string** — sempre enviar como string JSON.
6. **Descrição da tarefa não entra no create** — criar e depois `PATCH`.
7. **Concluir ≠ mover de coluna** — `POST .../completions` marca `completedAt`; a coluna é `stageId` / nextStage.
8. **Evitar duplicar** — listar projetos/tarefas antes de recriar (`GET /api/bff/projects`, `GET /api/bff/tasks/stage/:id`).

---

## Referência OpenAPI

- UI: http://orangepi.local:8080/swagger  
- JSON: http://orangepi.local:8080/api-json  

Contratos úteis no JSON: `CreateProjectDto`, `CreateProjectStageDto`, `CreateTaskDto`, `UpdateTaskDto`, `AuthLoginDto`.

No cliente externo, prefixar com `/api/bff`.

---

## Caso fipe-api (já criado)

| Campo | Valor |
|---|---|
| Projeto | `fipe-api` |
| ID | `739896941068029952` |
| UI | http://orangepi.local:8080/projects |
| Colunas | A Fazer → Em Progresso → Concluído |
| Tarefas | 34 (histórico Git), todas em Concluído e marcadas concluídas |

Fonte das tarefas locais: `TASKS.md`.
