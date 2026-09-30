import { apiFetch } from './apiFetch'

/**
 * EduScope Dashboard API.
 *
 * Vite Proxy를 사용하므로
 * localhost:8080을 직접 작성하지 않는다.
 */
export async function getDashboardSummary(datasetId) {

  const response = await apiFetch(
    `/api/dashboard/summary?datasetId=${datasetId}`,
    {
      method: 'GET',
      credentials: 'include',
    }
  )

  if (!response.ok) {
    throw new Error(
      `Dashboard API 호출 실패: ${response.status}`
    )
  }

  const contentType =
    response.headers.get('content-type')

  /**
   * 로그인 세션이 없으면
   * Spring Security 로그인 HTML이 반환될 수 있다.
   */
  if (
    !contentType ||
    !contentType.includes('application/json')
  ) {
    throw new Error(
      'Dashboard JSON 응답이 아닙니다. 로그인 상태를 확인하세요.'
    )
  }

  return response.json()
}