# Prompt 02 — Geração de Código Backend (Autenticação JWT)

**Etapa:** Geração de código com IA — módulo de autenticação  
**Padrão de prompting:** Role-based + Chain of Thought  
**Ferramenta:** Claude (Anthropic) / Kiro (AWS)  
**Data:** 2025-05-25

---

## Ciclo 1 — Prompt Inicial (Role-based)

```
Você é um desenvolvedor Java sênior especializado em Spring Boot 3 e Spring Security.

Implemente o módulo de autenticação JWT para o projeto MedTrack com as seguintes especificações:

### Contexto
- Java 21, Spring Boot 3.3.5
- PostgreSQL com tabela users já criada (UUID, email, password_hash, role, active)
- Roles: ADMIN e USER
- JWT com expiração de 24h, sem refresh token

### Requisitos
1. JwtService:
   - Gerar token com email como subject e role como claim
   - Validar token (expiração + assinatura)
   - Extrair email e role do token
   - Usar jjwt 0.12.x com chave HMAC-SHA256

2. JwtAuthFilter (OncePerRequestFilter):
   - Extrair token do header Authorization: Bearer <token>
   - Validar e popular SecurityContext com authorities
   - Ignorar rotas públicas

3. AuthService:
   - register: validar email único, hash senha com BCrypt, salvar, retornar token
   - login: buscar por email, verificar senha, verificar conta ativa, retornar token

4. AuthController:
   - POST /auth/register — body: {email, password} → 201 + token
   - POST /auth/login — body: {email, password} → 200 + token

5. SecurityConfig:
   - Stateless, CORS habilitado para localhost:3000
   - Rotas públicas: /auth/**, /swagger-ui/**, /api-docs/**
   - Rotas admin: /admin/** → ROLE_ADMIN
   - Demais rotas: authenticated

6. DTOs com Bean Validation:
   - RegisterRequest: @Email, @NotBlank, @Size(min=6)
   - LoginRequest: @Email, @NotBlank
   - AuthResponse: record(token, email, role)

Use records para DTOs, Lombok para entidades, e trate erros com mensagens claras.
```

---

## Saída Gerada (Ciclo 1)

A IA gerou corretamente:
- `JwtService` com geração/validação de tokens usando jjwt 0.12.x
- `JwtAuthFilter` como OncePerRequestFilter
- `AuthService` com register e login
- `AuthController` com endpoints REST
- `SecurityConfig` com CORS e filtro JWT
- DTOs como Java records com Bean Validation

---

## Avaliação Crítica (Ciclo 1)

### Aceito:
- Estrutura geral do fluxo JWT (filter → service → controller)
- Uso de records para DTOs (imutáveis, concisos)
- BCrypt para hash de senha
- Tratamento de conta desativada no login

### Ajustado:
- **JWT Secret**: A IA usou string plain text. Ajustado para Base64-encoded para compatibilidade com `Decoders.BASE64`
- **Error handling**: A IA retornava 500 para email duplicado. Ajustado para 400 com mensagem clara via `IllegalArgumentException`
- **CORS**: A IA não incluiu CORS. Adicionado para permitir frontend em localhost:3000

### Rejeitado:
- Sugestão de UserDetailsService customizado — desnecessário quando populamos o SecurityContext diretamente no filtro

---

## Ciclo 2 — Refinamento (Segurança)

```
Revise o código de autenticação considerando:
1. O que acontece se o token JWT for malformado ou expirado?
2. Como evitar timing attacks na comparação de senhas?
3. O filtro JWT deve logar erros ou falhar silenciosamente?

Ajuste o JwtAuthFilter para:
- Capturar qualquer exceção de parsing do token (malformado, expirado, assinatura inválida)
- Continuar a chain sem autenticação (fail-open para rotas públicas)
- Não expor detalhes do erro ao cliente
```

**Resultado:** JwtAuthFilter ajustado com try-catch genérico que permite a request continuar sem autenticação. Spring Security nega acesso nas rotas protegidas naturalmente.

---

## Ciclo 3 — Role-based Prompting (Revisão de Segurança)

```
Você é um especialista em segurança de aplicações web (OWASP Top 10).

Revise o módulo de autenticação JWT do MedTrack e identifique:
1. Vulnerabilidades potenciais
2. Melhorias de segurança recomendadas
3. Conformidade com boas práticas OWASP

Considere: o projeto é um MVP acadêmico, então priorize correções críticas sobre melhorias nice-to-have.
```

**Resultado da revisão:**
- ✅ BCrypt com salt automático (proteção contra rainbow tables)
- ✅ JWT stateless sem sessão server-side
- ✅ Mensagem genérica "Credenciais inválidas" (não revela se email existe)
- ⚠️ Recomendação: adicionar rate limiting no login (não implementado no MVP)
- ⚠️ Recomendação: refresh token com rotação (adiado para pós-MVP)

---

## Decisões Técnicas

| Decisão | Justificativa |
|---------|---------------|
| JWT no header Authorization | Padrão REST, compatível com SPA |
| BCrypt (strength 10) | Padrão seguro, performance aceitável |
| Fail-open no filtro JWT | Rotas públicas funcionam sem token |
| Mensagem genérica no login | Não revela existência de email (OWASP) |
| Records para DTOs | Imutáveis, menos boilerplate, Java 21 |
| Base64 para JWT secret | Compatibilidade com jjwt Decoders |
