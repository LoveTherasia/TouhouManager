import request from './request'

const BASE_URL = "http://localhost:8080/api/auth"

export const authAPI = {
    login(data){
        return request({
            url: `${BASE_URL}/login`,
            method: 'post',
            data
        })
    },

    register(data){
        return request({
            url: `${BASE_URL}/register`,
            method: 'post',
            data
        })
    },

    sendCode(email){
        return request({
            url: `${BASE_URL}/send-code`,
            method: 'post',
            params: { email }
        })
    }
}