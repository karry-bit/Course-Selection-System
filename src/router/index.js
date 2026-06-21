import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '../views/LoginView.vue'
import StudentShell from '../views/student/StudentShell.vue'
import StudentCourses from '../views/student/StudentCourses.vue'
import StudentTimetable from '../views/student/StudentTimetable.vue'
import StudentScores from '../views/student/StudentScores.vue'
import TeacherShell from '../views/teacher/TeacherShell.vue'
import TeacherCourses from '../views/teacher/TeacherCourses.vue'
import TeacherScores from '../views/teacher/TeacherScores.vue'
import AdminShell from '../views/admin/AdminShell.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import AdminStudents from '../views/admin/AdminStudents.vue'
import AdminTeachers from '../views/admin/AdminTeachers.vue'
import AdminCourses from '../views/admin/AdminCourses.vue'
import AdminClasses from '../views/admin/AdminClasses.vue'
import AdminTermSessions from '../views/admin/AdminTermSessions.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { guest: true }
  },
  {
    path: '/student',
    component: StudentShell,
    meta: { role: 'STUDENT' },
    children: [
      { path: '', redirect: '/student/courses' },
      { path: 'courses', name: 'StudentCourses', component: StudentCourses },
      { path: 'timetable', name: 'StudentTimetable', component: StudentTimetable },
      { path: 'scores', name: 'StudentScores', component: StudentScores }
    ]
  },
  {
    path: '/teacher',
    component: TeacherShell,
    meta: { role: 'TEACHER' },
    children: [
      { path: '', redirect: '/teacher/courses' },
      { path: 'courses', name: 'TeacherCourses', component: TeacherCourses },
      { path: 'scores', name: 'TeacherScores', component: TeacherScores }
    ]
  },
  {
    path: '/admin',
    component: AdminShell,
    meta: { role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: AdminDashboard },
      { path: 'students', name: 'AdminStudents', component: AdminStudents },
      { path: 'teachers', name: 'AdminTeachers', component: AdminTeachers },
      { path: 'courses', name: 'AdminCourses', component: AdminCourses },
      { path: 'classes', name: 'AdminClasses', component: AdminClasses },
      { path: 'term-sessions', name: 'AdminTermSessions', component: AdminTermSessions }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.guest) {
    // If already logged in, redirect to role home
    if (token && role) {
      return next(`/${role.toLowerCase()}`)
    }
    return next()
  }

  if (!token || !role) {
    return next('/login')
  }

  if (to.meta.role && to.meta.role !== role) {
    return next(`/${role.toLowerCase()}`)
  }

  next()
})

export default router