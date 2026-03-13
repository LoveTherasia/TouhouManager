// src/api/auth.js
import { remoteRequest } from './request'

//这里注册登录发送验证码都是调用服务器端的接口
export const authAPI = {
    login(data){
        return remoteRequest({
            url: '/api/auth/login',
            method: 'post',
            data
        })
    },

    register(data){
        return remoteRequest({
            url: '/api/auth/register',
            method: 'post',
            data
        })
    },

    sendCode(email){
        return remoteRequest({
            url: '/api/auth/send-code',
            method: 'post',
            params: { email }
        })
    },

    getUserProfile(){
        return remoteRequest({
            url: '/api/user/me',
            method: 'get'
        })
    },

    updateUserProfile(data){
        return remoteRequest({
            url: '/api/user/profile',
            method: 'put',
            data
        })
    }
}