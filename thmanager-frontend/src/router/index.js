import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Games from '../views/Games.vue'
import Replays from '../views/Replays.vue'
import Statistics from '../views/Statistics.vue'
import Settings from '../views/Settings.vue'
import UserEdit from '../views/UserEdit.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { transition: 'page' }
  },
  {
    path: '/games',
    name: 'Games',
    component: Games,
    meta: { transition: 'page' }
  },
  {
    path: '/replays',
    name: 'Replays',
    component: Replays,
    meta: { transition: 'page' }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: Statistics,
    meta: { transition: 'page' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { transition: 'page' }
  },
  {
    path: '/user/edit',
    name: 'UserEdit',
    component: UserEdit,
    meta: { transition: 'page' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
