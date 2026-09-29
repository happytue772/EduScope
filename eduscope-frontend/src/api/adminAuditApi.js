import {
  getCsrfToken
} from './authApi'


/**
 * ADMIN Audit Log 조회.
 *
 * Backend:
 * GET /api/admin/audit-logs
 *
 * Spring Data Page 구조로 반환된다.
 */
export async function getAdminAuditLogs(
  page = 0,
  size = 20
) {

  const params =
    new URLSearchParams({
      page: String(page),
      size: String(size)
    })


  const response =
    await fetch(
      '/api/admin/audit-logs?'
      + params.toString(),
      {
        method: 'GET',
        credentials: 'include'
      }
    )


  if (!response.ok) {

    throw new Error(
      'Audit Log 조회 실패: '
      + response.status
    )
  }


  return response.json()
}


/**
 * 특정 날짜 이전의 Audit Log 중
 * 실제 삭제 대상 건수를 조회한다.
 *
 * 실제 삭제는 수행하지 않는다.
 *
 * Backend:
 * GET /api/admin/audit-logs/purge-preview
 */
export async function getAuditPurgePreview(
  before
) {

  const params =
    new URLSearchParams({
      before
    })


  const response =
    await fetch(
      '/api/admin/audit-logs/purge-preview?'
      + params.toString(),
      {
        method: 'GET',
        credentials: 'include'
      }
    )


  const data =
    await readJsonSafely(
      response
    )


  if (!response.ok) {

    throw new Error(
      data?.message
      ?? (
        'Audit 삭제 대상 조회 실패: '
        + response.status
      )
    )
  }


  return data
}


/**
 * 특정 날짜 이전의 Audit Log를
 * 실제로 일괄 정리한다.
 *
 * DELETE 요청이므로
 * Spring Security CSRF Token을 사용한다.
 *
 * Backend:
 * DELETE /api/admin/audit-logs/purge
 */
export async function purgeAuditLogs(
  before
) {

  const csrf =
    await getCsrfToken()


  const params =
    new URLSearchParams({
      before
    })


  const response =
    await fetch(
      '/api/admin/audit-logs/purge?'
      + params.toString(),
      {
        method: 'DELETE',

        headers: {
          [csrf.headerName]:
            csrf.token
        },

        credentials: 'include'
      }
    )


  const data =
    await readJsonSafely(
      response
    )


  if (!response.ok) {

    throw new Error(
      data?.message
      ?? (
        'Audit Log 정리 실패: '
        + response.status
      )
    )
  }


  return data
}


/**
 * Backend 응답이 JSON인 경우에만
 * 안전하게 변환한다.
 *
 * 오류 응답이 비어 있거나 HTML이어도
 * Frontend가 추가 오류로 중단되지 않게 한다.
 */
async function readJsonSafely(
  response
) {

  const contentType =
    response.headers.get(
      'content-type'
    )


  if (
    contentType
    &&
    contentType.includes(
      'application/json'
    )
  ) {

    return response.json()
  }


  return null
}