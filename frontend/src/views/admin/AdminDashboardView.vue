<script setup>
import { ref, onMounted } from 'vue'
import adminService from '../../services/adminService'

const stats = ref({ medications: 0, users: 0, interactions: 0 })
const error = ref('')

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
  } catch (err) {
    console.error('Erro ao carregar estatísticas:', err)
    error.value = 'Não foi possível carregar as estatísticas.'
  }
})
</script>

<template>
  <div class="admin-dashboard">
    <h1>Painel administrativo</h1>
    <p class="subtitle">Resumo do sistema e atalhos para gerenciamento.</p>

    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <section class="stats">
      <div class="stat-card card">
        <span class="stat-number">{{ stats.medications }}</span>
        <span class="stat-label">Medicamentos</span>
      </div>
      <div class="stat-card card">
        <span class="stat-number">{{ stats.users }}</span>
        <span class="stat-label">Usuários</span>
      </div>
      <div class="stat-card card">
        <span class="stat-number">{{ stats.interactions }}</span>
        <span class="stat-label">Interações</span>
      </div>
    </section>

    <section class="actions-grid">
      <router-link :to="{ name: 'admin-medications' }" class="card action-card">
        <h3>Medicamentos</h3>
        <p>Cadastrar, editar e remover medicamentos do catálogo.</p>
      </router-link>
      <router-link :to="{ name: 'admin-interactions' }" class="card action-card">
        <h3>Interações</h3>
        <p>Manter a base local de interações críticas conhecidas.</p>
      </router-link>
      <router-link :to="{ name: 'admin-users' }" class="card action-card">
        <h3>Usuários</h3>
        <p>Listar e ativar/desativar usuários do sistema.</p>
      </router-link>
      <router-link :to="{ name: 'admin-config' }" class="card action-card">
        <h3>Configurações</h3>
        <p>Feature flags como integração openFDA e notificações.</p>
      </router-link>
    </section>
  </div>
</template>

<style scoped>
.subtitle { color: var(--color-text-muted); margin-top: -0.5rem; margin-bottom: 1.5rem; }

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}
.stat-card { text-align: center; }
.stat-number { font-size: 2.4rem; font-weight: 700; color: var(--color-primary); display: block; }
.stat-label { color: var(--color-text-muted); font-size: 0.95rem; }

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
}
.action-card {
  text-decoration: none;
  color: var(--color-text);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.action-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  text-decoration: none;
}
.action-card h3 { color: var(--color-primary); margin-bottom: 0.5rem; }
.action-card p { color: var(--color-text-muted); margin: 0; }
</style>
