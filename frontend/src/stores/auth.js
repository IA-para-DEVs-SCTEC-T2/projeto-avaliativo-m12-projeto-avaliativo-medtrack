import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import authService from '../services/authService'

const TOKEN_KEY = 'medtrack.token'
const EMAIL_KEY = 'medtrack.email'
const ROLE_KEY = 'medtrack.role'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || null)
  const email = ref(localStorage.getItem(EMAIL_KEY) || null)
  const role = ref(localStorage.getItem(ROLE_KEY) || null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  function applySession({ token: newToken, email: newEmail, role: newRole }) {
    token.value = newToken
    email.value = newEmail
    role.value = newRole
    localStorage.setItem(TOKEN_KEY, newToken)
    localStorage.setItem(EMAIL_KEY, newEmail)
    localStorage.setItem(ROLE_KEY, newRole)
  }

  async function login(credentials) {
    const { data } = await authService.login(credentials)
    applySession(data)
    return data
  }

  async function register(payload) {
    const { data } = await authService.register(payload)
    applySession(data)
    return data
  }

  function logout() {
    token.value = null
    email.value = null
    role.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EMAIL_KEY)
    localStorage.removeItem(ROLE_KEY)
  }

  return { token, email, role, isAuthenticated, isAdmin, login, register, logout }
})
