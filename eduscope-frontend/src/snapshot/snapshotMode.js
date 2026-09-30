const SNAPSHOT_SESSION_KEY =
  'eduscope_snapshot_mode'

const SNAPSHOT_FILE =
  '/demo-snapshot/snapshot.json'

let snapshotPromise = null

/**
 * Oracle/Render와 무관하게 동작하는
 * 읽기 전용 Snapshot 사용자.
 *
 * 실제 APP_USER를 복제하지 않는다.
 */
export const SNAPSHOT_USER = {
  userId: null,
  loginId: 'snapshot_demo',
  displayName: '스냅샷 데모',
  accountStatus: 'ACTIVE',
  roles: ['ANALYST'],
  snapshotMode: true
}

export function isSnapshotMode() {
  return window.sessionStorage.getItem(
    SNAPSHOT_SESSION_KEY
  ) === 'true'
}

export function enterSnapshotMode() {
  window.sessionStorage.setItem(
    SNAPSHOT_SESSION_KEY,
    'true'
  )
}

export function leaveSnapshotMode() {
  window.sessionStorage.removeItem(
    SNAPSHOT_SESSION_KEY
  )
}

/**
 * snapshot.json이 실제 배포되어 있을 때만
 * Snapshot 진입 버튼을 노출한다.
 */
export async function isSnapshotAvailable() {
  try {
    const response = await fetch(
      SNAPSHOT_FILE,
      {
        method: 'GET',
        cache: 'no-store'
      }
    )

    if (!response.ok) {
      return false
    }

    const contentType =
      response.headers.get(
        'content-type'
      )

    return Boolean(
      contentType
      &&
      contentType.includes(
        'application/json'
      )
    )
  } catch {
    return false
  }
}

async function loadSnapshot() {
  if (!snapshotPromise) {
    snapshotPromise =
      fetch(
        SNAPSHOT_FILE,
        {
          method: 'GET',
          cache: 'no-store'
        }
      )
      .then(
        async response => {
          if (!response.ok) {
            throw new Error(
              '스냅샷 데이터를 찾을 수 없습니다.'
            )
          }

          const snapshot =
            await response.json()

          if (
            !snapshot
            ||
            typeof snapshot !== 'object'
            ||
            !snapshot.responses
          ) {
            throw new Error(
              '스냅샷 데이터 형식이 올바르지 않습니다.'
            )
          }

          return snapshot
        }
      )
  }

  return snapshotPromise
}

/**
 * Query Parameter 순서 차이를 제거한
 * Snapshot Key를 만든다.
 */
export function normalizeSnapshotKey(
  requestUrl
) {
  const url =
    new URL(
      requestUrl,
      window.location.origin
    )

  const sortedParams =
    [...url.searchParams.entries()]
      .sort(
        ([keyA, valueA], [keyB, valueB]) => {
          const keyCompare =
            keyA.localeCompare(
              keyB
            )

          if (keyCompare !== 0) {
            return keyCompare
          }

          return valueA.localeCompare(
            valueB
          )
        }
      )

  const normalizedParams =
    new URLSearchParams()

  sortedParams.forEach(
    ([key, value]) => {
      normalizedParams.append(
        key,
        value
      )
    }
  )

  const query =
    normalizedParams.toString()

  return query
    ? url.pathname + '?' + query
    : url.pathname
}

/**
 * 저장된 학생 Index에서
 * 검색어/강의를 Frontend에서 Filter한다.
 */
function resolveStudentSearch(
  snapshot,
  requestUrl
) {
  const url =
    new URL(
      requestUrl,
      window.location.origin
    )

  const keyword =
    url.searchParams
      .get('keyword')
      ?.trim()
      ?.toLowerCase()
    ?? ''

  const coursePresentationId =
    url.searchParams.get(
      'coursePresentationId'
    )

  const index =
    Array.isArray(
      snapshot.studentSearchIndex
    )
      ? snapshot.studentSearchIndex
      : []

  return index
    .filter(
      row => {
        const keywordMatch =
          keyword.length === 0
          ||
          String(
            row.sourceStudentId
            ?? ''
          )
            .toLowerCase()
            .includes(
              keyword
            )

        const courseMatch =
          !coursePresentationId
          ||
          String(
            row.coursePresentationId
          )
          ===
          String(
            coursePresentationId
          )

        return (
          keywordMatch
          &&
          courseMatch
        )
      }
    )
    .slice(
      0,
      100
    )
}

export async function resolveSnapshotRequest(
  requestUrl
) {
  const snapshot =
    await loadSnapshot()

  const url =
    new URL(
      requestUrl,
      window.location.origin
    )

  if (
    url.pathname
    ===
    '/api/student-analysis/search'
  ) {
    return {
      status: 200,
      data:
        resolveStudentSearch(
          snapshot,
          requestUrl
        )
    }
  }

  const key =
    normalizeSnapshotKey(
      requestUrl
    )

  if (
    !Object.prototype
      .hasOwnProperty
      .call(
        snapshot.responses,
        key
      )
  ) {
    return {
      status: 404,
      data: {
        message:
          '현재 스냅샷에 저장되지 않은 조회입니다.'
      }
    }
  }

  return {
    status: 200,
    data:
      snapshot.responses[key]
  }
}
