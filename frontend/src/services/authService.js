import api from './api'

export default {
  login(credentials) {
    return api.post('/auth/login', credentials)
  },

  register(data) {
    return api.post('/auth/register', data)
  }
}
