<script setup>
import { ref, onMounted } from 'vue'
import adminService from '../../services/adminService'

const users = ref([])

async function loadUsers() {
  const response = await adminService.getUsers()
  users.value = response.data
}

async function toggleActive(userId) {
  await adminService.toggleUserActive(userId)
  await loadUsers()
}

onMounted(loadUsers)
</script>

<template>
  <div class="manage-users">
    <h1>Gerenciar Usuários</h1>

    <table>
      <thead>
        <tr>
          <th>E-mail</th>
          <th>Role</th>
          <th>Status</th>
          <th>Criado em</th>
          <th>Ações</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.email }}</td>
          <td>{{ user.role }}</td>
          <td>{{ user.active ? 'Ativo' : 'Inativo' }}</td>
          <td>{{ new Date(user.createdAt).toLocaleDateString('pt-BR') }}</td>
          <td>
            <button @click="toggleActive(user.id)">
              {{ user.active ? 'Desativar' : 'Ativar' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
