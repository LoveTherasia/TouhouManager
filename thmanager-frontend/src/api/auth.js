import request from './request'

export const authAPI = {
    login(data){
        return request({
            url: '/api/auth/login',
            method: 'post',
            data
        })
    },

    register(data){
        return request({
            url: '/api/auth/register',
            method: 'post',
            data
        })
    },

    sendCode(email){
        return request({
            url: '/api/auth/send-code',
            method: 'post',
            params: { email }
        })
    },

    getUserProfile(){
        return request({
            url: '/api/user/me',
            method: 'get'
        })
    },

    updateUserProfile(data){
        return request({
            url: '/api/user/profile',
            method: 'put',
            data
        })
    }
}