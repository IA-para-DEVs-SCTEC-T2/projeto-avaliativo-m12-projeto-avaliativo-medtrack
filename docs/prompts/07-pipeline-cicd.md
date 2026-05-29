# Prompt 07 — Pipeline CI/CD com GitHub Actions

**Etapa:** Configuração de integração contínua e entrega contínua  
**Padrão de prompting:** Role-based + Chain of Thought  
**Ferramenta:** Claude (Anthropic) / Kiro (AWS)  
**Data:** 2025-01-XX

---

## Prompt Utilizado

```
Você é um engenheiro DevOps sênior especializado em GitHub Actions, pipelines CI/CD 
para projetos Java Spring Boot + Vue.js, e boas práticas de segurança em repositórios.

Preciso configurar um pipeline CI/CD para o projeto MedTrack com as seguintes características:

### Contexto do Projeto
- Backend: Java 21, Spring Boot 3, Maven, PostgreSQL 15
- Frontend: Vue.js 3, Vite, Node 20
- Infraestrutura: Docker Compose
- Branches: main (produção), develop (integração), feature/** (desenvolvimento)
- Regra: PRs sempre para develop, nunca direto para main

### Requisitos do Pipeline
1. Job Backend:
   - Setup Java 21 (Temurin)
   - Service container PostgreSQL 15 para testes
   - Lint com Checkstyle (Google Java Style)
   - Execução de testes com Maven
   - Build do artefato

2. Job Frontend:
   - Setup Node.js 20
   - Instalação de dependências com npm ci
   - Lint com ESLint
   - Build de produção com Vite

3. Job Segurança:
   - Verificar que nenhum arquivo .env foi commitado no repositório
   - Falhar o pipeline se encontrar secrets expostos

4. Triggers:
   - Push em: main, develop, feature/**, docs/**
   - Pull requests apenas para: develop (reforçar fluxo via develop)

Raciocine passo a passo sobre:
1. Por que usar service containers ao invés de docker-compose no CI?
2. Quais variáveis de ambiente são necessárias para os testes rodarem isolados?
3. Como garantir que o cache de dependências funcione corretamente?
4. Qual a melhor estratégia para impedir que secrets vazem no repositório?

Gere o arquivo .github/workflows/ci.yml completo e comentado.
```

---

## Saída Gerada pela IA

A IA gerou o workflow completo com 3 jobs paralelos:

### Job `backend`
- PostgreSQL 15 como service container com health checks
- Java 21 Temurin com cache Maven
- Checkstyle para lint
- Testes com variáveis de ambiente isoladas (DB, JWT, SMTP mock)
- Build com skip de testes (já executados)

### Job `frontend`
- Node.js 20 com cache npm
- `npm ci` para instalação determinística
- ESLint para lint
- Build com variável `VITE_API_BASE_URL`

### Job `security-check`
- Verifica via `git ls-files` se algum `.env` foi commitado
- Falha o pipeline com mensagem clara se encontrar

---

## Avaliação Crítica

### Aceito sem alterações:
- Estrutura dos 3 jobs paralelos (backend, frontend, security)
- Uso de service container PostgreSQL com health checks
- Cache de Maven e npm configurados corretamente
- Variáveis de ambiente para testes isoladas e sem valores reais

### Ajustado:
- **PR triggers**: Removido `main` dos targets de PR. O fluxo correto é sempre 
  feature → develop → main. PRs diretos para main não devem ser incentivados pelo CI.
- **Motivo**: Reforçar a política de branching do projeto onde `develop` é o ponto 
  de integração obrigatório.

### Observações:
- O job de segurança verifica apenas `.env` no root. Poderia ser expandido para 
  detectar patterns de secrets (API keys, tokens) em qualquer arquivo, mas para o 
  MVP atual é suficiente.
- O `--no-transfer-progress` no Maven reduz ruído nos logs do CI.

---

## Refinamentos Aplicados

**Ciclo 2 — Ajuste de triggers:**
```
Ajuste o workflow para que pull_requests sejam aceitos apenas para a branch develop.
O fluxo do projeto exige que todo código passe por develop antes de ir para main.
Remova 'main' da lista de branches em pull_request triggers.
```

**Resultado:** Trigger de PR alterado de `branches: [ main, develop ]` para `branches: [ develop ]`.

---

## Decisões Técnicas

| Decisão | Justificativa |
|---------|---------------|
| Service container vs docker-compose | Mais rápido no CI, sem overhead de build de imagens |
| Cache Maven/npm | Reduz tempo de build em ~60% nas execuções subsequentes |
| Jobs paralelos | Backend e frontend são independentes, rodam simultaneamente |
| Variáveis de teste hardcoded | Valores fake apenas para CI, sem risco de vazamento |
| PR apenas para develop | Reforça política de branching e code review |

---

## Arquivo Gerado

Localização: `.github/workflows/ci.yml`
