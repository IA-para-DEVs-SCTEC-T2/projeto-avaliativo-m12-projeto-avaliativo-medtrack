<script setup>
import { ref, onMounted } from 'vue'
import api from '../../services/api'

const fdaEnabled = ref(false)
const fdaStatus = ref('')
const loading = ref(false)
const error = ref('')

async function loadStatus() {
  try {
    const { data } = await api.get('/admin/fda/status')
    fdaEnabled.value = !!data.enabled
    fdaStatus.value = data.status || ''
  } catch (err) {
    console.error(err)
    error.value = 'Não foi possível carregar o status da integração FDA.'
  }
}

async function toggleFda() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.post('/admin/fda/toggle', { enabled: !fdaEnabled.value })
    fdaEnabled.value = !!data.enabled
  } catch (err) {
    console.error(err)
    error.value = 'Não foi possível alterar a integração FDA.'
  } finally {
    loading.value = false
  }
}

async function testFda() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get('/admin/fda/test-connection')
    alert(`Conexão FDA: ${data.status || 'OK'}`)
  } catch (err) {
    console.error(err)
    error.value = 'Falha ao testar a conexão com a FDA.'
  } finally {
    loading.value = false
  }
}

onMounted(loadStatus)
</script>

<template>
  <div class="system-config">
    <h1>Configurações do sistema</h1>
    <p class="subtitle">Feature flags e integrações externas.</p>

    <div v-if="error" class="alert alert-error">{{ error }}</div>

    <article class="card">
      <h3>Integração openFDA</h3>
      <p>
        Ativa a consulta adicional na API openFDA durante a checagem de interações.
        A base local continua sendo consultada independentemente desta flag.
      </p>
      <p>
        <strong>Status atual:</strong>
        <span :class="fdaEnabled ? 'badge-success' : 'badge-muted'">
          {{ fdaEnabled ? 'Ativada' : 'Desativada' }}
        </span>
      </p>

      <div class="actions">
        <button class="button" :disabled="loading" @click="toggleFda">
          {{ fdaEnabled ? 'Desativar' : 'Ativar' }}
        </button>
        <button class="button button-secondary" :disabled="loading" @click="testFda">
          Testar conexão
        </button>
      </div>
    </article>
  </div>
</template>

<style scoped>
.subtitle { color: var(--color-text-muted); margin-top: -0.5rem; margin-bottom: 1rem; }
.actions { display: flex; gap: 0.5rem; margin-top: 1rem; }
.badge-success {
  display: inline-block;
  padding: 0.15rem 0.6rem;
  border-radius: 999px;
  background: #e8f5e9;
  color: var(--color-success);
  font-weight: 600;
  margin-left: 0.4rem;
}
.badge-muted {
  display: inline-block;
  padding: 0.15rem 0.6rem;
  border-radius: 999px;
  background: #f1f5f9;
  color: var(--color-text-muted);
  font-weight: 600;
  margin-left: 0.4rem;
}
</style>
