export async function enroll(studentId, courseId) {
  const res = await fetch('http://localhost:8080/api/v1/enrollments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ studentId, courseId })
  })
  const data = await res.json()
  if (!res.ok) throw data
  return data
}
