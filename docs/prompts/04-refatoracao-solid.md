# Prompt 04 — Refatoração SOLID com IA

**Etapa:** Refatoração guiada por IA com critério técnico  
**Padrão de prompting:** Role-based  
**Ferramenta:** Claude (Anthropic) / Kiro (AWS)  
**Data:** 2026-05-25

---

## Prompt Utilizado

```
Você é um arquiteto de software sênior especializado em Clean Code e princípios SOLID 
aplicados a Java com Spring Boot.

Analise o InteractionService abaixo e refatore seguindo os princípios SOLID. 
Documente qual princípio está sendo aplicado em cada mudança.

### Código Original (ANTES):

```java
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final MedicationRepository medicationRepository;

    public List<Interaction> findAll() {
        return interactionRepository.findAll();
    }

    public List<Interaction> findByMedicationId(Long medicationId) {
        return interactionRepository.findByMedicationId(medicationId);
    }

    @Transactional
    public Interaction create(Long medicationAId, Long medicationBId, Interaction interaction) {
        Medication medA = medicationRepository.findById(medicationAId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento A não encontrado"));
        Medication medB = medicationRepository.findById(medicationBId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento B não encontrado"));
        interaction.setMedicationA(medA);
        interaction.setMedicationB(medB);
        return interactionRepository.save(interaction);
    }

    @Transactional
    public void delete(Long id) {
        if (!interactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Interação não encontrada");
        }
        interactionRepository.deleteById(id);
    }
}
```

### Problemas identificados:
1. findByMedicationId consulta apenas a base local — viola OCP se quisermos adicionar FDA
2. Não há abstração para fontes de interação — viola DIP
3. O service mistura orquestração com acesso direto ao repositório — SRP parcial

### Requisitos da refatoração:
- Extrair interface InteractionChecker com método findInteractions(Long medicationId)
- Criar LocalInteractionChecker que implementa a interface
- Refatorar InteractionService para usar List<InteractionChecker> (Strategy pattern)
- Manter compatibilidade com AdminController e UserMedicationService
- Adicionar fallback: se uma fonte falhar, continuar com as outras
```

---

## Código ANTES (Original)

```java
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final MedicationRepository medicationRepository;

    public List<Interaction> findAll() {
        return interactionRepository.findAll();
    }

    public List<Interaction> findByMedicationId(Long medicationId) {
        return interactionRepository.findByMedicationId(medicationId);
    }

    @Transactional
    public Interaction create(Long medicationAId, Long medicationBId, Interaction interaction) {
        Medication medA = medicationRepository.findById(medicationAId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento A não encontrado"));
        Medication medB = medicationRepository.findById(medicationBId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento B não encontrado"));
        interaction.setMedicationA(medA);
        interaction.setMedicationB(medB);
        return interactionRepository.save(interaction);
    }

    @Transactional
    public void delete(Long id) {
        if (!interactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Interação não encontrada");
        }
        interactionRepository.deleteById(id);
    }
}
```

---

## Código DEPOIS (Refatorado)

### Interface extraída (DIP + OCP):

```java
public interface InteractionChecker {
    List<Interaction> findInteractions(Long medicationId);
    String getSource();
}
```

### Implementação local (SRP):

```java
@Component
@RequiredArgsConstructor
public class LocalInteractionChecker implements InteractionChecker {

    private final InteractionRepository interactionRepository;

    @Override
    public List<Interaction> findInteractions(Long medicationId) {
        return interactionRepository.findByMedicationId(medicationId);
    }

    @Override
    public String getSource() {
        return "LOCAL";
    }
}
```

### Service refatorado (orquestração):

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final MedicationRepository medicationRepository;
    private final List<InteractionChecker> interactionCheckers;

    public List<Interaction> findByMedicationId(Long medicationId) {
        List<Interaction> allInteractions = new ArrayList<>();
        for (InteractionChecker checker : interactionCheckers) {
            try {
                List<Interaction> found = checker.findInteractions(medicationId);
                allInteractions.addAll(found);
            } catch (Exception e) {
                log.warn("Erro ao consultar fonte '{}': {}", checker.getSource(), e.getMessage());
            }
        }
        return allInteractions;
    }

    // create() e delete() mantidos — responsabilidade de persistência local
}
```

---

## Princípios SOLID Aplicados

| Princípio | Aplicação |
|-----------|-----------|
| **S** — Single Responsibility | `LocalInteractionChecker` só busca interações locais. `InteractionService` só orquestra. |
| **O** — Open/Closed | Novas fontes (FDA, API externa) são adicionadas criando novo `InteractionChecker`, sem alterar código existente. |
| **L** — Liskov Substitution | Qualquer `InteractionChecker` pode ser substituído sem quebrar o contrato. |
| **I** — Interface Segregation | Interface `InteractionChecker` tem apenas 2 métodos focados. |
| **D** — Dependency Inversion | `InteractionService` depende da abstração `InteractionChecker`, não de `InteractionRepository` diretamente para busca. |

---

## Critério Técnico

- **Extensibilidade**: Para adicionar FDA como fonte, basta criar `FdaInteractionChecker implements InteractionChecker`
- **Resiliência**: Fallback com try-catch por fonte — se FDA falhar, local continua funcionando
- **Testabilidade**: Cada checker pode ser testado isoladamente com mocks
- **Compatibilidade**: API pública do `InteractionService` não mudou — `AdminController` e `UserMedicationService` continuam funcionando

---

## Avaliação Crítica

### Aceito da IA:
- Extração da interface `InteractionChecker` — padrão Strategy clássico
- Injeção de `List<InteractionChecker>` — Spring resolve automaticamente todas as implementações
- Fallback por fonte com logging

### Ajustado:
- A IA sugeriu criar `InteractionServiceFacade` separado — desnecessário, mantive o nome original para compatibilidade
- A IA sugeriu `@Order` nas implementações — removido, ordem não importa para agregação

### Rejeitado:
- Sugestão de usar `CompletableFuture` para consultas paralelas — over-engineering para MVP com apenas 1-2 fontes
