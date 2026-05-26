# Prompt 01 — Arquitetura e Planejamento

**Etapa:** Especificação e planejamento arquitetural  
**Padrão de prompting:** Chain of Thought + Role-based  
**Ferramenta:** Claude (Anthropic) / Kiro (AWS)  
**Data:** 2025-05-25

---

## Prompt Utilizado (Ciclo 1 — Chain of Thought + Role-based)

```
Você é um arquiteto de software sênior especializado em aplicações Java com Spring Boot 
e Vue.js, com foco em Clean Architecture e privacidade por design.

Preciso planejar uma aplicação chamada MedTrack com as seguintes características:
- Rastreador de medicamentos com alertas de interação
- Backend: Java 21, Spring Boot 3, Maven, PostgreSQL
- Frontend: Vue.js 3
- Deploy: Docker Compose local
- Dois perfis de usuário: ADMIN e USER
- Sem armazenamento de dados pessoais identificáveis (privacidade por design)

Antes de propor a arquitetura, raciocine passo a passo sobre:
1. Quais são os riscos de privacidade deste domínio?
2. Como a arquitetura em camadas deve ser estruturada?
3. Quais são os trade-offs entre usar uma API externa de interações (FDA) vs base local?
4. Como modelar o banco de dados respeitando os princípios de privacidade?

Depois do raciocínio, proponha:
- Diagrama de arquitetura em texto (ASCII)
- Modelo de dados com as principais entidades e relacionamentos
- Decisões arquiteturais registradas (ADRs)
```

---

## Saída Gerada pela IA

### Raciocínio (Chain of Thought)

**1. Riscos de privacidade no domínio:**
- Medicamentos revelam condições de saúde (HIV, câncer, doenças mentais) — dados sensíveis pela LGPD Art. 11
- Combinação de medicamentos pode identificar diagnósticos específicos
- E-mail + lista de medicamentos = vínculo identidade-saúde perigoso
- Solução: separar completamente identidade (UUID) de dados de saúde nas queries

**2. Arquitetura em camadas:**
- Controller → Service → Repository (padrão Spring Boot)
- DTOs para entrada/saída (nunca expor entidades JPA)
- GlobalExceptionHandler para tratamento uniforme de erros
- Security como cross-cutting concern (filtro JWT)
- Scheduler como componente independente

**3. Trade-offs FDA vs Base Local:**
| Critério | Apenas FDA | Apenas Local | Híbrido (escolhido) |
|----------|-----------|-------------|---------------------|
| Cobertura | Alta | Limitada | Alta |
| Disponibilidade | Depende internet | 100% | 100% (fallback) |
| Atualização | Automática | Manual | Automática + manual |
| Complexidade | Baixa | Baixa | Média |
| Latência | ~500ms | ~5ms | ~5ms (local first) |

**4. Modelo de banco com privacidade:**
- Tabela `users`: apenas UUID, email (para notificação), password_hash, role
- Sem campos: nome, CPF, endereço, telefone, data de nascimento
- Tabela `user_medications`: vincula user_id a medication_id sem contexto pessoal
- Queries de interação não fazem JOIN com dados de identidade

### Diagrama de Arquitetura Gerado

```
[Browser] → [Vue.js 3 SPA / Nginx :3000]
                    │
                    │ REST/JSON
                    ▼
         [Spring Boot 3 :8080]
         ├── Controllers (REST)
         ├── Services (lógica)
         ├── Repositories (JPA)
         ├── Security (JWT Filter)
         ├── Scheduler (@Scheduled)
         └── Integration (FDA Client)
                    │
                    ▼
         [PostgreSQL 15 :5432]
         ├── users
         ├── medications
         ├── interactions
         ├── user_medications
         └── system_config

Externos:
  [SMTP Server] ← NotificationService
  [openFDA API] ← FdaIntegrationService (feature flag)
```

### ADRs Gerados

A IA gerou 5 ADRs que foram documentados no README:
- ADR-001: Privacidade por Design
- ADR-002: Base de Interações Híbrida
- ADR-003: Scheduler para Notificações
- ADR-004: Autenticação JWT Stateless
- ADR-005: Docker Compose para Ambiente Local

---

## Avaliação Crítica

### Aceito sem alterações:
- Separação de identidade e dados de saúde (ADR-001) — princípio fundamental correto
- Abordagem híbrida para interações (ADR-002) — pragmática e resiliente
- Scheduler simples (ADR-003) — adequado para MVP, sem over-engineering
- Modelo de dados proposto — minimalista e focado em privacidade

### Ajustado:
- **ADR-004 (JWT)**: A IA sugeriu refresh token com rotação. Simplificamos para re-login no MVP, pois refresh token adiciona complexidade de implementação sem benefício claro para um projeto acadêmico.
- **Diagrama**: A IA gerou diagrama muito simplificado. Expandimos com detalhes das camadas internas e fluxo de dados.
- **Modelo de dados**: Adicionamos campo `source` na tabela interactions para distinguir dados locais de dados FDA.

### Rejeitado:
- Sugestão de usar Redis para cache de tokens — desnecessário para MVP com poucos usuários
- Sugestão de event sourcing para auditoria — complexidade desproporcional ao escopo

---

## Refinamentos Aplicados

**Ciclo 2 — Prompt de refinamento (detalhamento do modelo de dados):**
```
Com base na arquitetura proposta, detalhe o modelo de dados com:
- Tipos de dados específicos para cada campo (UUID, VARCHAR, ENUM, TIMESTAMP)
- Constraints e índices necessários
- Enums: Role (ADMIN, USER), Severity (MILD, MODERATE, SEVERE), FrequencyUnit
- Relacionamentos com cardinalidade explícita
- Considere que o campo 'source' em interactions deve indicar se veio da base local ou FDA

Mantenha o princípio de privacidade: nenhum campo que identifique pessoalmente o usuário.
```

**Ciclo 3 — Prompt com padrão Few-shot (ADRs no formato tabular):**
```
Reescreva os ADRs no formato tabular padronizado. Aqui está um exemplo do formato esperado:

### ADR-001: [Título]

| Campo | Valor |
|-------|-------|
| **Status** | Aceita |
| **Contexto** | [Descrição do problema/necessidade] |
| **Decisão** | [O que foi decidido] |
| **Consequência** | [Impacto positivo e negativo] |
| **Alternativas descartadas** | [O que foi considerado e por quê foi rejeitado] |

Aplique este formato para todos os 5 ADRs do MedTrack, mantendo o conteúdo técnico 
já definido mas adicionando o campo "Alternativas descartadas" com justificativa.
```

---

## Decisões Técnicas Documentadas

| Decisão | Justificativa | Referência |
|---------|---------------|-----------|
| UUID como identificador de usuário | Não sequencial, não revela informação | ADR-001 |
| E-mail separado de dados de saúde | Elimina vínculo identidade-condição | ADR-001 |
| Seed de 15+ interações críticas | Garante funcionamento offline | ADR-002 |
| Feature flag para FDA | Admin controla ativação sem deploy | ADR-002 |
| Cron a cada 1 minuto | Precisão aceitável sem mensageria | ADR-003 |
| JWT 24h sem refresh | Simplicidade para MVP | ADR-004 |
| Docker Compose 3 serviços | Reproduzível com um comando | ADR-005 |
