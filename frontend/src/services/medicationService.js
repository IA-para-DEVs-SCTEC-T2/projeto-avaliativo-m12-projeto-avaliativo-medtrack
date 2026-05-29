import api from './api'

export default {
  getMyMedications() {
    return api.get('/user-medications')
  },

  addMedication(data) {
    return api.post('/user-medications', data)
  },

  updateMedication(id, data) {
    return api.put(`/user-medications/${id}`, data)
  },

  deactivateMedication(id) {
    return api.patch(`/user-medications/${id}/deactivate`)
  },

  removeMedication(id) {
    return api.delete(`/user-medications/${id}`)
  },

  getInteractions(medicationId) {
    return api.get(`/interactions/medication/${medicationId}`)
  }
}
