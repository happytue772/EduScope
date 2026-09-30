import { apiFetch } from './apiFetch'

/**
 * ADMIN 전용 System Health 조회.
 *
 * Backend:
 * GET /api/admin/system-health
 *
 * 실제 Spring Boot / Oracle / JVM /
 * 최신 ANALYSIS_JOB 상태를 조회한다.
 */
export async function getSystemHealth() {

  const response =
    await apiFetch(
      '/api/admin/system-health',
      {
        method: 'GET',

        // Spring Security Session 전달
        credentials: 'include',

        headers: {
          Accept: 'application/json'
        }
      }
    )


  if (!response.ok) {

    let message =
      '시스템 상태를 조회하지 못했습니다.'


    try {

      const body =
        await response.json()


      if (body?.message) {
        message = body.message
      }

    } catch {

      // JSON 응답이 아니면 기본 메시지를 유지한다.
    }


    const error =
      new Error(message)


    error.status =
      response.status


    throw error
  }


  return response.json()
}