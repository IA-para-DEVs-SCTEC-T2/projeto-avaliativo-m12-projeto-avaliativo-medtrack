<script setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { useAuthStore } from './stores/auth'
import AppHeader from './components/layout/AppHeader.vue'

const route = useRoute()
const auth = useAuthStore()

const showHeader = computed(() => auth.isAuthenticated && !route.meta.guestOnly)
</script>

<template>
  <div class="app-shell">
    <AppHeader v-if="showHeader" />
    <main class="page">
      <RouterView />
    </main>
  </div>
</template>
