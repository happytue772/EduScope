import { apiFetch } from './apiFetch'

/**
 * 등록된 Dataset 목록 조회.
 */
export async function getDatasets() {

  const response = await apiFetch(
    '/api/datasets',
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
 * 특정 Dataset 상세 조회.
 */
export async function getDatasetDetail(
  datasetId
) {

  const response = await apiFetch(
    '/api/dataset-details/'
    + encodeURIComponent(datasetId),
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {

    throw new Error(
      'Dataset 상세 조회 실패: '
      + response.status
    )
  }

  return response.json()
}