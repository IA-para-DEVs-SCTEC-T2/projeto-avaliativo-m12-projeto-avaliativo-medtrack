<script setup>
import { ref, onMounted } from 'vue'
import medicationService from '../services/medicationService'

const medications = ref([])
const loading = ref(true)

async function loadMedications() {
  try {
    const response = await medicationService.getMyMedications()
    medications.value = response.data
  } catch (error) {
    console.error('Erro ao carregar medicamentos:', error)
  } finally {
    loading.value = false
  }
}

async function removeMedication(id) {
  if (confirm('Tem certeza que deseja remover este medicamento?')) {
    await medicationService.removeMedication(id)
    await loadMedications()
  }
}

onMounted(loadMedications)
</script>

<template>
  <div class="medications">
    <h1>Meus Medicamentos</h1>
    <p class="disclaimer">⚠️ Este sistema NÃO substitui orientação médica profissional.</p>

    <router-link to="/medications/add" class="btn-add">+ Adicionar Medicamento</router-link>

    <div v-if="loading">Carregando...</div>

    <div v-else-if="medications.length === 0" class="empty">
      <p>Você ainda não cadastrou nenhum medicamento.</p>
    </div>

    <div v-else class="medication-list">
      <div v-for="med in medications" :key="med.id" class="medication-card">
        <h3>{{ med.medicationName }}</h3>
        <p v-if="med.dosage"><strong>Dosagem:</strong> {{ med.dosage }}</p>
        <p v-if="med.frequencyValue">
          <strong>Frequência:</strong> {{ med.frequencyValue }}x {{ med.frequencyUnit }}
        </p>
        <p v-if="med.reminderTime"><strong>Lembrete:</strong> {{ med.reminderTime }}</p>
        <button @click="removeMedication(med.id)" class="btn-remove">Remover</button>
      </div>
    </div>
  </div>
</template>
