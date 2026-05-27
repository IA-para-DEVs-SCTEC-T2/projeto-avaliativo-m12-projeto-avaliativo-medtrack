<script setup>
import { ref, onMounted } from 'vue'
import medicationService from '../services/medicationService'
import DisclaimerBanner from '../components/common/DisclaimerBanner.vue'

const medications = ref([])
const loading = ref(true)
const error = ref('')

const FREQUENCY_LABEL = {
  HOURS: 'em horas',
  DAILY: 'vezes ao dia',
  WEEKLY: 'vezes por semana',
  MONTHLY: 'vezes ao mês'
}

async function loadMedications() {
  loading.value = true
  error.value = ''
  try {
    const response = await medicationService.getMyMedications()
    medications.value = response.data
  } catch (err) {
    console.error('Erro ao carregar medicamentos:', err)
    error.value = 'Não foi possível carregar seus medicamentos.'
  } finally {
    loading.value = false
  }
}

async function removeMedication(id) {
  if (!confirm('Tem certeza que deseja remover este medicamento?')) return
  try {
    await medicationService.removeMedication(id)
    await loadMedications()
  } catch (err) {
    console.error(err)
    alert('Não foi possível remover. Tente novamente.')
  }
}

function frequencyLabel(med) {
  if (!med.frequencyValue) return null
  return `${med.frequencyValue} ${FREQUENCY_LABEL[med.frequencyUnit] || med.frequencyUnit}`
}

onMounted(loadMedications)
</script>

<template>
  <div class="medications">
    <div class="page-header">
      <h1>Meus medicamentos</h1>
      <router-link class="button" :to="{ name: 'add-medication' }">+ Adicionar</router-link>
    </div>

    <DisclaimerBanner />

    <div v-if="error" class="alert alert-error">{{ error }}</div>
    <div v-if="loading">Carregando...</div>

    <div v-else-if="medications.length === 0" class="empty-state card">
      <p>Você ainda não cadastrou nenhum medicamento.</p>
      <router-link class="button" :to="{ name: 'add-medication' }">Cadastrar agora</router-link>
    </div>

    <div v-else class="grid-cards">
      <article v-for="med in medications" :key="med.id" class="card medication-card">
        <h3>{{ med.medicationName }}</h3>
        <p v-if="med.dosage"><strong>Dosagem:</strong> {{ med.dosage }}</p>
        <p v-if="frequencyLabel(med)"><strong>Frequência:</strong> {{ frequencyLabel(med) }}</p>
        <p v-if="med.reminderTime"><strong>Lembrete:</strong> {{ med.reminderTime }}</p>
        <div class="card-actions">
          <button class="button button-danger" @click="removeMedication(med.id)">Remover</button>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.page-header h1 { margin: 0; }
.empty-state .button { margin-top: 1rem; }
</style>
