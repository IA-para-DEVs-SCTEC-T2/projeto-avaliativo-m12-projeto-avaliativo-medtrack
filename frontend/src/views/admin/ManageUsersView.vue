<script setup>
import { ref, onMounted } from 'vue'
import adminService from '../../services/adminService'

const users = ref([])
const error = ref('')

async function loadUsers() {
  try {
    const response = await adminService.getUsers()
    users.value = response.data
  } catch (err) {
    console.error(err)
    error.value = 'Não foi possível carregar os usuários.'
  }
}

async function toggleActive(userId) {
  try {
    await adminService.toggleUserActive(userId)
    await loadUsers()
  } catch (err) {
    console.error(err)
    alert('Não foi possível alterar o status.')
  }
}

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('pt-BR')
}

onMounted(loadUsers)
</script>

<template>
  <div class="manage-users">
    <h1>Gerenciar usuários</h1>
    <p class="subtitle">Ativar e desativar contas. Nenhum dado pessoal além do e-mail é exibido.</p>

    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <table v-if="users.length > 0">
      <thead>
        <tr>
          <th>E-mail</th>
          <th>Role</th>
          <th>Status</th>
          <th>Criado em</th>
          <th class="actions-col">Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.email }}</td>
          <td>{{ user.role }}</td>
          <td>{{ user.active ? 'Ativo' : 'Inativo' }}</td>
          <td>{{ formatDate(user.createdAt) }}</td>
          <td class="actions-col">
            <button class="button button-compact" @click="toggleActive(user.id)">
              {{ user.active ? 'Desativar' : 'Ativar' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-else class="empty-state">Nenhum usuário cadastrado.</p>
  </div>
</template>

<style scoped>
.subtitle { color: var(--color-text-muted); margin-top: -0.5rem; margin-bottom: 1rem; }
.actions-col { white-space: nowrap; }
.button-compact { padding: 0.35rem 0.7rem; font-size: 0.85rem; }
</style>
