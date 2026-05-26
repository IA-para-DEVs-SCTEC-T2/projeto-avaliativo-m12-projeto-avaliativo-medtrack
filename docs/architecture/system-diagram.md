# MedTrack — Diagrama de Sistema

## Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                           INFRAESTRUTURA                            │
│                        (Docker Compose)                             │
│                                                                     │
│  ┌─────────────────┐  ┌─────────────────────┐  ┌───────────────┐  │
│  │   Frontend      │  │      Backend        │  │  PostgreSQL   │  │
│  │   (Nginx)       │  │   (Spring Boot)     │  │    15         │  │
│  │   :3000         │  │      :8080          │  │   :5432       │  │
│  └────────┬────────┘  └──────────┬──────────┘  └───────┬───────┘  │
│           │                      │                      │          │
└───────────┼──────────────────────┼──────────────────────┼──────────┘
            │                      │                      │
            │    HTTP/REST         │     JDBC             │
            └──────────────────────┘──────────────────────┘
```

## Arquitetura em Camadas (Backend)

```
┌─────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                  │
│                                                     │
│  AuthController    MedicationController             │
│  UserMedicationController    AdminController        │
│  InteractionController       FdaController          │
│                                                     │
│  ← @RestController, @Valid, DTOs request/response   │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│                    BUSINESS LAYER                     │
│                                                     │
│  AuthService           MedicationService            │
│  UserMedicationService InteractionService           │
│  NotificationService   NotificationScheduler        │
│  FdaIntegrationService AdminService                 │
│                                                     │
│  ← @Service, @Transactional, regras de negócio     │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│                  PERSISTENCE LAYER                    │
│                                                     │
│  UserRepository          MedicationRepository       │
│  UserMedicationRepository InteractionRepository     │
│  SystemConfigRepository                             │
│                                                     │
│  ← @Repository, Spring Data JPA, Flyway migrations │
└──────────────────────┬──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│                    DATABASE                           │
│                                                     │
│  PostgreSQL 15 — 5 tabelas principais               │
│  Migrations: V1 a V6 (Flyway)                      │
└─────────────────────────────────────────────────────┘
```

## Cross-Cutting Concerns

```
┌─────────────────────────────────────────────────────┐
│                  SECURITY                            │
│  SecurityConfig → JwtAuthFilter → JwtService        │
│  Roles: ADMIN, USER                                 │
│  Rotas públicas: /auth/register, /auth/login        │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              ERROR HANDLING                           │
│  GlobalExceptionHandler (@ControllerAdvice)          │
│  ResourceNotFoundException (404)                     │
│  InteractionConflictException (409)                  │
│  ValidationException (400)                           │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              SCHEDULING                              │
│  NotificationScheduler (@Scheduled cron)            │
│  Execução: a cada 1 minuto                          │
│  Verifica: user_medications com reminder pendente   │
│  Ação: envia e-mail via NotificationService         │
└─────────────────────────────────────────────────────┘
```

## Integrações Externas

```
┌─────────────────────────────────────────────────────┐
│              SMTP (Notificações)                     │
│                                                     │
│  Spring Mail → JavaMailSender                       │
│  Produção: Gmail (porta 587, TLS)                   │
│  Desenvolvimento: Mailtrap                          │
│  Templates: lembrete de dose, alerta de interação   │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              openFDA API (Feature Flag)              │
│                                                     │
│  Endpoint: https://api.fda.gov/drug/label.json      │
│  Sem API key necessária                             │
│  Timeout: 3 segundos                                │
│  Fallback: retorna apenas interações locais         │
│  Controle: system_config.FDA_INTEGRATION_ENABLED    │
└─────────────────────────────────────────────────────┘
```

## Fluxo de Verificação de Interações

```
Usuário cadastra medicamento
         │
         ▼
InteractionService.checkInteractions(userId, newMedicationId)
         │
         ├──▶ Busca interações na BASE LOCAL
         │         │
         │         ▼
         │    Retorna matches locais (severity: MILD/MODERATE/SEVERE)
         │
         ├──▶ FDA habilitada? (feature flag)
         │         │
         │         ├── NÃO → retorna apenas locais
         │         │
         │         └── SIM → FdaApiClient.searchInteractions()
         │                      │
         │                      ├── Timeout (3s) → fallback local
         │                      │
         │                      └── Sucesso → merge resultados
         │
         ▼
Retorna lista unificada de interações
         │
         ├── Se SEVERE → alerta imediato + e-mail
         ├── Se MODERATE → alerta na UI
         └── Se MILD → informativo na UI
```
