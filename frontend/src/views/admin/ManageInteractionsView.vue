<script setup>
import { ref, onMounted } from 'vue'
import adminService from '../../services/adminService'

const interactions = ref([])
const medications = ref([])
const form = ref({ medicationAId: '', medicationBId: '', severity: 'MODERATE', description: '' })

async function loadData() {
  const [interResp, medResp] = await Promise.all([
    adminService.getInteractions(),
    adminService.getMedications()
  ])
  interactions.value = interResp.data
  medications.value = medResp.data
}

async function createInteraction() {
  await adminService.createInteraction(form.value)
  form.value = { medicationAId: '', medicationBId: '', severity: 'MODERATE', description: '' }
  await loadData()
}

async function deleteInteraction(id) {
  if (confirm('Tem certeza que deseja remover esta interação?')) {
    await adminService.deleteInteraction(id)
    await loadData()
  }
}

function severityClass(severity) {
  return `severity-${severity.toLowerCase()}`
}

onMounted(loadData)
</script>

<template>
  <div class="manage-interactions">
    <h1>Gerenciar Interações</h1>

    <form @submit.prevent="createInteraction" class="interaction-form">
      <select v-model="form.medicationAId" required>
        <option value="" disabled>Medicamento A</option>
        <option v-for="med in medications" :key="med.id" :value="med.id">{{ med.name }}</option>
      </select>
      <select v-model="form.medicationBId" required>
        <option value="" disabled>Medicamento B</option>
        <option v-for="med in medications" :key="med.id" :value="med.id">{{ med.name }}</option>
      </select>
      <select v-model="form.severity">
        <option value="MILD">Leve</option>
        <option value="MODERATE">Moderada</option>
        <option value="SEVERE">Grave</option>
      </select>
      <input v-model="form.description" placeholder="Descrição da interação" />
      <button type="submit">Cadastrar</button>
    </form>

    <table>
      <thead>
        <tr>
          <th>Medicamento A</th>
          <th>Medicamento B</th>
          <th>Severidade</th>
          <th>Descrição</th>
          <th>Fonte</th>
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="inter in interactions" :key="inter.id">
          <td>{{ inter.medicationAName }}</td>
          <td>{{ inter.medicationBName }}</td>
          <td :class="severityClass(inter.severity)">{{ inter.severity }}</td>
          <td>{{ inter.description }}</td>
          <td>{{ inter.source }}</td>
          <td>
            <button @click="deleteInteraction(inter.id)">Remover</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
