<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import medicationService from '../services/medicationService'
import api from '../services/api'
import DisclaimerBanner from '../components/common/DisclaimerBanner.vue'

const router = useRouter()
const availableMedications = ref([])
const form = reactive({
  medicationId: '',
  dosage: '',
  frequencyValue: '',
  frequencyUnit: 'DAILY',
  reminderTime: ''
})
const interactionAlert = ref(null)
const loading = ref(false)
const error = ref('')

async function loadMedications() {
  try {
    // Endpoint público (autenticado): lista de medicamentos cadastrados
    const response = await api.get('/medications')
    availableMedications.value = response.data
  } catch (err) {
    console.error('Erro ao carregar medicamentos disponíveis:', err)
    error.value = 'Não foi possível carregar a lista de medicamentos.'
  }
}

async function addMedication() {
  loading.value = true
  error.value = ''
  try {
    const response = await medicationService.addMedication({
      medicationId: Number(form.medicationId),
      dosage: form.dosage || null,
      frequencyValue: form.frequencyValue ? Number(form.frequencyValue) : null,
      frequencyUnit: form.frequencyUnit || null,
      reminderTime: form.reminderTime || null
    })

    if (response.data.hasInteractions) {
      interactionAlert.value = response.data.interactions
    } else {
      router.push({ name: 'medications' })
    }
  } catch (err) {
    console.error('Erro ao adicionar medicamento:', err)
    error.value = err.response?.data?.message || 'Erro ao adicionar medicamento.'
  } finally {
    loading.value = false
  }
}

function dismissAlert() {
  interactionAlert.value = null
  router.push({ name: 'medications' })
}

const SEVERITY_LABEL = { MILD: 'Leve', MODERATE: 'Moderada', SEVERE: 'Grave' }

onMounted(loadMedications)
</script>

<template>
  <div class="add-medication">
    <h1>Adicionar medicamento</h1>

    <DisclaimerBanner />

    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <div v-if="interactionAlert" class="card interaction-card">
      <h2>⚠️ Interações detectadas</h2>
      <div
        v-for="inter in interactionAlert"
        :key="inter.id"
        class="interaction-item"
      >
        <strong>{{ inter.medicationAName }} + {{ inter.medicationBName }}</strong>
        <span class="severity-badge" :class="`severity-${inter.severity.toLowerCase()}`">
          {{ SEVERITY_LABEL[inter.severity] || inter.severity }}
        </span>
        <p>{{ inter.description }}</p>
      </div>
      <p class="warning-text">Consulte seu médico antes de combinar estes medicamentos.</p>
      <button class="button" @click="dismissAlert">Entendi</button>
    </div>

    <form v-else class="card medication-form" @submit.prevent="addMedication">
      <div class="field">
        <label for="medicationId">Medicamento</label>
        <select id="medicationId" v-model="form.medicationId" required>
          <option value="" disabled>Selecione...</option>
          <option v-for="med in availableMedications" :key="med.id" :value="med.id">
            {{ med.name }}<span v-if="med.activeIngredient"> ({{ med.activeIngredient }})</span>
          </option>
        </select>
      </div>

      <div class="field">
        <label for="dosage">Dosagem</label>
        <input id="dosage" v-model="form.dosage" placeholder="Ex: 500mg" />
      </div>

      <div class="field-row">
        <div class="field">
          <label for="frequencyValue">Quantidade</label>
          <input id="frequencyValue" v-model="form.frequencyValue" type="number" min="1" />
        </div>
        <div class="field">
          <label for="frequencyUnit">Unidade</label>
          <select id="frequencyUnit" v-model="form.frequencyUnit">
            <option value="HOURS">em horas</option>
            <option value="DAILY">vezes ao dia</option>
            <option value="WEEKLY">vezes por semana</option>
            <option value="MONTHLY">vezes ao mês</option>
          </select>
        </div>
      </div>

      <div class="field">
        <label for="reminderTime">Horário do lembrete</label>
        <input id="reminderTime" v-model="form.reminderTime" type="time" />
      </div>

      <div class="form-actions">
        <router-link :to="{ name: 'medications' }" class="button button-secondary">Cancelar</router-link>
        <button class="button" type="submit" :disabled="loading">
          {{ loading ? 'Adicionando...' : 'Adicionar' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1rem;
}

.interaction-card { border-left: 4px solid var(--color-warning); }
.interaction-item { padding: 0.75rem 0; border-bottom: 1px solid var(--color-border); }
.interaction-item:last-of-type { border-bottom: none; }

.severity-badge {
  margin-left: 0.5rem;
  padding: 0.15rem 0.5rem;
  font-size: 0.8rem;
  border-radius: 999px;
  background: #f1f5f9;
}

.warning-text {
  font-style: italic;
  color: var(--color-warning);
}

@media (max-width: 540px) {
  .field-row { grid-template-columns: 1fr; }
}
</style>
