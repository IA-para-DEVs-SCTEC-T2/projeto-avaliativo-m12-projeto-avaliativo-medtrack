<script setup>
import { ref, onMounted } from 'vue'
import adminService from '../../services/adminService'

const medications = ref([])
const form = ref({ name: '', activeIngredient: '', description: '' })
const editing = ref(null)

async function loadMedications() {
  const response = await adminService.getMedications()
  medications.value = response.data
}

async function saveMedication() {
  if (editing.value) {
    await adminService.updateMedication(editing.value, form.value)
  } else {
    await adminService.createMedication(form.value)
  }
  form.value = { name: '', activeIngredient: '', description: '' }
  editing.value = null
  await loadMedications()
}

function editMedication(med) {
  form.value = { name: med.name, activeIngredient: med.activeIngredient, description: med.description }
  editing.value = med.id
}

async function deleteMedication(id) {
  if (confirm('Tem certeza que deseja remover este medicamento?')) {
    await adminService.deleteMedication(id)
    await loadMedications()
  }
}

onMounted(loadMedications)
</script>

<template>
  <div class="manage-medications">
    <h1>Gerenciar Medicamentos</h1>

    <form @submit.prevent="saveMedication" class="medication-form">
      <input v-model="form.name" placeholder="Nome do medicamento" required />
      <input v-model="form.activeIngredient" placeholder="Princípio ativo" />
      <input v-model="form.description" placeholder="Descrição" />
      <button type="submit">{{ editing ? 'Atualizar' : 'Cadastrar' }}</button>
    </form>

    <table>
      <thead>
        <tr>
          <th>Nome</th>
          <th>Princípio Ativo</th>
          <th>Descrição</th>
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="med in medications" :key="med.id">
          <td>{{ med.name }}</td>
          <td>{{ med.activeIngredient }}</td>
          <td>{{ med.description }}</td>
          <td>
            <button @click="editMedication(med)">Editar</button>
            <button @click="deleteMedication(med.id)">Remover</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
