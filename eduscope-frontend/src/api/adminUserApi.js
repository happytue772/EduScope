import {
  getCsrfToken
} from './authApi'


/**
 * ADMIN 사용자 목록 조회.
 */
export async function getAdminUsers() {

  const response =
    await fetch(
      '/api/admin/users',
      {
        method: 'GET',
        credentials: 'include'
      }
    )


  if (!response.ok) {

    throw new Error(
      '사용자 목록 조회 실패: '
      + response.status
    )
  }


  return response.json()
}


/**
 * 사용자 계정 상태 및 Role 변경.
 *
 * PATCH
 * /api/admin/users/{userId}/access
 */
export async function updateAdminUserAccess(
  userId,
  accountStatus,
  roles
) {

  const csrf =
    await getCsrfToken()


  const response =
    await fetch(
      '/api/admin/users/'
      + encodeURIComponent(userId)
      + '/access',
      {
        method: 'PATCH',

        headers: {
          'Content-Type':
            'application/json',

          [csrf.headerName]:
            csrf.token
        },

        credentials: 'include',

        body:
          JSON.stringify({
            accountStatus,
            roles
          })
      }
    )


  let data = null

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

    data =
      await response.json()
  }


  if (!response.ok) {

    throw new Error(
      data?.message
      ?? '사용자 권한 변경에 실패했습니다.'
    )
  }


  return data
}