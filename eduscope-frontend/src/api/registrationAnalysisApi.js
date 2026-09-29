/**
 * 특정 강의의 수강/철회 분석 조회.
 */
export async function getRegistrationAnalysis(
  coursePresentationId
) {

  const response = await fetch(
    '/api/registration-analysis/'
    + coursePresentationId,
    {
      method: 'GET',
      credentials: 'include'
    }
  )

  if (!response.ok) {

    throw new Error(
      '수강 분석 조회 실패: '
      + response.status
    )
  }

  return response.json()
}