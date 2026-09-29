/**
 * 특정 강의의 최종성과별
 * 학습활동 비교 조회.
 */
export async function getResultAnalysis(
  coursePresentationId
) {

  const response = await fetch(
    '/api/result-analysis/'
    + coursePresentationId,
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {

    throw new Error(
      '성과 비교 조회 실패: '
      + response.status
    )
  }

  return response.json()
}