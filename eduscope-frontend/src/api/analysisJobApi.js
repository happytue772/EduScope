import {
  getCsrfToken
} from './authApi'


/**
 * Dataset 기준 Analysis Job 운영현황 조회.
 */
export async function getAnalysisJobOverview(
  datasetId
) {

  const response =
    await fetch(
      '/api/analysis-job-overview'
      + '?datasetId='
      + encodeURIComponent(
          datasetId
        ),
      {
        method: 'GET',
        credentials: 'include'
      }
    )


  if (!response.ok) {

    throw new Error(
      'Analysis Job 조회 실패: '
      + response.status
    )
  }


  return readJson(
    response
  )
}


/**
 * 신규 Analysis Job 요청.
 *
 * 실제 Hadoop 실행이 아니라
 * PENDING Job을 생성한다.
 */
export async function createAnalysisJob(
  request
) {

  const csrf =
    await getCsrfToken()


  const response =
    await fetch(
      '/api/analysis-jobs',
      {
        method: 'POST',

        headers: {
          'Content-Type':
            'application/json',

          [csrf.headerName]:
            csrf.token
        },

        credentials: 'include',

        body:
          JSON.stringify(
            request
          )
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
        'Analysis Job 요청 실패: '
        + response.status
      )
    )
  }


  return data
}


/**
 * FAILED Job 재실행 요청.
 *
 * 기존 Job은 보존되고
 * 새로운 PENDING Job이 생성된다.
 */
export async function retryAnalysisJob(
  jobId,
  hdfsOutputPath
) {

  const csrf =
    await getCsrfToken()


  const response =
    await fetch(
      '/api/analysis-jobs/'
      + encodeURIComponent(
          jobId
        )
      + '/retry',
      {
        method: 'POST',

        headers: {
          'Content-Type':
            'application/json',

          [csrf.headerName]:
            csrf.token
        },

        credentials: 'include',

        body:
          JSON.stringify({
            hdfsOutputPath
          })
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
        'Analysis Job 재실행 요청 실패: '
        + response.status
      )
    )
  }


  return data
}


/**
 * 정상 JSON 응답 조회.
 */
async function readJson(
  response
) {

  const data =
    await readJsonSafely(
      response
    )


  if (data == null) {

    throw new Error(
      'Analysis Job 응답이 JSON이 아닙니다.'
    )
  }


  return data
}


/**
 * JSON 응답 안전 처리.
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
/**
 * PENDING Analysis Job을
 * 실제 Hadoop MapReduce 실행으로 넘긴다.
 */
export async function executeAnalysisJob(
  jobId
) {

  const csrf =
    await getCsrfToken()


  const response =
    await fetch(
      '/api/analysis-jobs/'
      + encodeURIComponent(jobId)
      + '/execute',
      {
        method: 'POST',

        headers: {
          [csrf.headerName]:
            csrf.token
        },

        credentials: 'include'
      }
    )


  const text =
    await response.text()


  let data = null


  if (text) {

    try {

      data =
        JSON.parse(text)

    } catch {

      data = null
    }
  }


  if (!response.ok) {

    throw new Error(
      data?.message
      ?? (
        'Analysis Job 실행 실패: '
        + response.status
      )
    )
  }


  return data
}