import { getCsrfToken } from './authApi'

/**
 * ADMIN Dataset 전체 조회.
 *
 * 기존 관리 API를 유지해 논리 삭제된 Dataset도 함께 조회한다.
 */
export async function getAdminDatasets() {
  const response = await fetch(
    '/api/admin/datasets',
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {
    throw new Error(
      'Dataset 목록 조회 실패: '
      + response.status
    )
  }

  return response.json()
}

/**
 * Dataset 메타정보 신규 등록.
 *
 * 기존 ADMIN API를 유지한다.
 */
export async function createAdminDataset(request) {
  return sendJsonWithCsrf(
    '/api/admin/datasets',
    'POST',
    request,
    'Dataset 등록에 실패했습니다.'
  )
}

/**
 * Dataset 메타정보 수정.
 *
 * Version은 수정하지 않는다.
 */
export async function updateAdminDataset(
  datasetId,
  request
) {
  return sendJsonWithCsrf(
    '/api/admin/datasets/'
      + encodeURIComponent(datasetId),
    'PATCH',
    request,
    'Dataset 수정에 실패했습니다.'
  )
}

/**
 * Dataset 논리 삭제.
 *
 * 기존 관리 API를 유지한다.
 */
export async function deleteAdminDataset(datasetId) {
  const csrf = await getCsrfToken()

  const response = await fetch(
    '/api/admin/datasets/'
      + encodeURIComponent(datasetId),
    {
      method: 'DELETE',

      headers: {
        [csrf.headerName]: csrf.token
      },

      credentials: 'include'
    }
  )

  const data = await readJsonSafely(response)

  if (!response.ok) {
    throw new Error(
      data?.message
        ?? 'Dataset 삭제에 실패했습니다.'
    )
  }

  return data
}

/**
 * 기존 Dataset을 기준으로 새 Version을 등록한다.
 *
 * 기존 행을 덮어쓰지 않고 Backend가 새로운 DATASET 행을 생성한다.
 */
export async function createDatasetVersion(
  datasetId,
  request
) {
  return sendJsonWithCsrf(
    '/api/datasets/'
      + encodeURIComponent(datasetId)
      + '/versions',
    'POST',
    request,
    'Dataset Version 등록에 실패했습니다.'
  )
}

/**
 * 특정 Dataset의 실제 DATASET_FILE 목록 조회.
 */
export async function getAdminDatasetFiles(datasetId) {
  const response = await fetch(
    '/api/dataset-files/dataset/'
      + encodeURIComponent(datasetId),
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {
    throw new Error(
      'Dataset File 조회 실패: '
      + response.status
    )
  }

  const result = await response.json()

  return Array.isArray(result)
    ? result
    : []
}

/**
 * 실제 원본 파일 메타정보를 DATASET_FILE에 등록한다.
 */
export async function createDatasetFile(
  datasetId,
  request
) {
  return sendJsonWithCsrf(
    '/api/dataset-files/dataset/'
      + encodeURIComponent(datasetId),
    'POST',
    request,
    'Dataset File 등록에 실패했습니다.'
  )
}

/**
 * 실제 파일에서 계산한 SHA-256 Checksum을 갱신한다.
 */
export async function updateDatasetFileChecksum(
  datasetFileId,
  contentHash
) {
  return sendJsonWithCsrf(
    '/api/dataset-files/'
      + encodeURIComponent(datasetFileId)
      + '/checksum',
    'PUT',
    {
      contentHash
    },
    'Checksum 갱신에 실패했습니다.'
  )
}

/**
 * JSON Body + CSRF가 필요한 관리 요청 공통 처리.
 */
async function sendJsonWithCsrf(
  url,
  method,
  request,
  defaultMessage
) {
  const csrf = await getCsrfToken()

  const response = await fetch(
    url,
    {
      method,

      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token
      },

      credentials: 'include',

      body: JSON.stringify(request)
    }
  )

  const data = await readJsonSafely(response)

  if (!response.ok) {
    throw new Error(
      data?.message
        ?? defaultMessage
    )
  }

  return data
}

/**
 * JSON 응답이 없는 경우까지 안전하게 처리한다.
 */
async function readJsonSafely(response) {
  const contentType =
    response.headers.get('content-type')

  if (
    contentType
    &&
    contentType.includes('application/json')
  ) {
    return response.json()
  }

  return null
}