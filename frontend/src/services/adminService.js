import api from './api'

export default {
  // Medicamentos
  getMedications() {
    return api.get('/admin/medications')
  },
  createMedication(data) {
    return api.post('/admin/medications', data)
  },
  updateMedication(id, data) {
    return api.put(`/admin/medications/${id}`, data)
  },
  deleteMedication(id) {
    return api.delete(`/admin/medications/${id}`)
  },

  // Interações
  getInteractions() {
    return api.get('/admin/interactions')
  },
  createInteraction(data) {
    return api.post('/admin/interactions', data)
  },
  deleteInteraction(id) {
    return api.delete(`/admin/interactions/${id}`)
  },

  // Usuários
  getUsers() {
    return api.get('/admin/users')
  },
  toggleUserActive(id) {
    return api.patch(`/admin/users/${id}/toggle-active`)
  }
}
