/**
 * Dataset의 최신 Data Quality 분석 결과 조회.
 */
export async function getDataQualitySummary(
  datasetId
) {

  const response = await fetch(
    '/api/analysis/data-quality/summary'
    + '?datasetId='
    + encodeURIComponent(datasetId),
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {

    throw new Error(
      'Data Quality 조회 실패: '
      + response.status
    )
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
      'Data Quality JSON 응답이 아닙니다.'
    )
  }


  return response.json()
}