import {
  useEffect,
  useMemo,
  useState
} from 'react'

import {
  getDashboardSummary
} from '../api/dashboardApi'

import '../styles/dashboard.css'


/**
 * EduScope 메인 Dashboard.
 *
 * Oracle에 적재된 실제 OULAD 집계값을
 * Spring Boot Dashboard API를 통해 조회한다.
 */
function DashboardPage() {

  const [
    summary,
    setSummary
  ] = useState(null)


  const [
    loading,
    setLoading
  ] = useState(true)


  const [
    error,
    setError
  ] = useState(null)


  useEffect(() => {

    async function loadDashboard() {

      try {

        /*
         * 현재 실제 OULAD Dataset ID.
         * 임의 데이터를 생성하지 않는다.
         */
        const data =
          await getDashboardSummary(1)


        setSummary(
          data
        )

      } catch (err) {

        setError(
          err.message
        )

      } finally {

        setLoading(
          false
        )
      }
    }


    loadDashboard()

  }, [])


  /**
   * 최종 결과 전체 수강 건수.
   * 실제 API 값만 사용하여 계산한다.
   */
  const resultTotal =
    useMemo(() => {

      if (!summary) {

        return 0
      }


      return (
        summary.passCount
        +
        summary.failCount
        +
        summary.withdrawnCount
        +
        summary.distinctionCount
      )

    }, [
      summary
    ])


  /**
   * 결과별 비율 계산.
   */
  function getRate(
    count
  ) {

    if (
      resultTotal === 0
    ) {

      return 0
    }


    return (
      (
        count
        /
        resultTotal
      )
      *
      100
    )
      .toFixed(1)
  }


  if (loading) {

    return (

      <div className="state-message">

        Dashboard 데이터를 불러오는 중입니다.

      </div>

    )
  }


  if (error) {

    return (

      <div className="state-message error">

        {error}

      </div>

    )
  }


  if (!summary) {

    return (

      <div className="state-message">

        Dashboard 데이터가 없습니다.

      </div>

    )
  }


  return (

    <div className="dashboard-page">

      {/* =========================
          Dashboard Header
          ========================= */}

      <header className="dashboard-header">

        <div>

          <p className="dashboard-subtitle">

            OULAD 학습 데이터 분석

          </p>


          <h1>

            EduScope Dashboard

          </h1>

        </div>


        <div className="dataset-badge">

          <span className="dataset-badge-dot" />

          Dataset #1

        </div>

      </header>


      {/* =========================
          상단 KPI
          ========================= */}

      <section className="summary-grid">

        <SummaryCard
          title="원본 CSV"
          value={
            summary.datasetFileCount
          }
          unit="개"
        />


        <SummaryCard
          title="강의 개설"
          value={
            summary.coursePresentationCount
          }
          unit="개"
        />


        <SummaryCard
          title="고유 학생"
          value={
            summary.uniqueStudentCount
          }
          unit="명"
        />


        <SummaryCard
          title="수강 건수"
          value={
            summary.enrollmentCount
          }
          unit="건"
        />


        <SummaryCard
          title="총 학습 클릭"
          value={
            summary.totalClickCount
          }
          unit="회"
        />

      </section>


      {/* =========================
          최종 결과 분포
          ========================= */}

      <section className="dashboard-section">

        <div className="section-heading">

          <div>

            <p className="section-label">

              학습 성과

            </p>


            <h2>

              최종 결과 분포

            </h2>

          </div>


          <span className="section-total">

            총 {resultTotal.toLocaleString()}건

          </span>

        </div>


        <div className="result-grid">

          <ResultCard
            title="Pass"
            value={
              summary.passCount
            }
            rate={
              getRate(
                summary.passCount
              )
            }
            tone="pass"
          />


          <ResultCard
            title="Fail"
            value={
              summary.failCount
            }
            rate={
              getRate(
                summary.failCount
              )
            }
            tone="fail"
          />


          <ResultCard
            title="Withdrawn"
            value={
              summary.withdrawnCount
            }
            rate={
              getRate(
                summary.withdrawnCount
              )
            }
            tone="withdrawn"
          />


          <ResultCard
            title="Distinction"
            value={
              summary.distinctionCount
            }
            rate={
              getRate(
                summary.distinctionCount
              )
            }
            tone="distinction"
          />

        </div>

      </section>


      {/* =========================
          데이터 출처
          ========================= */}

      <section className="dashboard-section">

        <div className="section-heading">

          <div>

            <p className="section-label">

              분석 기준

            </p>


            <h2>

              Dashboard 데이터 출처

            </h2>

          </div>

        </div>


        <div className="analysis-info">

          <div>

            <span>

              강의 활동 분석 Job

            </span>


            <strong>

              #{summary.courseActivityJobId}

            </strong>

          </div>


          <div>

            <span>

              강의 결과 분석 Job

            </span>


            <strong>

              #{summary.courseResultJobId}

            </strong>

          </div>

        </div>

      </section>

    </div>
  )
}


/**
 * 상단 KPI 카드.
 */
function SummaryCard({
  title,
  value,
  unit
}) {

  return (

    <article className="summary-card">

      <div className="summary-card-accent" />


      <span className="card-title">

        {title}

      </span>


      <div className="card-value">

        {value.toLocaleString()}


        <span>

          {unit}

        </span>

      </div>

    </article>
  )
}


/**
 * 최종 결과 카드.
 *
 * tone은 표시 스타일에만 사용하며
 * 실제 데이터 값에는 영향을 주지 않는다.
 */
function ResultCard({
  title,
  value,
  rate,
  tone
}) {

  return (

    <article
      className={
        `result-card result-card-${tone}`
      }
    >

      <div className="result-card-header">

        <span>

          {title}

        </span>


        <strong>

          {rate}%

        </strong>

      </div>


      <div className="result-value">

        {value.toLocaleString()}

        <span>
          건
        </span>

      </div>


      <div className="result-bar">

        <div
          className="result-bar-value"
          style={{
            width:
              `${rate}%`
          }}
        />

      </div>

    </article>
  )
}


export default DashboardPage