import { apiFetch } from './apiFetch'

/**
 * 특정 강의 VLE 학습활동 분석 조회.
 */
export async function getActivityAnalysis(
  coursePresentationId
) {

  const response = await apiFetch(
    '/api/activity-analysis/'
    + coursePresentationId,
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {

    throw new Error(
      '학습활동 분석 조회 실패: '
      + response.status
    )
  }

  return response.json()
}