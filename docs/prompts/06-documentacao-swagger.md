# Prompt 06 — Documentação Swagger/OpenAPI com IA

**Etapa:** Geração de documentação automática da API  
**Padrão de prompting:** Role-based  
**Ferramenta:** Claude (Anthropic) / Kiro (AWS)  
**Data:** 2025-05-25

---

## Prompt Utilizado

```
Você é um desenvolvedor Java sênior especializado em documentação de APIs REST 
com Springdoc OpenAPI (Swagger).

Adicione anotações OpenAPI completas nos controllers do MedTrack:

### Controllers a documentar:
1. AuthController — /auth/register, /auth/login
2. MedicationController — /medications (listagem pública)
3. UserMedicationController — /user-medications (CRUD do usuário)
4. AdminController — /admin/** (CRUD admin)
5. InteractionController — /interactions/medication/{id}
6. FdaController — /admin/fda/** (integração FDA)

### Para cada endpoint, adicione:
- @Operation com summary e description
- @ApiResponses com todos os códigos HTTP possíveis
- @Tag para agrupar endpoints por domínio
- Exemplos de request/response quando relevante

### Configuração global (já existente):
- SwaggerConfig com Bearer Authentication
- Swagger UI em /swagger-ui.html
- API docs em /api-docs

Mantenha as anotações concisas e em português quando possível.
```

---

## Saída Gerada

A IA adicionou anotações em todos os 6 controllers:
- `@Tag` para agrupamento por domínio
- `@Operation` com summary e description
- `@ApiResponses` com códigos 200, 201, 400, 401, 404, 409

---

## Avaliação Crítica

### Aceito:
- Agrupamento por @Tag (Autenticação, Medicamentos, Admin, FDA)
- @ApiResponses com códigos HTTP corretos para cada endpoint
- Descriptions claras e concisas

### Ajustado:
- A IA adicionou `@Schema` em todos os DTOs — removido dos records (Java já expõe a estrutura)
- A IA usou `@io.swagger.v3.oas.annotations.parameters.RequestBody` — desnecessário com Spring Boot

### Rejeitado:
- Sugestão de exemplos hardcoded em cada response — polui o código, Swagger gera automaticamente

---

## Endpoints Documentados

| Controller | Endpoints | Tag |
|-----------|-----------|-----|
| AuthController | POST /auth/register, POST /auth/login | Autenticação |
| MedicationController | GET /medications, GET /medications/{id} | Medicamentos |
| UserMedicationController | GET/POST/DELETE /user-medications | Medicamentos do Usuário |
| AdminController | CRUD /admin/medications, /admin/interactions, /admin/users | Admin |
| InteractionController | GET /interactions/medication/{id} | Interações |
| FdaController | GET/POST /admin/fda/** | FDA Integration |

---

## Acesso

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **Autenticação**: Clicar em "Authorize" e inserir `Bearer <token>`
