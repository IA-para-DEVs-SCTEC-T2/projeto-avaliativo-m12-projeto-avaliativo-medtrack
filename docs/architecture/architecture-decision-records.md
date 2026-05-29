# Architecture Decision Records (ADRs) — MedTrack

## Índice

1. [ADR-001: Privacidade por Design](#adr-001-privacidade-por-design)
2. [ADR-002: Base de Interações Híbrida](#adr-002-base-de-interações-híbrida)
3. [ADR-003: Scheduler para Notificações](#adr-003-scheduler-para-notificações)
4. [ADR-004: Autenticação JWT Stateless](#adr-004-autenticação-jwt-stateless)
5. [ADR-005: Docker Compose para Ambiente Local](#adr-005-docker-compose-para-ambiente-local)

---

## ADR-001: Privacidade por Design

| Campo | Valor |
|-------|-------|
| **Status** | Aceita |
| **Data** | 2025-05-25 |
| **Contexto** | Medicamentos revelam condições de saúde (HIV, câncer, doenças mentais), classificando-os como dados sensíveis pela LGPD (Art. 11). A combinação de medicamentos pode identificar diagnósticos específicos. |
| **Decisão** | Não armazenar dados pessoais identificáveis. O usuário é identificado apenas por UUID interno. O e-mail é armazenado exclusivamente para envio de notificações e não é vinculado a informações de saúde nas queries do banco. |
| **Consequência** | Sem funcionalidade "esqueci minha senha por CPF" nem relatórios nominais. Recuperação de acesso exclusivamente via e-mail de reset. Elimina necessidade de base legal específica para tratamento de dados sensíveis. |
| **Alternativas descartadas** | (1) Criptografia de dados pessoais — adiciona complexidade sem eliminar o risco de vazamento; (2) Anonimização — difícil garantir irreversibilidade com dataset pequeno; (3) Consentimento explícito — burocrático e não elimina o risco. |

---

## ADR-002: Base de Interações Híbrida

| Campo | Valor |
|-------|-------|
| **Status** | Aceita |
| **Data** | 2025-05-25 |
| **Contexto** | A API do RxNav descontinuou o endpoint de interações em janeiro de 2024. A openFDA é confiável mas depende de conectividade externa. O sistema precisa funcionar offline. |
| **Decisão** | Manter tabela local de interações críticas (seed via Flyway com 15+ interações conhecidas) e integrar openFDA como camada adicional ativável por feature flag no banco, gerenciável pelo admin. |
| **Consequência** | O admin deve manter a base local atualizada. A integração FDA é explicitamente opcional e documentada como tal. Funcionamento garantido mesmo sem internet. |
| **Alternativas descartadas** | (1) Apenas FDA — falha sem internet, single point of failure; (2) Apenas local — cobertura limitada a interações conhecidas; (3) RxNav — API descontinuada em 2024. |

---

## ADR-003: Scheduler para Notificações

| Campo | Valor |
|-------|-------|
| **Status** | Aceita |
| **Data** | 2025-05-25 |
| **Contexto** | Necessidade de enviar lembretes de dose no horário correto. Sistemas de mensageria (RabbitMQ, Kafka) aumentariam complexidade desnecessariamente para o escopo do projeto. |
| **Decisão** | Usar Spring @Scheduled com cron rodando a cada minuto para verificar medicamentos com lembrete pendente. Envio via JavaMailSender (SMTP). |
| **Consequência** | Precisão de ±1 minuto nos lembretes. Aceitável para o domínio médico (não é emergência). Sem dependência de infraestrutura adicional. |
| **Alternativas descartadas** | (1) RabbitMQ — complexidade de infraestrutura desnecessária para MVP; (2) Cron externo — acoplamento com sistema operacional; (3) WebSocket push — requer conexão ativa do usuário no browser. |

---

## ADR-004: Autenticação JWT Stateless

| Campo | Valor |
|-------|-------|
| **Status** | Aceita |
| **Data** | 2025-05-25 |
| **Contexto** | Aplicação SPA (Vue.js) precisa de autenticação sem sessão server-side. Necessidade de escalar horizontalmente sem compartilhar estado. |
| **Decisão** | JWT com expiração de 24h, roles ADMIN/USER embutidas no token, sem refresh token no MVP (re-login após expiração). |
| **Consequência** | Não é possível invalidar tokens individuais antes da expiração. Aceitável para MVP acadêmico. Simplicidade de implementação. |
| **Alternativas descartadas** | (1) Session-based — não escala com SPA, requer sticky sessions; (2) OAuth2 externo (Google, GitHub) — complexidade desnecessária, dependência externa; (3) Refresh token com rotação — complexidade adicional sem benefício claro para MVP. |

---

## ADR-005: Docker Compose para Ambiente Local

| Campo | Valor |
|-------|-------|
| **Status** | Aceita |
| **Data** | 2025-05-25 |
| **Contexto** | Necessidade de ambiente reproduzível para desenvolvimento e avaliação acadêmica. Avaliador deve conseguir rodar com um único comando. |
| **Decisão** | Docker Compose com 3 serviços: PostgreSQL 15 (banco), Spring Boot (backend na porta 8080), Nginx (frontend na porta 3000). Todas as configurações via .env. |
| **Consequência** | Um único `docker compose up --build` sobe todo o ambiente. Requer Docker instalado na máquina do avaliador. |
| **Alternativas descartadas** | (1) Kubernetes — overkill para desenvolvimento local e avaliação; (2) Instalação manual — não reproduzível, sujeita a "funciona na minha máquina"; (3) Cloud deploy — custo e complexidade desnecessários. |
