/**
 * 강의 목록 조회.
 */
export async function getCourses() {

  const response = await fetch(
    '/api/courses',
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {
    throw new Error(
      '강의 목록 조회 실패: ' + response.status
    )
  }

  return response.json()
}

/**
 * 특정 강의의 종합 분석 조회.
 */
export async function getCourseAnalysis(
  coursePresentationId
) {

  const response = await fetch(
    '/api/course-analysis/' + coursePresentationId,
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {
    throw new Error(
      '강의 분석 조회 실패: ' + response.status
    )
  }

  return response.json()
}