# Prompt 08 — Análise Crítica de Saída Incorreta da IA

**Etapa:** Avaliação e correção de saída gerada  
**Padrão de prompting:** Iterativo com feedback explícito  
**Ferramenta:** Kiro (AWS)  
**Data:** 2026-05-25

---

## Contexto

Este documento registra um caso em que a saída gerada pela IA precisou ser identificada como incorreta, corrigida e documentada — conforme exigido pelo critério 15 da rubrica do projeto.

---

## Problema Identificado

**O que foi pedido:** Geração da lógica de cálculo da próxima dose em `NotificationScheduler.java`

**O que a IA gerou:**
```java
// Código gerado pela IA (INCORRETO)
public LocalDateTime calculateNextDose(UserMedication med) {
    if (med.getLastTakenAt() == null) {
        return med.getStartDatetime();
    }
    return med.getLastTakenAt().plusHours(med.getFrequencyValue());
}
```

**O problema:** O código assumia que a unidade de frequência era sempre `HOUR`, ignorando completamente o enum `FrequencyUnit` que contempla `HOUR`, `DAY` e `WEEK`. Medicamentos com frequência diária ou semanal teriam o horário de próxima dose calculado completamente errado.

**Como foi identificado:** Durante a revisão manual do código gerado, ao comparar a implementação com os requisitos definidos em `specs/notifications/requirements.md`. O campo `frequencyUnit` estava sendo ignorado sem nenhum aviso ou comentário da IA.

---

## Correção Aplicada

**Prompt de correção:**
```
O código que você gerou para calculateNextDose está incorreto. Ele ignora o campo 
frequencyUnit e sempre soma horas. Corrija a implementação para suportar os três 
casos do enum FrequencyUnit: HOUR, DAY e WEEK. Use um switch expression do Java 21.
Além disso, garanta que se nextDose < NOW(), o método calcule o próximo horário 
futuro a partir de startDatetime (não apenas somar um período ao passado).
```

**Código corrigido:**
```java
public LocalDateTime calculateNextDose(UserMedication med) {
    LocalDateTime base = med.getLastTakenAt() != null 
        ? med.getLastTakenAt() 
        : med.getStartDatetime();
    
    LocalDateTime next = base.plus(switch (med.getFrequencyUnit()) {
        case HOUR  -> Duration.ofHours(med.getFrequencyValue());
        case DAY   -> Duration.ofDays(med.getFrequencyValue());
        case WEEK  -> Duration.ofDays(med.getFrequencyValue() * 7L);
    });
    
    // Se a próxima dose calculada já passou, avança até o próximo horário futuro
    LocalDateTime now = LocalDateTime.now();
    while (next.isBefore(now)) {
        next = next.plus(switch (med.getFrequencyUnit()) {
            case HOUR  -> Duration.ofHours(med.getFrequencyValue());
            case DAY   -> Duration.ofDays(med.getFrequencyValue());
            case WEEK  -> Duration.ofDays(med.getFrequencyValue() * 7L);
        });
    }
    return next;
}
```

---

## Lição Aprendida

A IA gerou código funcionalmente parcial sem sinalizar a limitação. O campo estava no modelo de dados e no enum, mas a IA não mapeou o relacionamento entre eles na lógica de cálculo. **Revisão humana do código gerado é obrigatória**, especialmente em lógica de negócio com múltiplos casos condicionais. Nunca assumir que a ausência de erros de compilação equivale à corretude lógica.

**Boa prática identificada:** Especificar no prompt todos os casos de enum que devem ser tratados, com exemplos concretos, evita esse tipo de saída parcial.
