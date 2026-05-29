# Prompt 05 — Geração de Testes com IA

**Etapa:** Geração de testes automatizados  
**Padrão de prompting:** Few-shot  
**Ferramenta:** Claude (Anthropic) / Kiro (AWS)  
**Data:** 2026-05-25

---

## Prompt Utilizado (Few-shot)

```
Você é um engenheiro de qualidade sênior especializado em testes Java com JUnit 5, 
Mockito e Spring Boot Test.

Gere testes para o projeto MedTrack seguindo este padrão:

### Exemplo de teste unitário (padrão esperado):

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("MedicationService — Testes Unitários")
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;

    @Test
    @DisplayName("findById deve retornar medicamento existente")
    void findById_shouldReturnExistingMedication() {
        Medication med = Medication.builder().id(1L).name("Varfarina").build();
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(med));

        Medication result = medicationService.findById(1L);

        assertThat(result.getName()).isEqualTo("Varfarina");
    }
}
```

### Gere testes para:

1. **InteractionService** (unitário):
   - findByMedicationId agrega resultados de todos os checkers
   - findByMedicationId continua se um checker falhar (fallback)
   - create associa medicamentos e salva
   - create lança exceção se medicamento não existe
   - delete lança exceção se interação não existe

2. **NotificationService** (unitário com mock do JavaMailSender):
   - sendDoseReminder envia e-mail com dados corretos
   - sendDoseReminder trata dosagem null
   - sendInteractionAlert inclui severidade no corpo
   - Exceção do mailSender não propaga

3. **AuthController** (integração com MockMvc):
   - POST /auth/register retorna 201 com token
   - POST /auth/register retorna 400 para email inválido
   - POST /auth/login retorna 200 com token
   - POST /auth/login retorna 400 para credenciais inválidas

4. **AuthService** (unitário):
   - register cria usuário e retorna token
   - register lança exceção se email já existe
   - login retorna token para credenciais válidas
   - login lança exceção para senha incorreta
   - login lança exceção para conta desativada

Use AssertJ para assertions, @DisplayName em português, e nomes de método descritivos.
```

---

## Saída Gerada

A IA gerou 4 classes de teste cobrindo:
- 6 testes para InteractionService
- 4 testes para NotificationService
- 5 testes para AuthService
- 5 testes para AuthController

Total: **20 testes** cobrindo happy path + casos de erro.

---

## Avaliação Crítica

### Aceito:
- Estrutura com @ExtendWith(MockitoExtension.class) para unitários
- @WebMvcTest para testes de controller (slice test)
- ArgumentCaptor para verificar conteúdo do e-mail
- AssertJ para assertions fluentes

### Ajustado:
- **AuthControllerTest**: A IA não mockava o JwtService (necessário para SecurityConfig). Adicionado @MockBean
- **NotificationServiceTest**: A IA testava retorno void com assertEquals. Corrigido para verify()
- **Nomes**: Padronizados para `metodo_cenario_resultado` em inglês com @DisplayName em português

### Rejeitado:
- Sugestão de @SpringBootTest para testes unitários — pesado demais, @ExtendWith é suficiente
- Sugestão de Testcontainers — desnecessário para testes unitários com mocks

---

## Cobertura

| Classe | Cenários | Tipo |
|--------|----------|------|
| InteractionService | 6 | Unitário |
| NotificationService | 4 | Unitário |
| AuthService | 5 | Unitário |
| AuthController | 5 | Integração (MockMvc) |
| **Total** | **20** | — |
