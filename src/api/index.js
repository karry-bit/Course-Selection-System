const BASE = '/api'

function authHeaders() {
  const token = localStorage.getItem('token')
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {})
  }
}

async function request(url, options = {}) {
  const res = await fetch(url, options)
  if (res.status === 204) return null
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    const err = new Error(data.message || `HTTP ${res.status}`)
    err.status = res.status
    err.data = data
    // 记录错误日志
    console.error(`[API Error] ${options.method || 'GET'} ${url}`, {
      status: res.status,
      message: data.message,
      data: data
    })
    throw err
  }
  return data
}

export default {
  // Auth
  login(username, password) {
    return request(`${BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    })
  },

  // Student
  getStudentInfo() {
    return request(`${BASE}/student/info`, { headers: authHeaders() })
  },
  getStudentCourses(search, semester, teacherId) {
    const params = new URLSearchParams()
    if (search) params.set('search', search)
    if (semester) params.set('semester', semester)
    if (teacherId) params.set('teacherId', teacherId)
    const qs = params.toString()
    return request(`${BASE}/student/courses${qs ? '?' + qs : ''}`, { headers: authHeaders() })
  },
  enroll(courseId, force = false) {
    return request(`${BASE}/student/enroll`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ courseId, force })
    })
  },
  dropCourse(courseId) {
    return request(`${BASE}/student/enroll/${courseId}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
  },
  getMyEnrollments() {
    return request(`${BASE}/student/my-enrollments`, { headers: authHeaders() })
  },
  getMyScores() {
    return request(`${BASE}/student/my-scores`, { headers: authHeaders() })
  },
  getTermSession() {
    return request(`${BASE}/student/term-session`, { headers: authHeaders() })
  },
  studentChangePassword(oldPassword, newPassword) {
    return request(`${BASE}/student/change-password`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ oldPassword, newPassword })
    })
  },

  // Teacher
  getTeacherInfo() {
    return request(`${BASE}/teacher/info`, { headers: authHeaders() })
  },
  getTeacherCourses() {
    return request(`${BASE}/teacher/courses`, { headers: authHeaders() })
  },
  getCourseStudents(courseId) {
    return request(`${BASE}/teacher/courses/${courseId}/students`, { headers: authHeaders() })
  },
  setScore(enrollmentId, score) {
    return request(`${BASE}/teacher/enrollments/${enrollmentId}/score`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({ score })
    })
  },
  batchSetScore(scores) {
    return request(`${BASE}/teacher/enrollments/batch-score`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify(scores)
    })
  },
  teacherChangePassword(oldPassword, newPassword) {
    return request(`${BASE}/teacher/change-password`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ oldPassword, newPassword })
    })
  },

  // Admin - Students
  getAdminStudents() {
    return request(`${BASE}/admin/students`, { headers: authHeaders() })
  },
  createStudent(body) {
    return request(`${BASE}/admin/students`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  updateStudent(id, body) {
    return request(`${BASE}/admin/students/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  deleteStudent(id) {
    return request(`${BASE}/admin/students/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
  },
  importStudents(formData) {
    return request(`${BASE}/admin/students/import`, {
      method: 'POST',
      headers: {
        ...authHeaders(),
        'Content-Type': undefined
      },
      body: formData
    })
  },

  // Admin - Teachers
  getAdminTeachers() {
    return request(`${BASE}/admin/teachers`, { headers: authHeaders() })
  },
  createTeacher(body) {
    return request(`${BASE}/admin/teachers`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  updateTeacher(id, body) {
    return request(`${BASE}/admin/teachers/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  deleteTeacher(id) {
    return request(`${BASE}/admin/teachers/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
  },
  importTeachers(formData) {
    return request(`${BASE}/admin/teachers/import`, {
      method: 'POST',
      headers: {
        ...authHeaders(),
        'Content-Type': undefined
      },
      body: formData
    })
  },
  resetStudentPassword(id) {
    return request(`${BASE}/admin/students/${id}/reset-password`, {
      method: 'POST',
      headers: authHeaders()
    })
  },
  resetTeacherPassword(id) {
    return request(`${BASE}/admin/teachers/${id}/reset-password`, {
      method: 'POST',
      headers: authHeaders()
    })
  },

  // Admin - Courses
  getAdminCourses(semester) {
    const qs = semester ? '?semester=' + semester : ''
    return request(`${BASE}/admin/courses${qs}`, { headers: authHeaders() })
  },
  createCourse(body) {
    return request(`${BASE}/admin/courses`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  updateCourse(id, body) {
    return request(`${BASE}/admin/courses/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  deleteCourse(id) {
    return request(`${BASE}/admin/courses/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
  },

  // Admin - Classes
  getAdminClasses() {
    return request(`${BASE}/admin/classes`, { headers: authHeaders() })
  },
  createClass(body) {
    return request(`${BASE}/admin/classes`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  updateClass(id, body) {
    return request(`${BASE}/admin/classes/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  deleteClass(id) {
    return request(`${BASE}/admin/classes/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
  },

  // Admin - Enrollments/Scores
  getAdminEnrollments(courseId) {
    const qs = courseId ? '?courseId=' + courseId : ''
    return request(`${BASE}/admin/enrollments${qs}`, { headers: authHeaders() })
  },
  setAdminScore(enrollmentId, score) {
    return request(`${BASE}/admin/enrollments/${enrollmentId}/score`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({ score })
    })
  },

  // Admin - Term Sessions
  getAdminTermSessions() {
    return request(`${BASE}/admin/term-sessions`, { headers: authHeaders() })
  },
  createTermSession(body) {
    return request(`${BASE}/admin/term-sessions`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  updateTermSession(id, body) {
    return request(`${BASE}/admin/term-sessions/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify(body)
    })
  },
  deleteTermSession(id) {
    return request(`${BASE}/admin/term-sessions/${id}`, {
      method: 'DELETE',
      headers: authHeaders()
    })
  },

  // Admin - Stats
  getAdminStats() {
    return request(`${BASE}/admin/stats`, { headers: authHeaders() })
  }
}
