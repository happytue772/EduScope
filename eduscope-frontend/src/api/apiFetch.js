import {
  isSnapshotMode,
  resolveSnapshotRequest
} from '../snapshot/snapshotMode'

/**
 * 일반 모드는 기존 Backend API를 사용하고,
 * Snapshot 모드는 Vercel 정적 JSON을 사용한다.
 */
export async function apiFetch(
  input,
  init = {}
) {
  if (
    !isSnapshotMode()
  ) {
    return fetch(
      input,
      init
    )
  }

  const method =
    String(
      init.method
      ?? 'GET'
    )
      .toUpperCase()

  if (
    method !== 'GET'
  ) {
    return jsonResponse(
      {
        message:
          '스냅샷 데모에서는 조회만 가능합니다.'
      },
      403
    )
  }

  try {
    const result =
      await resolveSnapshotRequest(
        String(input)
      )

    return jsonResponse(
      result.data,
      result.status
    )
  } catch (error) {
    return jsonResponse(
      {
        message:
          error?.message
          ??
          '스냅샷 조회에 실패했습니다.'
      },
      500
    )
  }
}

function jsonResponse(
  data,
  status
) {
  return new Response(
    JSON.stringify(
      data
    ),
    {
      status,
      headers: {
        'Content-Type':
          'application/json',
        'Cache-Control':
          'no-store'
      }
    }
  )
}
