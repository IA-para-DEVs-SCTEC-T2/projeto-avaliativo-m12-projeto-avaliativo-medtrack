# Prompt 03 — Geração de Código Frontend (Cadastro de Medicamentos + Interações)

**Etapa:** Geração de código com IA — fluxo do usuário  
**Padrão de prompting:** Role-based + Few-shot  
**Ferramenta:** Claude (Anthropic) / Kiro (AWS)  
**Data:** 2026-05-25

---

## Ciclo 1 — Prompt Inicial (Role-based)

```
Você é um desenvolvedor fullstack sênior especializado em Spring Boot 3 e Vue.js 3 
com Composition API.

Implemente o fluxo principal do usuário no MedTrack:
1. Usuário cadastra um medicamento na sua lista pessoal
2. Sistema verifica interações com medicamentos já cadastrados
3. Se houver interações, exibe alerta modal com severidade e descrição
4. Disclaimer visível em todas as telas

### Backend (Spring Boot)
- UserMedicationController: GET /user-medications, POST /user-medications, DELETE /user-medications/{id}
- UserMedicationService: buscar por usuário (via email do JWT), adicionar com verificação de interações
- InteractionController: GET /interactions/medication/{id} (consulta pública autenticada)
- Response do POST deve incluir lista de interações detectadas

### Frontend (Vue.js 3)
- MedicationsView: lista medicamentos do usuário com botão remover
- AddMedicationView: formulário com select de medicamentos, dosagem, frequência, horário lembrete
- Modal de alerta de interação quando POST retorna interações
- Disclaimer "Este sistema NÃO substitui orientação médica" em todas as views

### Regras de negócio
- Verificar interações apenas com medicamentos que o PRÓPRIO usuário já usa
- Retornar severidade (MILD/MODERATE/SEVERE) e descrição
- Não bloquear o cadastro — apenas alertar
```

---

## Saída Gerada (Ciclo 1)

A IA gerou:
- `UserMedicationService` com lógica de verificação de interações
- `UserMedicationController` com 3 endpoints
- `InteractionController` para consulta
- Views Vue.js com formulário e modal de alerta
- DTOs: `UserMedicationRequest`, `UserMedicationResponse`, `InteractionAlertResponse`

---

## Avaliação Crítica

### Aceito:
- Fluxo de verificação: busca medicamentos do usuário → busca interações do novo → filtra matches
- Modal de alerta não-bloqueante (usuário pode dispensar e continuar)
- Disclaimer em todas as views
- Uso de `Authentication auth` para extrair email do JWT

### Ajustado:
- **Acesso ao medicamento de outro usuário**: A IA não validava ownership no DELETE. Adicionada verificação `um.getUser().getId().equals(user.getId())`
- **Select de medicamentos**: A IA usava input text. Alterado para select com lista do backend (melhor UX)
- **FrequencyUnit**: A IA usava string livre. Alterado para enum com select no frontend

### Rejeitado:
- Sugestão de WebSocket para alertas em tempo real — desnecessário para MVP

---

## Ciclo 2 — Few-shot (Padrão de resposta de interação)

```
Aqui está um exemplo do formato de resposta esperado quando o usuário cadastra um 
medicamento que tem interação:

Entrada: POST /user-medications
{
  "medicationId": 1,
  "dosage": "5mg",
  "frequencyValue": 1,
  "frequencyUnit": "DAILY",
  "reminderTime": "08:00"
}

Saída esperada (201 Created):
{
  "medication": {
    "id": 42,
    "medicationId": 1,
    "medicationName": "Varfarina",
    "dosage": "5mg",
    "frequencyValue": 1,
    "frequencyUnit": "DAILY",
    "reminderTime": "08:00"
  },
  "interactions": [
    {
      "id": 1,
      "medicationAName": "Varfarina",
      "medicationBName": "Aspirina",
      "severity": "SEVERE",
      "description": "Risco aumentado de sangramento",
      "source": "LOCAL"
    }
  ],
  "hasInteractions": true
}

Gere o InteractionAlertResponse record e ajuste o service para retornar neste formato.
```

---

## Decisões Técnicas

| Decisão | Justificativa |
|---------|---------------|
| Alerta não-bloqueante | Usuário decide se continua — sistema apenas informa |
| Verificação apenas com meds do usuário | Interações globais seriam ruído desnecessário |
| Email do JWT como identificador | Não expõe UUID interno na API |
| Select de medicamentos | Evita erros de digitação, melhor UX |
| Disclaimer obrigatório | Requisito legal — não é conselho médico |


---

## Ciclo 3 — MVP Funcional (Login, Cadastro, Header, Guards e Seed Admin)

**Data:** 2026-05-27
**Issue:** [#33 — feat: frontend MVP funcional + seed de admin](https://github.com/IA-para-DEVs-SCTEC-T2/projeto-avaliativo-medtrack/issues/33)
**Padrão de prompting:** Role-based + Chain of Thought
**Ferramenta:** Kiro

### Contexto

Após o primeiro ciclo, o frontend ficou com `LoginView`, `RegisterView` e `DashboardView` como stubs ("a ser implementada"). O usuário não tinha como autenticar pela UI, e não existia seed de admin no backend, embora `ADMIN_DEFAULT_PASSWORD` estivesse declarado no `.env`.

### Prompt Inicial

```
Você é um desenvolvedor fullstack sênior em Spring Boot 3 + Vue 3 (Composition API).

Implemente o frontend mínimo funcional do MedTrack para fechar o gap entre o backend
pronto e a UI atual em estado de esqueleto. Mantenha as decisões de privacidade
do projeto (sem PII além de e-mail) e o disclaimer médico obrigatório.

Backend
- AdminBootstrap (CommandLineRunner): cria um usuário ADMIN no primeiro start se nenhum
  admin existir, lendo ADMIN_DEFAULT_EMAIL (default admin@medtrack.local) e
  ADMIN_DEFAULT_PASSWORD do ambiente. Idempotente.
- application.yml expõe app.admin.default-email e app.admin.default-password.

Frontend
- LoginView e RegisterView com formulários reativos, integração com authService,
  exibição de erros e redirect pós-autenticação.
- AppHeader com brand, navegação (Início, Meus medicamentos, Painel admin condicional)
  e botão de logout. Renderizado apenas quando autenticado.
- DashboardView com cards apontando para as áreas principais.
- Pinia store auth.js: persiste token + email + role em localStorage e expõe
  isAuthenticated/isAdmin. Centraliza login/register/logout.
- Router: meta requiresAuth, requiresAdmin, guestOnly e guard global.
- api.js com interceptor que injeta Bearer e desloga em 401.
- Estilo unificado em assets/styles/main.css (tokens CSS, sem framework).

Regras
- Disclaimer visível em todas as telas relevantes (componente DisclaimerBanner).
- Telas admin (ManageMedications/Interactions/Users + SystemConfig) usando os
  endpoints existentes em /admin/**.
- Nada de PII além de e-mail.
```

### Saída e ajustes

A geração entregou o esqueleto correto. Ajustes manuais aplicados:

| Item | Razão |
|------|-------|
| `api.js` — interceptor de 401 redireciona para `/login` apenas se já não estiver lá | Evitar loop de redirect na própria tela de login |
| `auth.js` (store) — persistir `email` e `role` em `localStorage` | Sem isso, refresh perderia o estado e o guard `requiresAdmin` falharia |
| `router/index.js` — guard checa `to.meta.guestOnly && isAuthenticated` | Evita usuário logado abrir `/login` ou `/register` |
| `AddMedicationView` — listar medicamentos via `GET /medications` em vez de `/admin/medications` | Endpoint público (autenticado) — usuário comum não tem permissão no `/admin` |
| `SystemConfigView` — wirar com `/admin/fda/status`, `/admin/fda/toggle` e `/admin/fda/test-connection` | Endpoints já existentes no backend; trocar mock estático |
| `AdminBootstrap` — `if (defaultPassword == null \|\| isBlank())` | Não derrubar o boot quando a env não estiver definida (ex: ambiente de teste) |

### Decisões técnicas

| Decisão | Justificativa |
|---------|---------------|
| Guards no router (não em cada view) | Padrão Vue Router, ponto único de proteção |
| Persistência em `localStorage` (não `sessionStorage`) | UX: usuário não relogar a cada aba |
| Header fora do `<RouterView>` | Navegação consistente e evita re-render por rota |
| Tokens CSS em `:root` (sem framework) | Manter o stack enxuto conforme `tech.md` (PrimeVue/Vuetify ainda não foram adicionados) |
| Seed admin condicional via env | Permite ambientes (CI, testes) sem admin se a senha não for definida |

### Avaliação crítica

- **Aceito:** estrutura de auth, guards, persistência mínima, header com nav.
- **Ajustado:** todos os pontos da tabela acima.
- **Rejeitado:** sugestão da IA de adicionar refresh token rotation — fora do escopo
  do MVP (registrado em `Melhorias Futuras` do README).


---

## Ciclo 4 — FdaInteractionChecker (FDA como fonte adicional de interações)

**Data:** 2026-05-27
**Issue:** [#37 — feat(backend): plugar openFDA como fonte adicional de interações](https://github.com/IA-para-DEVs-SCTEC-T2/projeto-avaliativo-medtrack/issues/37)
**Padrão de prompting:** Role-based + Chain of Thought
**Ferramenta:** Kiro

> Ciclo é backend, mas mantenho a anotação aqui porque dá continuidade direta ao trabalho do Ciclo 3 (frontend MVP) — agora a flag de configuração que aparece em `SystemConfigView` ganha efeito real.

### Contexto

O ADR-002 prometia FDA como camada adicional ativável, e a refatoração SOLID (PR #25) já tinha exposto o `InteractionService` recebendo `List<InteractionChecker>`. Faltava o segundo implementador. Sem ele, a flag `FDA_INTEGRATION_ENABLED` só blindava o método `searchDrugLabel` — que ninguém chamava no fluxo real.

### Prompt Inicial

```
Você é um desenvolvedor backend Java sênior, focado em design por contratos.

Implemente FdaInteractionChecker que agregue interações vindas da Drug Label API
ao service InteractionService através do Strategy Pattern já existente.

Requisitos:
- @Component implementando InteractionChecker.
- findInteractions(medicationId): busca o nome no MedicationRepository, chama
  FdaApiClient.searchDrugLabel, extrai drug_interactions e faz matching contra
  o catálogo local (name + activeIngredient, case-insensitive).
- Interações em memória, source="FDA", severidade default MODERATE, descrição
  truncada a 500 chars.
- Retornar lista vazia se: medicamento não existe, FDA Optional.empty, FDA não
  cita nenhum item do catálogo.
- Não persiste nada — InteractionService apenas agrega.
- Cobertura unitária com Mockito: getSource, medicamento inexistente, flag
  desligada (Optional.empty), match por nome, match por active_ingredient,
  exclusão do próprio medicamento, ausência de match no catálogo, truncamento.

Não inventar campos: respeitar o que já existe em FdaSearchResponse.
```

### Saída e ajustes

| Item | Razão |
|------|-------|
| `mentions(haystackLower, candidate)` separado do loop principal | Legibilidade e isolamento da regra de matching |
| `truncate` com `length() <= max` antes de cortar | Evitar `StringIndexOutOfBoundsException` quando description é exatamente o tamanho máximo |
| `findAll()` no checker (não no service) | Local Checker já busca por ID; manter cada checker independente para preservar OCP |
| Excluir o próprio `medicationId` do catálogo no loop | Sem isso, "Varfarina interactions described here" causaria self-match |
| Severidade `MODERATE` default | Drug Label API não tem severidade; documentado em ADR-002 |

### Decisões técnicas

| Decisão | Justificativa |
|---------|---------------|
| Não persistir interações da FDA | Evita cache stale e duplicação. A FDA pode atualizar bula e nossa base ficaria divergente. |
| Matching por substring case-insensitive | Suficiente para MVP. Risco de falso positivo aceitável dado que o usuário sempre vê o `source` e a `description`. |
| `Severity.MODERATE` default | Conservador. Subestimar é menos perigoso que superestimar (usuário ignora severo demais). |
| Tolerância a falha total na FDA | `FdaApiClient` já retorna `Optional.empty` em qualquer erro/timeout. O service também pega exceção do checker e segue com os outros. Failure does not propagate. |

### Avaliação crítica

- **Aceito:** estrutura por Strategy, matching por nome + active_ingredient, agregação no service.
- **Ajustado:** todos os pontos da tabela acima.
- **Rejeitado:** sugestão de cachear respostas da FDA com `@Cacheable` — desnecessário no MVP, ficou registrado em "Melhorias Futuras".
