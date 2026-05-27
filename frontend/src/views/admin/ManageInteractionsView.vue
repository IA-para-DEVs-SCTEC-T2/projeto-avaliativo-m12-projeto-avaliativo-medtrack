<script setup>
import { reactive, ref, onMounted } from 'vue'
import adminService from '../../services/adminService'

const interactions = ref([])
const medications = ref([])
const form = reactive({ medicationAId: '', medicationBId: '', severity: 'MODERATE', description: '' })
const loading = ref(false)
const error = ref('')

const SEVERITY_LABEL = { MILD: 'Leve', MODERATE: 'Moderada', SEVERE: 'Grave' }

async function loadData() {
  try {
    const [interResp, medResp] = await Promise.all([
      adminService.getInteractions(),
      adminService.getMedications()
    ])
    interactions.value = interResp.data
    medications.value = medResp.data
  } catch (err) {
    console.error(err)
    error.value = 'Não foi possível carregar as interações.'
  }
}

async function createInteraction() {
  if (form.medicationAId === form.medicationBId) {
    error.value = 'Selecione dois medicamentos diferentes.'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await adminService.createInteraction({
      medicationAId: Number(form.medicationAId),
      medicationBId: Number(form.medicationBId),
      severity: form.severity,
      description: form.description
    })
    resetForm()
    await loadData()
  } catch (err) {
    console.error(err)
    error.value = err.response?.data?.message || 'Não foi possível cadastrar a interação.'
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.medicationAId = ''
  form.medicationBId = ''
  form.severity = 'MODERATE'
  form.description = ''
}

async function deleteInteraction(id) {
  if (!confirm('Tem certeza que deseja remover esta interação?')) return
  try {
    await adminService.deleteInteraction(id)
    await loadData()
  } catch (err) {
    console.error(err)
    alert('Não foi possível remover.')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="manage-interactions">
    <h1>Gerenciar interações</h1>
    <p class="subtitle">Base local de interações críticas usada para alertar usuários.</p>

    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <form class="card interaction-form" @submit.prevent="createInteraction">
      <h3>Cadastrar interação</h3>

      <div class="field-row">
        <div class="field">
          <label for="medA">Medicamento A</label>
          <select id="medA" v-model="form.medicationAId" required>
            <option value="" disabled>Selecione...</option>
            <option v-for="med in medications" :key="med.id" :value="med.id">{{ med.name }}</option>
          </select>
        </div>
        <div class="field">
          <label for="medB">Medicamento B</label>
          <select id="medB" v-model="form.medicationBId" required>
            <option value="" disabled>Selecione...</option>
            <option v-for="med in medications" :key="med.id" :value="med.id">{{ med.name }}</option>
          </select>
        </div>
      </div>

      <div class="field">
        <label for="severity">Severidade</label>
        <select id="severity" v-model="form.severity">
          <option value="MILD">Leve</option>
          <option value="MODERATE">Moderada</option>
          <option value="SEVERE">Grave</option>
        </select>
      </div>

      <div class="field">
        <label for="description">Descrição</label>
        <input id="description" v-model="form.description" placeholder="Descrição da interação" />
      </div>

      <div class="form-actions">
        <button class="button" type="submit" :disabled="loading">
          {{ loading ? 'Salvando...' : 'Cadastrar' }}
        </button>
      </div>
    </form>

    <table v-if="interactions.length > 0">
      <thead>
        <tr>
          <th>Medicamento A</th>
          <th>Medicamento B</th>
          <th>Severidade</th>
          <th>Descrição</th>
          <th>Fonte</th>
          <th class="actions-col">Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="inter in interactions" :key="inter.id">
          <td>{{ inter.medicationAName }}</td>
          <td>{{ inter.medicationBName }}</td>
          <td :class="`severity-${inter.severity.toLowerCase()}`">
            {{ SEVERITY_LABEL[inter.severity] || inter.severity }}
          </td>
          <td>{{ inter.description || '—' }}</td>
          <td>{{ inter.source }}</td>
          <td class="actions-col">
            <button class="button button-danger button-compact" @click="deleteInteraction(inter.id)">Remover</button>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-else class="empty-state">Nenhuma interação cadastrada.</p>
  </div>
</template>

<style scoped>
.subtitle { color: var(--color-text-muted); margin-top: -0.5rem; margin-bottom: 1rem; }
.interaction-form { margin-bottom: 1.5rem; }
.interaction-form h3 { margin-bottom: 1rem; }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.form-actions { display: flex; justify-content: flex-end; }
.actions-col { white-space: nowrap; }
.button-compact { padding: 0.35rem 0.7rem; font-size: 0.85rem; }

@media (max-width: 540px) {
  .field-row { grid-template-columns: 1fr; }
}
</style>
