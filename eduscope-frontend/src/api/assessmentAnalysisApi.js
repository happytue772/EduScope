/**
 * 특정 강의의 평가 분석 조회.
 */
export async function getAssessmentAnalysis(
  coursePresentationId
) {

  const response = await fetch(
    '/api/assessment-analysis/'
    + coursePresentationId,
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {

    throw new Error(
      '평가 분석 조회 실패: '
      + response.status
    )
  }

  return response.json()
}