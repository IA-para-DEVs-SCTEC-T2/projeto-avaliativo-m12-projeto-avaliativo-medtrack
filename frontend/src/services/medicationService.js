import api from './api'

export default {
  getMyMedications() {
    return api.get('/user-medications')
  },

  addMedication(data) {
    return api.post('/user-medications', data)
  },

  removeMedication(id) {
    return api.delete(`/user-medications/${id}`)
  },

  getInteractions(medicationId) {
    return api.get(`/interactions/medication/${medicationId}`)
  }
}
