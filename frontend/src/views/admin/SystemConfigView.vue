<script setup>
import { ref } from 'vue'

const configs = ref([
  { key: 'FDA_INTEGRATION_ENABLED', value: 'false', label: 'Integração FDA' },
  { key: 'NOTIFICATION_ENABLED', value: 'true', label: 'Notificações por e-mail' }
])
</script>

<template>
  <div class="system-config">
    <h1>Configurações do Sistema</h1>

    <div class="config-list">
      <div v-for="config in configs" :key="config.key" class="config-item">
        <label>{{ config.label }}</label>
        <span>{{ config.value === 'true' ? 'Ativado' : 'Desativado' }}</span>
      </div>
    </div>

    <p class="note">⚠️ Funcionalidade completa será implementada na Issue #6 (notificações + FDA).</p>
  </div>
</template>
