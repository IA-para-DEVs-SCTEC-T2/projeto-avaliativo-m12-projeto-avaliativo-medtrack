<script setup>
import { useAuthStore } from '../stores/auth'
import DisclaimerBanner from '../components/common/DisclaimerBanner.vue'

const auth = useAuthStore()
</script>

<template>
  <div class="dashboard">
    <h1>Olá, {{ auth.email }}</h1>
    <p class="subtitle">O que você quer fazer agora?</p>

    <DisclaimerBanner />

    <section class="dashboard-grid">
      <router-link :to="{ name: 'medications' }" class="card dashboard-card">
        <h3>Meus medicamentos</h3>
        <p>Veja os medicamentos cadastrados na sua rotina e os horários dos lembretes.</p>
      </router-link>

      <router-link :to="{ name: 'add-medication' }" class="card dashboard-card">
        <h3>Adicionar medicamento</h3>
        <p>Cadastre um novo medicamento e seja alertado sobre interações em tempo real.</p>
      </router-link>

      <router-link
        v-if="auth.isAdmin"
        :to="{ name: 'admin-dashboard' }"
        class="card dashboard-card dashboard-card-admin"
      >
        <h3>Painel administrativo</h3>
        <p>Gerenciar medicamentos, usuários e interações cadastradas no sistema.</p>
      </router-link>
    </section>
  </div>
</template>

<style scoped>
.subtitle {
  color: var(--color-text-muted);
  margin-top: -0.5rem;
}
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
  margin-top: 1.25rem;
}
.dashboard-card {
  display: block;
  text-decoration: none;
  color: var(--color-text);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.dashboard-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  text-decoration: none;
}
.dashboard-card h3 { color: var(--color-primary); margin-bottom: 0.5rem; }
.dashboard-card p { color: var(--color-text-muted); margin: 0; }
.dashboard-card-admin h3 { color: var(--color-accent); }
</style>
