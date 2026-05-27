<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header class="app-header">
    <div class="app-header-inner">
      <router-link :to="{ name: auth.isAuthenticated ? 'home' : 'login' }" class="brand">
        <span class="brand-mark">+</span>
        <span class="brand-name">MedTrack</span>
      </router-link>

      <nav v-if="auth.isAuthenticated" class="app-nav">
        <router-link :to="{ name: 'home' }">Início</router-link>
        <router-link :to="{ name: 'medications' }">Meus medicamentos</router-link>
        <router-link v-if="auth.isAdmin" :to="{ name: 'admin-dashboard' }">Painel admin</router-link>
      </nav>

      <div v-if="auth.isAuthenticated" class="app-user">
        <span class="user-email">{{ auth.email }}</span>
        <button class="button button-secondary button-compact" @click="logout">Sair</button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.app-header-inner {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 0.75rem 1.5rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 700;
  font-size: 1.1rem;
  color: var(--color-primary);
  text-decoration: none;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: var(--color-primary);
  color: #fff;
  border-radius: 6px;
  font-weight: 700;
}

.app-nav {
  display: flex;
  gap: 1.25rem;
  flex: 1;
}

.app-nav a {
  color: var(--color-text-muted);
  font-weight: 500;
  padding: 0.25rem 0;
  border-bottom: 2px solid transparent;
}

.app-nav a.router-link-active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.app-user {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-email {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.button-compact {
  padding: 0.4rem 0.85rem;
  font-size: 0.9rem;
}

@media (max-width: 720px) {
  .app-header-inner { flex-wrap: wrap; }
  .app-nav { width: 100%; order: 3; }
  .user-email { display: none; }
}
</style>
