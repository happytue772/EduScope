/**
 * 학생 수강정보 검색.
 */
export async function searchStudents(
  keyword,
  coursePresentationId
) {

  const params =
    new URLSearchParams()

  if (keyword?.trim()) {

    params.set(
      'keyword',
      keyword.trim()
    )
  }

  if (coursePresentationId) {

    params.set(
      'coursePresentationId',
      coursePresentationId
    )
  }

  const response = await fetch(
    '/api/student-analysis/search?'
    + params.toString(),
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {

    throw new Error(
      '학생 검색 실패: '
      + response.status
    )
  }

  return response.json()
}


/**
 * 특정 학생-강의 상세 분석.
 */
export async function getStudentAnalysis(
  studentCourseId
) {

  const response = await fetch(
    '/api/student-analysis/'
    + studentCourseId,
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {

    throw new Error(
      '학생 분석 조회 실패: '
      + response.status
    )
  }

  return response.json()
}