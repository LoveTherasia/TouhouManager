// src/api/request.js
import axios from 'axios'

const isDev = import.meta.env.DEV

// 本地 API 基础路径
const localBaseURL = isDev ? '' : '' // 本地路径，开发环境为空字符串

// 远程服务器 API 基础路径
const remoteBaseURL = 'http://121.43.124.131:8080'

// 创建本地 API 请求实例
const localRequest = axios.create({
  baseURL: localBaseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 创建远程 API 请求实例
const remoteRequest = axios.create({
  baseURL: remoteBaseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 本地
localRequest.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if(token){
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 请求拦截器 - 远程
remoteRequest.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if(token){
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 本地
localRequest.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('Local API Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 远程
remoteRequest.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('Remote API Error:', error)
    return Promise.reject(error)
  }
)

export { localRequest, remoteRequest }