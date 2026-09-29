import {
  useEffect,
  useMemo,
  useState
} from 'react'

import {
  getAdminAuditLogs,
  getAuditPurgePreview,
  purgeAuditLogs
} from '../api/adminAuditApi'

import '../styles/adminAudit.css'


/**
 * 날짜 / 시간 표시.
 */
function formatDateTime(value) {

  if (!value) {
    return '-'
  }

  return new Date(value)
    .toLocaleString()
}


/**
 * Spring Data Page 응답 정규화.
 *
 * Backend:
 * Page<AuditLogResponse>
 */
function normalizeAuditResponse(result) {

  if (
    !result
    ||
    typeof result !== 'object'
    ||
    !Array.isArray(result.content)
  ) {

    return null
  }

  return {
    logs:
      result.content,

    totalElements:
      result.totalElements
      ?? 0,

    totalPages:
      result.totalPages
      ?? 0,

    hasNext:
      result.last === false
  }
}


/**
 * 오늘 날짜를 YYYY-MM-DD 형식으로 반환.
 *
 * 미래 날짜 Purge 선택 방지용.
 */
function getTodayDate() {

  const now =
    new Date()

  const year =
    now.getFullYear()

  const month =
    String(
      now.getMonth() + 1
    ).padStart(
      2,
      '0'
    )

  const day =
    String(
      now.getDate()
    ).padStart(
      2,
      '0'
    )

  return `${year}-${month}-${day}`
}


/**
 * ADMIN Audit Log 관리 화면.
 *
 * 기능:
 * - Audit Log 조회
 * - 검색
 * - Action Filter
 * - Pagination
 * - Purge Preview
 * - 오래된 Audit Log 정리
 */
function AdminAuditPage() {

  const [
    logs,
    setLogs
  ] = useState([])


  const [
    loading,
    setLoading
  ] = useState(true)


  const [
    error,
    setError
  ] = useState(null)


  const [
    keyword,
    setKeyword
  ] = useState('')


  const [
    actionType,
    setActionType
  ] = useState('ALL')


  const [
    page,
    setPage
  ] = useState(0)


  const [
    totalElements,
    setTotalElements
  ] = useState(0)


  const [
    totalPages,
    setTotalPages
  ] = useState(0)


  const [
    hasNext,
    setHasNext
  ] = useState(false)


  /*
   * Audit Purge 기준 날짜.
   */
  const [
    purgeBefore,
    setPurgeBefore
  ] = useState('')


  /*
   * Preview를 통해 확인된
   * 실제 삭제 대상 건수.
   */
  const [
    purgeCount,
    setPurgeCount
  ] = useState(null)


  const [
    purging,
    setPurging
  ] = useState(false)


  const [
    purgeMessage,
    setPurgeMessage
  ] = useState('')


  /*
   * Purge 후 현재 Page가 같아도
   * 강제로 다시 조회하기 위한 값.
   */
  const [
    refreshKey,
    setRefreshKey
  ] = useState(0)


  const pageSize = 20

  const today =
    getTodayDate()


  /**
   * Audit Log 조회.
   */
  useEffect(() => {

    let cancelled = false


    async function loadLogs() {

      try {

        const result =
          await getAdminAuditLogs(
            page,
            pageSize
          )


        if (cancelled) {
          return
        }


        const normalized =
          normalizeAuditResponse(
            result
          )


        if (!normalized) {

          setLogs([])

          setTotalElements(0)

          setTotalPages(0)

          setHasNext(false)

          setError(
            'Audit Log API 응답 구조를 확인할 수 없습니다.'
          )

          return
        }


        setLogs(
          normalized.logs
        )


        setTotalElements(
          normalized.totalElements
        )


        setTotalPages(
          normalized.totalPages
        )


        setHasNext(
          normalized.hasNext
        )


        setError(null)

      } catch (err) {

        if (cancelled) {
          return
        }


        setLogs([])

        setTotalElements(0)

        setTotalPages(0)

        setHasNext(false)

        setError(
          err.message
        )

      } finally {

        if (!cancelled) {

          setLoading(false)
        }
      }
    }


    loadLogs()


    return () => {

      cancelled = true
    }

  }, [
    page,
    refreshKey
  ])


  /**
   * 현재 Page에서 실제 존재하는
   * Action Type 목록.
   */
  const actionTypes =
    useMemo(() => {

      return [
        ...new Set(
          logs
            .map(
              log =>
                log.actionType
            )
            .filter(Boolean)
        )
      ].sort()

    }, [logs])


  /**
   * 현재 Page 내부 검색 / 필터.
   */
  const filteredLogs =
    useMemo(() => {

      const normalizedKeyword =
        keyword
          .trim()
          .toLowerCase()


      return logs.filter(
        log => {

          const matchesAction =
            actionType === 'ALL'
            ||
            log.actionType === actionType


          /*
           * targetId가 Number/String 어느 쪽이어도
           * 검색할 수 있도록 문자열로 변환.
           */
          const searchableTargetId =
            log.targetId == null
              ? ''
              : String(
                  log.targetId
                )
                  .toLowerCase()


          const matchesKeyword =
            normalizedKeyword.length === 0

            ||

            log.loginId
              ?.toLowerCase()
              .includes(
                normalizedKeyword
              )

            ||

            log.actionType
              ?.toLowerCase()
              .includes(
                normalizedKeyword
              )

            ||

            log.targetType
              ?.toLowerCase()
              .includes(
                normalizedKeyword
              )

            ||

            searchableTargetId
              .includes(
                normalizedKeyword
              )

            ||

            log.description
              ?.toLowerCase()
              .includes(
                normalizedKeyword
              )


          return (
            matchesAction
            &&
            matchesKeyword
          )
        }
      )

    }, [
      logs,
      keyword,
      actionType
    ])


  /**
   * 실제 삭제 전에
   * 삭제 대상 Audit 건수를 확인한다.
   */
  async function handlePurgePreview() {

    if (!purgeBefore) {

      setPurgeCount(null)

      setPurgeMessage(
        '기준 날짜를 선택해주세요.'
      )

      return
    }


    try {

      setPurgeCount(null)

      setPurgeMessage('')


      const result =
        await getAuditPurgePreview(
          purgeBefore
        )


      const count =
        Number(
          result?.deleteCount
          ?? 0
        )


      setPurgeCount(
        count
      )


      if (count === 0) {

        setPurgeMessage(
          '해당 날짜 이전에 정리할 Audit Log가 없습니다.'
        )
      }

    } catch (err) {

      setPurgeCount(null)

      setPurgeMessage(
        err.message
      )
    }
  }


  /**
   * 오래된 Audit Log 실제 정리.
   *
   * Preview를 먼저 수행한 경우에만
   * 실행 가능하다.
   */
  async function handlePurge() {

    if (!purgeBefore) {

      setPurgeMessage(
        '기준 날짜를 선택해주세요.'
      )

      return
    }


    if (
      purgeCount == null
      ||
      purgeCount <= 0
    ) {

      return
    }


    const confirmed =
      window.confirm(
        purgeBefore
        + ' 00:00 이전 Audit Log '
        + purgeCount.toLocaleString()
        + '건을 정리하시겠습니까?\n\n'
        + '삭제된 Audit Log는 복구되지 않습니다.'
      )


    if (!confirmed) {
      return
    }


    try {

      setPurging(true)

      setPurgeMessage('')


      const result =
        await purgeAuditLogs(
          purgeBefore
        )


      const deletedCount =
        Number(
          result?.deletedCount
          ?? 0
        )


      setPurgeMessage(
        deletedCount.toLocaleString()
        + '건의 Audit Log를 정리했습니다.'
      )


      /*
       * 기존 Preview는 삭제 전 결과이므로 초기화.
       */
      setPurgeCount(null)


      /*
       * 삭제 후 첫 페이지부터 다시 조회.
       */
      setLoading(true)

      setPage(0)

      setRefreshKey(
        previous =>
          previous + 1
      )

    } catch (err) {

      setPurgeMessage(
        err.message
      )

    } finally {

      setPurging(false)
    }
  }


  /**
   * 이전 Page 이동.
   */
  function handlePreviousPage() {

    if (page === 0) {
      return
    }


    setLoading(true)


    setPage(
      previous =>
        Math.max(
          previous - 1,
          0
        )
    )
  }


  /**
   * 다음 Page 이동.
   */
  function handleNextPage() {

    if (!hasNext) {
      return
    }


    setLoading(true)


    setPage(
      previous =>
        previous + 1
    )
  }


  /*
   * 최초 조회 및 페이지 이동 중 Loading.
   */
  if (loading) {

    return (
      <main className="admin-audit-page">

        Audit Log를 불러오는 중입니다.

      </main>
    )
  }


  return (
    <main className="admin-audit-page">


      {/* =========================
          Header
          ========================= */}

      <header className="admin-audit-header">

        <p>
          Security / Audit
        </p>


        <h1>
          Audit Log
        </h1>


        <span>
          로그인과 관리자 작업 이력을
          실제 AUDIT_LOG 기준으로 조회합니다.
        </span>

      </header>


      {/* =========================
          Error
          ========================= */}

      {error && (

        <div className="admin-audit-error">

          {error}

        </div>

      )}


      {/* =========================
          Summary
          ========================= */}

      <section className="admin-audit-summary">


        <article>

          <span>
            전체 Audit
          </span>

          <strong>
            {
              totalElements
                .toLocaleString()
            }
          </strong>

        </article>


        <article>

          <span>
            현재 Page LOGIN
          </span>

          <strong>
            {
              logs.filter(
                log =>
                  log.actionType === 'LOGIN'
              ).length
            }
          </strong>

        </article>


        <article>

          <span>
            현재 Page ROLE_CHANGE
          </span>

          <strong>
            {
              logs.filter(
                log =>
                  log.actionType === 'ROLE_CHANGE'
              ).length
            }
          </strong>

        </article>


        <article>

          <span>
            현재 Page Action 유형
          </span>

          <strong>
            {actionTypes.length}
          </strong>

        </article>


      </section>


      {/* =========================
          Search / Filter
          ========================= */}

      <section className="admin-audit-filter">


        <input
          type="text"
          value={keyword}
          placeholder="Login ID, 대상, 설명 검색"
          onChange={
            event =>
              setKeyword(
                event.target.value
              )
          }
        />


        <select
          value={actionType}
          onChange={
            event =>
              setActionType(
                event.target.value
              )
          }
        >

          <option value="ALL">
            전체 Action
          </option>


          {actionTypes.map(
            type => (

              <option
                key={type}
                value={type}
              >
                {type}
              </option>

            )
          )}

        </select>


      </section>


      {/* =========================
          Audit 보존 관리
          ========================= */}

      <section className="admin-audit-purge">


        <div>

          <h2>
            Audit 보존 관리
          </h2>


          <p>
            선택한 날짜 00:00 이전의
            Audit Log만 일괄 정리합니다.
          </p>

        </div>


        <div className="admin-audit-purge-controls">


          <input
            type="date"
            value={purgeBefore}
            max={today}
            onChange={
              event => {

                setPurgeBefore(
                  event.target.value
                )

                /*
                 * 기준 날짜가 변경되면
                 * 기존 Preview는 사용할 수 없다.
                 */
                setPurgeCount(null)

                setPurgeMessage('')
              }
            }
          />


          <button
            type="button"
            disabled={
              !purgeBefore
              ||
              purging
            }
            onClick={
              handlePurgePreview
            }
          >
            삭제 대상 확인
          </button>


          <button
            type="button"
            className="danger"
            disabled={
              purging
              ||
              purgeCount == null
              ||
              purgeCount <= 0
            }
            onClick={
              handlePurge
            }
          >
            {
              purging
                ? '정리 중...'
                : '오래된 기록 정리'
            }
          </button>


        </div>


        {purgeCount !== null && (

          <p className="audit-purge-count">

            실제 삭제 대상:

            <strong>
              {' '}
              {
                purgeCount
                  .toLocaleString()
              }
              건
            </strong>

          </p>

        )}


        {purgeMessage && (

          <p className="audit-purge-message">

            {purgeMessage}

          </p>

        )}


      </section>


      {/* =========================
          Audit Table
          ========================= */}

      <section className="admin-audit-panel">


        <div className="admin-audit-table-wrapper">


          <table>


            <thead>

              <tr>

                <th>
                  ID
                </th>

                <th>
                  시간
                </th>

                <th>
                  수행 사용자
                </th>

                <th>
                  Action
                </th>

                <th>
                  Target
                </th>

                <th>
                  Request URI
                </th>

                <th>
                  설명
                </th>

                <th>
                  IP
                </th>

              </tr>

            </thead>


            <tbody>


              {
                filteredLogs.length === 0
                  ? (

                      <tr>

                        <td
                          colSpan="8"
                          className="admin-audit-empty"
                        >
                          표시할 Audit Log가 없습니다.
                        </td>

                      </tr>

                    )
                  : filteredLogs.map(
                      log => (

                        <tr
                          key={
                            log.auditId
                          }
                        >


                          <td>

                            {
                              log.auditId
                            }

                          </td>


                          <td>

                            {
                              formatDateTime(
                                log.createdAt
                              )
                            }

                          </td>


                          <td>


                            <strong>

                              {
                                log.loginId
                                ?? '-'
                              }

                            </strong>


                            <div className="audit-user-id">

                              USER #

                              {
                                log.userId
                                ?? '-'
                              }

                            </div>


                          </td>


                          <td>


                            <span
                              className={
                                'audit-action '
                                + (
                                  log.actionType
                                    ?.toLowerCase()
                                  ?? ''
                                )
                              }
                            >

                              {
                                log.actionType
                                ?? '-'
                              }

                            </span>


                          </td>


                          <td>

                            {
                              log.targetType
                              ?? '-'
                            }


                            {
                              log.targetId != null
                                ? (
                                    ' #'
                                    + log.targetId
                                  )
                                : ''
                            }

                          </td>


                          <td className="audit-uri">

                            {
                              log.requestUri
                              ?? '-'
                            }

                          </td>


                          <td className="audit-description">

                            {
                              log.description
                              ?? '-'
                            }

                          </td>


                          <td>

                            {
                              log.ipAddress
                              ?? '-'
                            }

                          </td>


                        </tr>

                      )
                    )
              }


            </tbody>


          </table>


        </div>


        {/* =========================
            Pagination
            ========================= */}

        <div className="admin-audit-pagination">


          <button
            type="button"
            disabled={
              page === 0
            }
            onClick={
              handlePreviousPage
            }
          >
            이전
          </button>


          <span>

            {
              totalPages === 0
                ? '0 페이지'
                : `${page + 1} 페이지 / ${totalPages}`
            }

          </span>


          <button
            type="button"
            disabled={
              !hasNext
            }
            onClick={
              handleNextPage
            }
          >
            다음
          </button>


        </div>


      </section>


    </main>
  )
}


export default AdminAuditPage