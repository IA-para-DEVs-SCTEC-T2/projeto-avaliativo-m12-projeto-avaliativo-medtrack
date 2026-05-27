<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import DisclaimerBanner from '../components/common/DisclaimerBanner.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ email: '', password: '' })
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login({ email: form.email, password: form.password })
    const redirect = route.query.redirect && typeof route.query.redirect === 'string'
      ? route.query.redirect
      : '/'
    router.push(redirect)
  } catch (err) {
    error.value = err.response?.data?.message || 'Não foi possível entrar. Verifique e-mail e senha.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h1>Entrar</h1>
      <p class="auth-subtitle">Acesse sua conta MedTrack</p>

      <DisclaimerBanner />

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <form @submit.prevent="submit">
        <div class="field">
          <label for="email">E-mail</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            autocomplete="username"
            required
            placeholder="seu@email.com"
          />
        </div>

        <div class="field">
          <label for="password">Senha</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            required
            placeholder="••••••••"
          />
        </div>

        <button class="button" type="submit" :disabled="loading">
          {{ loading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>

      <p class="auth-switch">
        Ainda não tem conta?
        <router-link :to="{ name: 'register' }">Cadastre-se</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding-top: 2rem;
}
.auth-card {
  width: 100%;
  max-width: 420px;
}
.auth-subtitle {
  margin-top: -0.5rem;
  margin-bottom: 1rem;
  color: var(--color-text-muted);
}
.auth-switch {
  margin-top: 1.25rem;
  text-align: center;
  color: var(--color-text-muted);
}
.button { width: 100%; }
</style>
