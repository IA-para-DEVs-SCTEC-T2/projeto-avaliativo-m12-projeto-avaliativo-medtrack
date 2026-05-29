<script setup>
import { reactive, ref, onMounted } from 'vue'
import adminService from '../../services/adminService'

const medications = ref([])
const form = reactive({ name: '', activeIngredient: '', description: '' })
const editing = ref(null)
const loading = ref(false)
const error = ref('')

async function loadMedications() {
  try {
    const response = await adminService.getMedications()
    medications.value = response.data
  } catch (err) {
    console.error(err)
    error.value = 'Não foi possível carregar os medicamentos.'
  }
}

async function saveMedication() {
  loading.value = true
  error.value = ''
  try {
    if (editing.value) {
      await adminService.updateMedication(editing.value, { ...form })
    } else {
      await adminService.createMedication({ ...form })
    }
    resetForm()
    await loadMedications()
  } catch (err) {
    console.error(err)
    error.value = err.response?.data?.message || 'Não foi possível salvar.'
  } finally {
    loading.value = false
  }
}

function editMedication(med) {
  form.name = med.name
  form.activeIngredient = med.activeIngredient || ''
  form.description = med.description || ''
  editing.value = med.id
}

function resetForm() {
  form.name = ''
  form.activeIngredient = ''
  form.description = ''
  editing.value = null
}

async function deleteMedication(id) {
  if (!confirm('Tem certeza que deseja remover este medicamento?')) return
  try {
    await adminService.deleteMedication(id)
    await loadMedications()
  } catch (err) {
    console.error(err)
    alert('Não foi possível remover.')
  }
}

onMounted(loadMedications)
</script>

<template>
  <div class="manage-medications">
    <h1>Gerenciar medicamentos</h1>
    <p class="subtitle">Catálogo central usado por todos os usuários.</p>

    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <form class="card medication-form" @submit.prevent="saveMedication">
      <h3>{{ editing ? 'Editar medicamento' : 'Cadastrar medicamento' }}</h3>
      <div class="field">
        <label for="name">Nome</label>
        <input id="name" v-model="form.name" placeholder="Ex: Paracetamol" required />
      </div>
      <div class="field">
        <label for="activeIngredient">Princípio ativo</label>
        <input id="activeIngredient" v-model="form.activeIngredient" placeholder="Ex: Paracetamol" />
      </div>
      <div class="field">
        <label for="description">Descrição</label>
        <input id="description" v-model="form.description" placeholder="Breve descrição" />
      </div>
      <div class="form-actions">
        <button v-if="editing" type="button" class="button button-secondary" @click="resetForm">Cancelar</button>
        <button class="button" type="submit" :disabled="loading">
          {{ loading ? 'Salvando...' : (editing ? 'Atualizar' : 'Cadastrar') }}
        </button>
      </div>
    </form>

    <table v-if="medications.length > 0">
      <thead>
        <tr>
          <th>Nome</th>
          <th>Princípio ativo</th>
          <th>Descrição</th>
          <th class="actions-col">Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="med in medications" :key="med.id">
          <td>{{ med.name }}</td>
          <td>{{ med.activeIngredient || '—' }}</td>
          <td>{{ med.description || '—' }}</td>
          <td class="actions-col">
            <button class="button button-secondary button-compact" @click="editMedication(med)">Editar</button>
            <button class="button button-danger button-compact" @click="deleteMedication(med.id)">Remover</button>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-else class="empty-state">Nenhum medicamento cadastrado ainda.</p>
  </div>
</template>

<style scoped>
.subtitle { color: var(--color-text-muted); margin-top: -0.5rem; margin-bottom: 1rem; }
.medication-form { margin-bottom: 1.5rem; }
.medication-form h3 { margin-bottom: 1rem; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.5rem; }
.actions-col { white-space: nowrap; }
.actions-col .button + .button { margin-left: 0.4rem; }
.button-compact { padding: 0.35rem 0.7rem; font-size: 0.85rem; }
</style>
