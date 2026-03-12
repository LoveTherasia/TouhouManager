import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user',() => {
    const user = ref(null);
    const token = ref(localStorage.getItem('token') || '')

    const setUser = (userData) => {
        user.value = userData
    }

    const setToken = (newToken) => {
        token.value = newToken
        localStorage.setItem('token',newToken)
    }

    const logout = () => {
        user.value = null
        token.value = ''
        localStorage.removeItem('token')
    }

    const isLoggedIn = () => {
        return !!token.value
    }

    return {
        user,
        token,
        setUser,
        setToken,
        logout,
        isLoggedIn
    }
})