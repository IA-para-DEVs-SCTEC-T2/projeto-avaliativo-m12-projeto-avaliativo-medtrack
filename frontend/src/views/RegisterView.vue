<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import DisclaimerBanner from '../components/common/DisclaimerBanner.vue'

const auth = useAuthStore()
const router = useRouter()

const form = reactive({ email: '', password: '', confirmPassword: '' })
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  if (form.password.length < 6) {
    error.value = 'A senha deve ter no mínimo 6 caracteres.'
    return
  }
  if (form.password !== form.confirmPassword) {
    error.value = 'As senhas não conferem.'
    return
  }

  loading.value = true
  try {
    await auth.register({ email: form.email, password: form.password })
    router.push({ name: 'home' })
  } catch (err) {
    error.value = err.response?.data?.message || 'Não foi possível concluir o cadastro.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h1>Criar conta</h1>
      <p class="auth-subtitle">Sem CPF, sem nome real. Apenas e-mail para notificações.</p>

      <DisclaimerBanner />

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <form @submit.prevent="submit">
        <div class="field">
          <label for="email">E-mail</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            autocomplete="email"
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
            autocomplete="new-password"
            required
            minlength="6"
            placeholder="Mínimo 6 caracteres"
          />
        </div>

        <div class="field">
          <label for="confirm">Confirmar senha</label>
          <input
            id="confirm"
            v-model="form.confirmPassword"
            type="password"
            autocomplete="new-password"
            required
            minlength="6"
          />
        </div>

        <button class="button" type="submit" :disabled="loading">
          {{ loading ? 'Cadastrando...' : 'Cadastrar' }}
        </button>
      </form>

      <p class="auth-switch">
        Já tem conta?
        <router-link :to="{ name: 'login' }">Entrar</router-link>
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
