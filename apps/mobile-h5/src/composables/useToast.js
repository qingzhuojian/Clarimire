import { ref } from 'vue'

const visible = ref(false)
const message = ref('')
const type = ref('success')
let timer = null

export function useToast() {
  const show = (msg, toastType = 'success') => {
    message.value = msg
    type.value = toastType
    visible.value = true
    clearTimeout(timer)
    timer = setTimeout(() => {
      visible.value = false
    }, 2500)
  }

  return { visible, message, type, show }
}
