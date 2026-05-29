import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMedicationStore = defineStore('medication', () => {
  const medications = ref([])

  function setMedications(meds) {
    medications.value = meds
  }

  return { medications, setMedications }
})
