<script setup>
import { ref, onMounted } from 'vue'
import adminService from '../../services/adminService'

const stats = ref({ medications: 0, users: 0, interactions: 0 })

onMounted(async () => {
  try {
    const [meds, users, interactions] = await Promise.all([
      adminService.getMedications(),
      adminService.getUsers(),
      adminService.getInteractions()
    ])
    stats.value = {
      medications: meds.data.length,
      users: users.data.length,
      interactions: interactions.data.length
    }
  } catch (error) {
    console.error('Erro ao carregar estatísticas:', error)
  }
})
</script>

<template>
  <div class="admin-dashboard">
    <h1>Painel Administrativo</h1>
    <div class="stats">
      <div class="stat-card">
        <h3>{{ stats.medications }}</h3>
        <p>Medicamentos</p>
      </div>
      <div class="stat-card">
        <h3>{{ stats.users }}</h3>
        <p>Usuários</p>
      </div>
      <div class="stat-card">
        <h3>{{ stats.interactions }}</h3>
        <p>Interações</p>
      </div>
    </div>
  </div>
</template>
