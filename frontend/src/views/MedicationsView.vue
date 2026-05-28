<script setup>
import { ref, onMounted } from 'vue'
import medicationService from '../services/medicationService'
import DisclaimerBanner from '../components/common/DisclaimerBanner.vue'

const medications = ref([])
const loading = ref(true)
const error = ref('')
const editing = ref(null) // id do medicamento sendo editado
const editForm = ref({})

const FREQUENCY_LABEL = {
  HOURS: 'em horas',
  DAILY: 'vezes ao dia',
  WEEKLY: 'vezes por semana',
  MONTHLY: 'vezes ao mês'
}

function formatTime(time) {
  if (!time) return null
  return time.length > 5 ? time.substring(0, 5) : time
}

function frequencyLabel(med) {
  if (!med.frequencyValue) return null
  return `${med.frequencyValue} ${FREQUENCY_LABEL[med.frequencyUnit] || med.frequencyUnit}`
}

function formatDate(date) {
  if (!date) return null
  return new Date(date).toLocaleDateString('pt-BR')
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

function startEdit(med) {
  editing.value = med.id
  editForm.value = {
    medicationId: med.medicationId,
    dosage: med.dosage || '',
    frequencyValue: med.frequencyValue || '',
    frequencyUnit: med.frequencyUnit || 'DAILY',
    reminderTime: formatTime(med.reminderTime) || '',
    startDate: med.startDate || '',
    endDate: med.endDate || ''
  }
}

function cancelEdit() {
  editing.value = null
  editForm.value = {}
}

async function saveEdit(id) {
  try {
    await medicationService.updateMedication(id, {
      ...editForm.value,
      medicationId: editForm.value.medicationId,
      frequencyValue: editForm.value.frequencyValue ? Number(editForm.value.frequencyValue) : null,
      reminderTime: editForm.value.reminderTime || null,
      startDate: editForm.value.startDate || null,
      endDate: editForm.value.endDate || null
    })
    editing.value = null
    await loadMedications()
  } catch (err) {
    console.error(err)
    alert('Não foi possível salvar as alterações.')
  }
}

async function deactivateMedication(id) {
  if (!confirm('Deseja inativar este medicamento? Os lembretes serão cessados.')) return
  try {
    await medicationService.deactivateMedication(id)
    await loadMedications()
  } catch (err) {
    console.error(err)
    alert('Não foi possível inativar.')
  }
}

async function removeMedication(id) {
  if (!confirm('Tem certeza que deseja remover este medicamento permanentemente?')) return
  try {
    await medicationService.removeMedication(id)
    await loadMedications()
  } catch (err) {
    console.error(err)
    alert('Não foi possível remover.')
  }
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
      <article v-for="med in medications" :key="med.id" class="card medication-card" :class="{ 'card-inactive': !med.active, 'card-editing': editing === med.id }">

        <!-- Modo visualização -->
        <template v-if="editing !== med.id">
          <div class="card-header">
            <h3>{{ med.medicationName }}</h3>
            <span class="status-badge" :class="med.active ? 'badge-active' : 'badge-inactive'">
              {{ med.active ? 'Ativo' : 'Inativo' }}
            </span>
          </div>
          <p v-if="med.dosage"><strong>Dosagem:</strong> {{ med.dosage }}</p>
          <p v-if="frequencyLabel(med)"><strong>Frequência:</strong> {{ frequencyLabel(med) }}</p>
          <p v-if="med.reminderTime"><strong>Lembrete:</strong> {{ formatTime(med.reminderTime) }}</p>
          <p v-if="med.startDate"><strong>Início:</strong> {{ formatDate(med.startDate) }}</p>
          <p v-if="med.endDate"><strong>Fim:</strong> {{ formatDate(med.endDate) }}</p>
          <div class="card-actions">
            <button class="button button-secondary button-compact" @click="startEdit(med)">Editar</button>
            <button v-if="med.active" class="button button-compact" @click="deactivateMedication(med.id)">Inativar</button>
            <button class="button button-danger button-compact" @click="removeMedication(med.id)">Remover</button>
          </div>
        </template>

        <!-- Modo edição -->
        <template v-else>
          <h3>Editando: {{ med.medicationName }}</h3>
          <div class="edit-form">
            <div class="field">
              <label>Dosagem</label>
              <input v-model="editForm.dosage" placeholder="Ex: 500mg" />
            </div>
            <div class="field-row">
              <div class="field">
                <label>Frequência</label>
                <input v-model="editForm.frequencyValue" type="number" min="1" />
              </div>
              <div class="field">
                <label>Unidade</label>
                <select v-model="editForm.frequencyUnit">
                  <option value="HOURS">em horas</option>
                  <option value="DAILY">vezes ao dia</option>
                  <option value="WEEKLY">vezes por semana</option>
                  <option value="MONTHLY">vezes ao mês</option>
                </select>
              </div>
            </div>
            <div class="field">
              <label>Lembrete</label>
              <input v-model="editForm.reminderTime" type="time" />
            </div>
            <div class="field-row">
              <div class="field">
                <label>Início</label>
                <input v-model="editForm.startDate" type="date" />
              </div>
              <div class="field">
                <label>Fim (opcional)</label>
                <input v-model="editForm.endDate" type="date" />
              </div>
            </div>
            <div class="card-actions">
              <button class="button button-secondary button-compact" @click="cancelEdit">Cancelar</button>
              <button class="button button-compact" @click="saveEdit(med.id)">Salvar</button>
            </div>
          </div>
        </template>
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}
.card-header h3 { margin: 0; }

.status-badge {
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  white-space: nowrap;
}
.badge-active { background: #e8f5e9; color: var(--color-success); }
.badge-inactive { background: #f1f5f9; color: var(--color-text-muted); }

.card-inactive {
  opacity: 0.65;
  border-left: 3px solid var(--color-text-muted);
}

.card-editing {
  grid-column: 1 / -1;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.button-compact {
  padding: 0.35rem 0.7rem;
  font-size: 0.85rem;
}

.edit-form .field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

@media (max-width: 540px) {
  .edit-form .field-row { grid-template-columns: 1fr; }
}
</style>
