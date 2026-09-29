import {
  useEffect,
  useState
} from 'react'

import {
  Link,
  useNavigate
} from 'react-router-dom'

import {
  getSystemHealth
} from '../api/systemHealthApi'

import '../styles/systemHealth.css'


/**
 * 밀리초를 읽기 쉬운 실행시간으로 변환한다.
 */
function formatUptime(
  milliseconds
) {

  if (
    milliseconds === null
    ||
    milliseconds === undefined
  ) {
    return '-'
  }


  const totalSeconds =
    Math.floor(
      milliseconds / 1000
    )


  const days =
    Math.floor(
      totalSeconds / 86400
    )


  const hours =
    Math.floor(
      (totalSeconds % 86400)
      /
      3600
    )


  const minutes =
    Math.floor(
      (totalSeconds % 3600)
      /
      60
    )


  if (days > 0) {

    return (
      `${days}일 ${hours}시간 ${minutes}분`
    )
  }


  if (hours > 0) {

    return (
      `${hours}시간 ${minutes}분`
    )
  }


  return (
    `${minutes}분`
  )
}


/**
 * 숫자 천 단위 표시.
 */
function formatNumber(
  value
) {

  if (
    value === null
    ||
    value === undefined
  ) {
    return '-'
  }


  return Number(value)
    .toLocaleString(
      'ko-KR'
    )
}


/**
 * 날짜/시간 표시.
 */
function formatDateTime(
  value
) {

  if (!value) {
    return '-'
  }


  const date =
    new Date(value)


  if (
    Number.isNaN(
      date.getTime()
    )
  ) {
    return value
  }


  return date.toLocaleString(
    'ko-KR'
  )
}


/**
 * 상태별 CSS Class.
 */
function getStatusClass(
  status
) {

  switch (status) {

    case 'UP':
    case 'SUCCESS':
      return 'system-health-status-success'


    case 'RUNNING':
      return 'system-health-status-running'


    case 'PENDING':
      return 'system-health-status-pending'


    case 'DOWN':
    case 'FAILED':
      return 'system-health-status-failed'


    default:
      return 'system-health-status-neutral'
  }
}


/**
 * API 오류 메시지 변환.
 */
function getHealthErrorMessage(
  error
) {

  if (
    error?.status === 401
  ) {

    return (
      '로그인 세션이 만료되었거나 인증되지 않았습니다.'
    )
  }


  if (
    error?.status === 403
  ) {

    return (
      '관리자 조회 권한이 필요한 기능입니다.'
    )
  }


  return (
    error?.message
    ||
    '시스템 상태 조회 중 오류가 발생했습니다.'
  )
}


/**
 * ADMIN / DEMO_ADMIN 조회용 System Health 화면.
 *
 * 실제 Backend API 값만 사용한다.
 */
function SystemHealthPage() {

  const navigate =
    useNavigate()


  const [
    health,
    setHealth
  ] = useState(null)


  const [
    loading,
    setLoading
  ] = useState(true)


  const [
    refreshing,
    setRefreshing
  ] = useState(false)


  const [
    error,
    setError
  ] = useState('')


  /**
   * 최초 진입 시 실제 System Health 조회.
   */
  useEffect(
    () => {

      let cancelled =
        false


      getSystemHealth()
        .then(
          data => {

            if (cancelled) {
              return
            }


            setHealth(
              data
            )


            setError(
              ''
            )
          }
        )
        .catch(
          requestError => {

            if (cancelled) {
              return
            }


            setError(
              getHealthErrorMessage(
                requestError
              )
            )
          }
        )
        .finally(
          () => {

            if (cancelled) {
              return
            }


            setLoading(
              false
            )
          }
        )


      return () => {

        cancelled =
          true
      }

    },
    []
  )


  /**
   * 사용자가 직접 상태 새로고침.
   */
  async function handleRefresh() {

    try {

      setRefreshing(
        true
      )


      const data =
        await getSystemHealth()


      setHealth(
        data
      )


      setError(
        ''
      )

    } catch (
      requestError
    ) {

      setError(
        getHealthErrorMessage(
          requestError
        )
      )

    } finally {

      setRefreshing(
        false
      )
    }
  }


  /**
   * 이전 화면으로 이동.
   */
  function handleBack() {

    navigate(
      -1
    )
  }


  if (
    loading
    &&
    !health
  ) {

    return (

      <main className="system-health-page">

        <div className="system-health-loading">

          <div className="system-health-spinner" />

          <span>
            시스템 상태를 확인하고 있습니다.
          </span>

        </div>

      </main>
    )
  }


  const application =
    health?.application


  const database =
    health?.database


  const runtime =
    health?.runtime


  const latestJob =
    health?.latestAnalysisJob


  return (

    <main className="system-health-page">

      {/* =====================================================
          Page Navigation
          ===================================================== */}

      <div className="system-health-page-nav">

        <button
          type="button"
          className="system-health-back-button"
          onClick={
            handleBack
          }
        >
          <span>
            ←
          </span>

          이전 페이지
        </button>


        <Link
          to="/"
          className="system-health-home-link"
        >
          대시보드로
        </Link>

      </div>


      {/* =====================================================
          Header
          ===================================================== */}

      <header className="system-health-header">

        <div className="system-health-heading">

          <span className="system-health-eyebrow">
            ADMIN / DEMO_ADMIN · SYSTEM MONITORING
          </span>


          <h1>
            시스템 상태
          </h1>


          <p>
            EduScope 애플리케이션과 Oracle,
            JVM 및 최신 분석 작업 상태를 확인합니다.
          </p>

        </div>


        <button
          type="button"
          className="system-health-refresh-button"
          onClick={
            handleRefresh
          }
          disabled={
            refreshing
          }
        >

          <span
            className={
              refreshing
                ? 'system-health-refresh-icon rotating'
                : 'system-health-refresh-icon'
            }
          >
            ↻
          </span>


          {
            refreshing
              ? '확인 중...'
              : '상태 새로고침'
          }

        </button>

      </header>


      {/* =====================================================
          Error
          ===================================================== */}

      {
        error
        &&
        (

          <div className="system-health-error">

            <span>
              !
            </span>


            <p>
              {error}
            </p>

          </div>

        )
      }


      {/* =====================================================
          Status Summary
          ===================================================== */}

      <section className="system-health-summary">

        {/* Application */}

        <article className="system-health-summary-card">

          <div className="system-health-summary-icon lavender">
            A
          </div>


          <div className="system-health-summary-content">

            <div className="system-health-card-top">

              <span>
                Application
              </span>


              <span
                className={
                  `
                  system-health-status-badge
                  ${getStatusClass(
                    application?.status
                  )}
                  `
                }
              >
                {
                  application?.status
                  ??
                  '-'
                }
              </span>

            </div>


            <strong>
              {
                application?.applicationName
                ??
                '-'
              }
            </strong>


            <p>
              Spring Boot Application
            </p>

          </div>

        </article>


        {/* Database */}

        <article className="system-health-summary-card">

          <div className="system-health-summary-icon blue">
            DB
          </div>


          <div className="system-health-summary-content">

            <div className="system-health-card-top">

              <span>
                Oracle
              </span>


              <span
                className={
                  `
                  system-health-status-badge
                  ${getStatusClass(
                    database?.status
                  )}
                  `
                }
              >
                {
                  database?.status
                  ??
                  '-'
                }
              </span>

            </div>


            <strong>
              {
                database?.databaseProduct
                ??
                '-'
              }
            </strong>


            <p>
              응답시간 {
                database?.responseTimeMs
                ??
                '-'
              } ms
            </p>

          </div>

        </article>


        {/* JVM */}

        <article className="system-health-summary-card">

          <div className="system-health-summary-icon beige">
            J
          </div>


          <div className="system-health-summary-content">

            <div className="system-health-card-top">

              <span>
                Java Runtime
              </span>


              <span className="system-health-live-dot" />

            </div>


            <strong>
              {
                runtime?.javaVersion
                ??
                '-'
              }
            </strong>


            <p>
              Java Version
            </p>

          </div>

        </article>


        {/* Latest Job */}

        <article className="system-health-summary-card">

          <div className="system-health-summary-icon rose">
            J
          </div>


          <div className="system-health-summary-content">

            <div className="system-health-card-top">

              <span>
                Latest Job
              </span>


              <span
                className={
                  `
                  system-health-status-badge
                  ${getStatusClass(
                    latestJob?.status
                  )}
                  `
                }
              >
                {
                  latestJob?.status
                  ??
                  'NONE'
                }
              </span>

            </div>


            <strong>

              {
                latestJob?.jobId
                  ? `#${latestJob.jobId}`
                  : '-'
              }

            </strong>


            <p>
              {
                latestJob?.analysisType
                ??
                '분석 이력 없음'
              }
            </p>

          </div>

        </article>

      </section>


      {/* =====================================================
          Database / Runtime
          ===================================================== */}

      <section className="system-health-detail-grid">

        {/* Oracle */}

        <article className="system-health-panel">

          <div className="system-health-panel-header">

            <div>

              <span>
                DATABASE
              </span>


              <h2>
                Oracle 연결 상태
              </h2>

            </div>


            <div
              className={
                `
                system-health-panel-indicator
                ${getStatusClass(
                  database?.status
                )}
                `
              }
            />

          </div>


          <div className="system-health-info-list">

            <InfoRow
              label="연결 상태"
              value={
                database?.status
              }
              status
            />


            <InfoRow
              label="DB 제품"
              value={
                database?.databaseProduct
              }
            />


            <InfoRow
              label="DB 버전"
              value={
                database?.databaseVersion
              }
            />


            <InfoRow
              label="응답 시간"
              value={
                database?.responseTimeMs !== null
                &&
                database?.responseTimeMs !== undefined
                  ? `${database.responseTimeMs} ms`
                  : '-'
              }
            />

          </div>

        </article>


        {/* JVM */}

        <article className="system-health-panel">

          <div className="system-health-panel-header">

            <div>

              <span>
                RUNTIME
              </span>


              <h2>
                JVM 실행 상태
              </h2>

            </div>


            <div className="system-health-runtime-mark">
              JVM
            </div>

          </div>


          <div className="system-health-info-list">

            <InfoRow
              label="Java Version"
              value={
                runtime?.javaVersion
              }
            />


            <InfoRow
              label="실행 시간"
              value={
                formatUptime(
                  runtime?.uptimeMs
                )
              }
            />


            <InfoRow
              label="Processor"
              value={
                runtime?.availableProcessors
              }
            />


            <InfoRow
              label="Memory"
              value={
                runtime
                  ? (
                      `${formatNumber(
                        runtime.usedMemoryMb
                      )} MB / ${formatNumber(
                        runtime.maxMemoryMb
                      )} MB`
                    )
                  : '-'
              }
            />

          </div>

        </article>

      </section>


      {/* =====================================================
          Latest Job
          ===================================================== */}

      <section className="system-health-panel system-health-job-panel">

        <div className="system-health-panel-header">

          <div>

            <span>
              ANALYSIS JOB
            </span>


            <h2>
              최신 분석 작업
            </h2>

          </div>


          {
            latestJob
            &&
            (

              <span
                className={
                  `
                  system-health-status-badge
                  system-health-job-status
                  ${getStatusClass(
                    latestJob.status
                  )}
                  `
                }
              >
                {latestJob.status}
              </span>

            )
          }

        </div>


        {
          latestJob
            ? (

              <div className="system-health-job-grid">

                <InfoBlock
                  label="Job ID"
                  value={
                    `#${latestJob.jobId}`
                  }
                />


                <InfoBlock
                  label="분석 유형"
                  value={
                    latestJob.analysisType
                  }
                />


                <InfoBlock
                  label="Imported"
                  value={
                    latestJob.resultImportedYn
                    ??
                    '-'
                  }
                />


                <InfoBlock
                  label="Output Records"
                  value={
                    formatNumber(
                      latestJob.outputRecordCount
                    )
                  }
                />


                <InfoBlock
                  label="Processing"
                  value={
                    latestJob.processingTimeMs !== null
                    &&
                    latestJob.processingTimeMs !== undefined
                      ? (
                          `${formatNumber(
                            latestJob.processingTimeMs
                          )} ms`
                        )
                      : '-'
                  }
                />


                <InfoBlock
                  label="완료 시간"
                  value={
                    formatDateTime(
                      latestJob.finishedAt
                    )
                  }
                />


                <InfoBlock
                  label="Error Step"
                  value={
                    latestJob.errorStep
                    ??
                    '-'
                  }
                />

              </div>

            )
            : (

              <div className="system-health-empty">
                조회 가능한 Analysis Job이 없습니다.
              </div>

            )
        }

      </section>


      {/* =====================================================
          Footer
          ===================================================== */}

      <footer className="system-health-footer">

        <span className="system-health-footer-dot" />


        <span>
          마지막 확인
        </span>


        <strong>
          {
            formatDateTime(
              health?.checkedAt
            )
          }
        </strong>

      </footer>

    </main>
  )
}


/**
 * 상세 상태 한 행.
 */
function InfoRow({
  label,
  value,
  status = false
}) {

  return (

    <div className="system-health-info-row">

      <span>
        {label}
      </span>


      {
        status
          ? (

            <strong
              className={
                getStatusClass(
                  value
                )
              }
            >
              {
                value
                ??
                '-'
              }
            </strong>

          )
          : (

            <strong>
              {
                value
                ??
                '-'
              }
            </strong>

          )
      }

    </div>
  )
}


/**
 * 최신 Job 정보 블록.
 */
function InfoBlock({
  label,
  value
}) {

  return (

    <div className="system-health-info-block">

      <span>
        {label}
      </span>


      <strong>
        {
          value
          ??
          '-'
        }
      </strong>

    </div>
  )
}


export default SystemHealthPage