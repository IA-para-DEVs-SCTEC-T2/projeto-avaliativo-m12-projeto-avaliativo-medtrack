# Prompt 03 — Geração de Código Frontend (Cadastro de Medicamentos + Interações)

**Etapa:** Geração de código com IA — fluxo do usuário  
**Padrão de prompting:** Role-based + Few-shot  
**Ferramenta:** Claude (Anthropic) / Kiro (AWS)  
**Data:** 2025-05-25

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
