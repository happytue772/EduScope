/**
 * 현재 로그인 사용자 조회 API.
 */
export async function getCurrentUser() {

  const response = await fetch(
    '/api/auth/me',
    {
      method: 'GET',
      credentials: 'include'
    }
  )


  if (!response.ok) {

    const error =
      new Error(
        '사용자 조회 실패: '
        + response.status
      )

    error.status =
      response.status

    throw error
  }


  const contentType =
    response.headers.get(
      'content-type'
    )


  if (
    !contentType
    ||
    !contentType.includes(
      'application/json'
    )
  ) {

    throw new Error(
      '로그인이 필요합니다.'
    )
  }


  return response.json()
}


/**
 * CSRF Token 조회.
 */
export async function getCsrfToken() {

  const response =
    await fetch(
      '/api/auth/csrf',
      {
        credentials: 'include'
      }
    )


  if (!response.ok) {

    throw new Error(
      'CSRF 토큰 조회 실패'
    )
  }


  return response.json()
}


/**
 * 회원가입.
 */
export async function signup(
  form
) {

  const csrf =
    await getCsrfToken()


  const response =
    await fetch(
      '/api/auth/signup',
      {
        method: 'POST',

        headers: {
          'Content-Type':
            'application/json',

          [csrf.headerName]:
            csrf.token
        },

        credentials:
          'include',

        body:
          JSON.stringify(form)
      }
    )


  const data =
    await response.json()


  if (!response.ok) {

    throw new Error(
      data.message
      ??
      '회원가입에 실패했습니다.'
    )
  }


  return data
}


/**
 * Spring Security 로그인.
 */
export async function login(
  loginId,
  password
) {

  const csrf =
    await getCsrfToken()


  const body =
    new URLSearchParams()


  body.append(
    'username',
    loginId
  )


  body.append(
    'password',
    password
  )


  body.append(
    csrf.parameterName,
    csrf.token
  )


  const response =
    await fetch(
      '/api/auth/login',
      {
        method: 'POST',

        headers: {
          'Content-Type':
            'application/x-www-form-urlencoded'
        },

        credentials:
          'include',

        body
      }
    )


  /*
   * 실패 응답 JSON 안전하게 조회.
   */
  let responseData = null


  try {

    responseData =
      await response.json()

  } catch {

    responseData = null
  }


  if (!response.ok) {

    const error =
      new Error(
        responseData?.message
        ??
        'LOGIN_FAILED'
      )


    error.status =
      response.status


    /*
     * HTTP 429의 남은 제한시간.
     */
    error.retryAfterSeconds =
      Number(
        responseData?.retryAfterSeconds
        ??
        response.headers.get(
          'Retry-After'
        )
        ??
        0
      )


    throw error
  }


  return getCurrentUser()
}


/**
 * Spring Security Session 로그아웃.
 */
export async function logout() {

  const csrf =
    await getCsrfToken()


  const response =
    await fetch(
      '/api/auth/logout',
      {
        method: 'POST',

        headers: {
          [csrf.headerName]:
            csrf.token
        },

        credentials:
          'include'
      }
    )


  if (!response.ok) {

    throw new Error(
      '로그아웃에 실패했습니다.'
    )
  }
}