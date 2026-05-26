<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import medicationService from '../services/medicationService'
import adminService from '../services/adminService'

const router = useRouter()
const availableMedications = ref([])
const form = ref({
  medicationId: '',
  dosage: '',
  frequencyValue: '',
  frequencyUnit: 'DAILY',
  reminderTime: ''
})
const interactionAlert = ref(null)
const loading = ref(false)

async function loadMedications() {
  try {
    const response = await adminService.getMedications()
    availableMedications.value = response.data
  } catch (error) {
    console.error('Erro ao carregar medicamentos disponíveis:', error)
  }
}

async function addMedication() {
  loading.value = true
  try {
    const response = await medicationService.addMedication({
      medicationId: Number(form.value.medicationId),
      dosage: form.value.dosage || null,
      frequencyValue: form.value.frequencyValue ? Number(form.value.frequencyValue) : null,
      frequencyUnit: form.value.frequencyUnit || null,
      reminderTime: form.value.reminderTime || null
    })

    if (response.data.hasInteractions) {
      interactionAlert.value = response.data.interactions
    } else {
      router.push('/medications')
    }
  } catch (error) {
    console.error('Erro ao adicionar medicamento:', error)
    alert('Erro ao adicionar medicamento')
  } finally {
    loading.value = false
  }
}

function dismissAlert() {
  interactionAlert.value = null
  router.push('/medications')
}

function severityLabel(severity) {
  const labels = { MILD: 'Leve', MODERATE: 'Moderada', SEVERE: 'Grave' }
  return labels[severity] || severity
}

onMounted(loadMedications)
</script>

<template>
  <div class="add-medication">
    <h1>Adicionar Medicamento</h1>
    <p class="disclaimer">⚠️ Este sistema NÃO substitui orientação médica profissional.</p>

    <!-- Alerta de Interação -->
    <div v-if="interactionAlert" class="interaction-modal">
      <h2>⚠️ Interações Detectadas!</h2>
      <div v-for="inter in interactionAlert" :key="inter.id" class="interaction-item"
           :class="'severity-' + inter.severity.toLowerCase()">
        <strong>{{ inter.medicationAName }} + {{ inter.medicationBName }}</strong>
        <span class="severity-badge">{{ severityLabel(inter.severity) }}</span>
        <p>{{ inter.description }}</p>
      </div>
      <p class="warning">Consulte seu médico antes de combinar estes medicamentos.</p>
      <button @click="dismissAlert">Entendi</button>
    </div>

    <!-- Formulário -->
    <form v-else @submit.prevent="addMedication" class="medication-form">
      <label>Medicamento</label>
      <select v-model="form.medicationId" required>
        <option value="" disabled>Selecione um medicamento</option>
        <option v-for="med in availableMedications" :key="med.id" :value="med.id">
          {{ med.name }} ({{ med.activeIngredient }})
        </option>
      </select>

      <label>Dosagem</label>
      <input v-model="form.dosage" placeholder="Ex: 500mg" />

      <label>Frequência</label>
      <div class="frequency-row">
        <input v-model="form.frequencyValue" type="number" min="1" placeholder="Qtd" />
        <select v-model="form.frequencyUnit">
          <option value="HOURS">horas</option>
          <option value="DAILY">vezes ao dia</option>
          <option value="WEEKLY">vezes por semana</option>
          <option value="MONTHLY">vezes ao mês</option>
        </select>
      </div>

      <label>Horário do lembrete</label>
      <input v-model="form.reminderTime" type="time" />

      <button type="submit" :disabled="loading">
        {{ loading ? 'Adicionando...' : 'Adicionar' }}
      </button>
    </form>
  </div>
</template>
