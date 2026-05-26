import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/DashboardView.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue')
  },
  // Admin routes
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: () => import('../views/admin/AdminDashboardView.vue')
  },
  {
    path: '/admin/medications',
    name: 'admin-medications',
    component: () => import('../views/admin/ManageMedicationsView.vue')
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: () => import('../views/admin/ManageUsersView.vue')
  },
  {
    path: '/admin/interactions',
    name: 'admin-interactions',
    component: () => import('../views/admin/ManageInteractionsView.vue')
  },
  {
    path: '/admin/config',
    name: 'admin-config',
    component: () => import('../views/admin/SystemConfigView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
