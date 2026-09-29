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
 * 지정 시간만큼 대기한다.
 *
 * Render Free 서버의 Cold Start 과정에서
 * API 재시도 간격을 두기 위해 사용한다.
 */
function delay(
  milliseconds
) {

  return new Promise(
    resolve => {

      window.setTimeout(
        resolve,
        milliseconds
      )

    }
  )
}


/**
 * CSRF Token 조회.
 *
 * Render Free 환경에서는
 * 일정 시간 미사용 시 서버가 종료되었다가
 * 첫 요청 시 다시 시작될 수 있다.
 *
 * 이 과정에서 Vercel Rewrite가
 * 502 / 503 / 504를 반환할 수 있으므로
 * 제한적으로 재시도한다.
 *
 * 기존 Spring Security CSRF 구조는 그대로 유지한다.
 */
export async function getCsrfToken() {

  const maxAttempts = 4


  const retryStatuses =
    new Set([
      502,
      503,
      504
    ])


  let lastError = null


  for (
    let attempt = 1;
    attempt <= maxAttempts;
    attempt++
  ) {

    try {

      const response =
        await fetch(
          '/api/auth/csrf',
          {
            method: 'GET',

            credentials:
              'include',

            /*
             * Cold Start 재시도 과정에서
             * 브라우저 Cache 응답을 사용하지 않는다.
             */
            cache:
              'no-store'
          }
        )


      /*
       * 정상 응답.
       */
      if (
        response.ok
      ) {

        return response.json()
      }


      /*
       * 502 / 503 / 504 이외의 오류는
       * Render Cold Start로 처리하지 않는다.
       *
       * 예:
       * 401 / 403 / 404 등
       */
      if (
        !retryStatuses.has(
          response.status
        )
      ) {

        const error =
          new Error(
            'CSRF 토큰 조회 실패'
          )


        error.status =
          response.status


        throw error
      }


      /*
       * Render가 시작 중일 가능성이 있는 경우.
       */
      lastError =
        new Error(
          'DEMO_SERVER_WAKING'
        )


      lastError.code =
        'DEMO_SERVER_WAKING'


      lastError.status =
        response.status

    }
    catch (
      error
    ) {

      /*
       * 위에서 직접 생성한
       * Cold Start 오류는 그대로 유지한다.
       */
      if (
        error?.code
        ===
        'DEMO_SERVER_WAKING'
      ) {

        lastError =
          error

      }

      /*
       * fetch 자체가 실패하는 네트워크 오류도
       * Render Cold Start 과정일 가능성이 있다.
       */
      else if (
        error instanceof TypeError
      ) {

        lastError =
          new Error(
            'DEMO_SERVER_WAKING'
          )


        lastError.code =
          'DEMO_SERVER_WAKING'

      }

      /*
       * 그 외 오류는 기존 방식대로 전달한다.
       */
      else {

        throw error
      }
    }


    /*
     * 마지막 시도가 아니라면
     * 잠시 기다린 후 재요청한다.
     */
    if (
      attempt < maxAttempts
    ) {

      await delay(
        5000
      )
    }
  }


  /*
   * 모든 재시도가 실패한 경우.
   */
  throw (
    lastError
    ??
    new Error(
      'CSRF 토큰 조회 실패'
    )
  )
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

  /*
   * 로그인 전에 CSRF Token을 조회한다.
   *
   * Render가 Sleep 상태라면
   * getCsrfToken() 내부에서 재시도한다.
   */
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

  }
  catch {

    responseData =
      null
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