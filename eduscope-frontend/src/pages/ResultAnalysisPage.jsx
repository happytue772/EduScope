import {
  useEffect,
  useMemo,
  useState
} from 'react'

import {
  Link,
  useSearchParams
} from 'react-router-dom'

import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from 'recharts'

import {
  getCourses
} from '../api/courseAnalysisApi'

import {
  getResultAnalysis
} from '../api/resultAnalysisApi'

import '../styles/resultAnalysis.css'


const METRICS = {

  avgClickCount: {
    label: '학생 평균 클릭',
    unit: '회/명'
  },

  avgActiveDayCount: {
    label: '평균 활동 일수',
    unit: '일'
  },

  totalClickCount: {
    label: '총 클릭',
    unit: '회'
  },

  studentCount: {
    label: '학생 수',
    unit: '명'
  }
}


/**
 * 최종 결과별 학습활동 비교 화면.
 */
function ResultAnalysisPage() {

  const [
    searchParams,
    setSearchParams
  ] = useSearchParams()

  const courseIdFromUrl =
    searchParams.get('courseId')


  const [courses, setCourses] =
    useState([])

  const [
    selectedCourseId,
    setSelectedCourseId
  ] = useState('')

  const [
    selectedMetric,
    setSelectedMetric
  ] = useState('avgClickCount')

  const [analysis, setAnalysis] =
    useState(null)

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState(null)


  /**
   * 강의 목록 조회.
   */
  useEffect(() => {

    async function loadCourses() {

      try {

        const data =
          await getCourses()

        setCourses(data)

        if (data.length === 0) {
          return
        }

        const exists =
          courseIdFromUrl
          &&
          data.some(
            (course) =>
              String(
                course.coursePresentationId
              )
              === courseIdFromUrl
          )

        const initialId =
          exists
            ? courseIdFromUrl
            : String(
                data[0]
                  .coursePresentationId
              )

        setSelectedCourseId(
          initialId
        )

      } catch (err) {

        setError(err.message)

      }
    }

    loadCourses()

  }, [])


  /**
   * 선택 강의 성과 비교 조회.
   */
  useEffect(() => {

    if (!selectedCourseId) {
      return
    }

    async function loadAnalysis() {

      try {

        setLoading(true)

        const data =
          await getResultAnalysis(
            selectedCourseId
          )

        setAnalysis(data)
        setError(null)

      } catch (err) {

        setError(err.message)

      } finally {

        setLoading(false)
      }
    }

    loadAnalysis()

  }, [selectedCourseId])


  const metric =
    METRICS[selectedMetric]


  const chartData =
    useMemo(() => {

      return (
        analysis?.resultActivities
        ?? []
      ).map(
        (row) => ({

          finalResult:
            row.finalResult,

          value:
            Number(
              row[selectedMetric]
            )
        })
      )

    }, [
      analysis,
      selectedMetric
    ])


  /**
   * 선택 지표의 가장 높은 그룹.
   */
  const highestGroup =
    useMemo(() => {

      const rows =
        analysis?.resultActivities
        ?? []

      if (rows.length === 0) {
        return null
      }

      return rows.reduce(
        (maxRow, currentRow) =>
          Number(
            currentRow[
              selectedMetric
            ]
          )
          >
          Number(
            maxRow[
              selectedMetric
            ]
          )
            ? currentRow
            : maxRow
      )

    }, [
      analysis,
      selectedMetric
    ])


  /**
   * 최종 결과별 활동 데이터를 CSV로 저장.
   */
  function downloadCsv() {

    const rows =
      analysis?.resultActivities
      ?? []

    if (rows.length === 0) {
      return
    }

    const header = [
      'finalResult',
      'studentCount',
      'totalClickCount',
      'avgClickCount',
      'avgActiveDayCount'
    ]

    const dataRows =
      rows.map(
        (row) => [
          row.finalResult,
          row.studentCount,
          row.totalClickCount,
          row.avgClickCount,
          row.avgActiveDayCount
        ]
      )

    const csv =
      [
        header,
        ...dataRows
      ]
        .map(
          (row) =>
            row.join(',')
        )
        .join('\n')

    const blob =
      new Blob(
        [
          '\uFEFF',
          csv
        ],
        {
          type:
            'text/csv;charset=utf-8;'
        }
      )

    const url =
      URL.createObjectURL(blob)

    const link =
      document.createElement('a')

    link.href = url

    link.download =
      'result_analysis_'
      + selectedCourseId
      + '.csv'

    document.body.appendChild(link)

    link.click()

    document.body.removeChild(link)

    URL.revokeObjectURL(url)
  }


  if (error) {

    return (
      <main className="result-page">

        <div className="page-error">
          {error}
        </div>

      </main>
    )
  }


  return (
    <main className="result-page">

      <header className="result-header">

        <div>

          <p>
            학습활동과 최종성과 비교
          </p>

          <h1>
            성과 비교
          </h1>

        </div>

        <Link
          to={
            selectedCourseId
              ? '/courses?courseId='
                + selectedCourseId
              : '/courses'
          }
        >
          강의 분석으로 돌아가기
        </Link>

      </header>


      <section className="result-controls">

        <div>

          <label>
            강의
          </label>

          <select
            value={selectedCourseId}
            onChange={(event) => {

              const id =
                event.target.value

              setSelectedCourseId(id)

              setSearchParams({
                courseId: id
              })
            }}
          >

            {courses.map(
              (course) => (

                <option
                  key={
                    course.coursePresentationId
                  }
                  value={
                    course.coursePresentationId
                  }
                >

                  {course.codeModule}
                  {' / '}
                  {course.codePresentation}

                </option>

              )
            )}

          </select>

        </div>


        <div>

          <label>
            비교 지표
          </label>

          <select
            value={selectedMetric}
            onChange={(event) =>
              setSelectedMetric(
                event.target.value
              )
            }
          >

            <option value="avgClickCount">
              학생 평균 클릭
            </option>

            <option value="avgActiveDayCount">
              평균 활동 일수
            </option>

            <option value="totalClickCount">
              총 클릭
            </option>

            <option value="studentCount">
              학생 수
            </option>

          </select>

        </div>


        <button
          type="button"
          onClick={downloadCsv}
        >
          CSV 다운로드
        </button>

      </section>


      {loading && (

        <div className="analysis-loading">
          성과 데이터를
          불러오는 중입니다.
        </div>

      )}


      {!loading && analysis && (
        <>

          <section className="result-guide">

            <p>
              Pass, Fail, Withdrawn,
              Distinction은 OULAD 원본의
              final_result 값입니다.
            </p>

            <p>
              평가분석의 score &lt; 40 기준과
              이 화면의 Fail은 서로 다른 개념입니다.
              여기서는 실제 최종 결과를 사용합니다.
            </p>

          </section>


          {/* 최종결과 전체 분포 */}
          {analysis.courseResult && (

            <section className="result-kpi-grid">

              <ResultKpi
                title="전체 학생"
                value={
                  analysis
                    .courseResult
                    .studentCount
                }
                unit="명"
              />

              <ResultKpi
                title="Pass"
                value={
                  analysis
                    .courseResult
                    .passCount
                }
                unit="명"
              />

              <ResultKpi
                title="Fail"
                value={
                  analysis
                    .courseResult
                    .failCount
                }
                unit="명"
              />

              <ResultKpi
                title="Withdrawn"
                value={
                  analysis
                    .courseResult
                    .withdrawnCount
                }
                unit="명"
              />

              <ResultKpi
                title="Distinction"
                value={
                  analysis
                    .courseResult
                    .distinctionCount
                }
                unit="명"
              />

            </section>

          )}


          {/* 선택 지표 Insight */}
          {highestGroup && (

            <section className="result-insight">

              <span>
                현재 지표가 가장 높은 그룹
              </span>

              <strong>
                {highestGroup.finalResult}
              </strong>

              <p>
                {
                  Number(
                    highestGroup[
                      selectedMetric
                    ]
                  )
                  .toLocaleString(
                    undefined,
                    {
                      maximumFractionDigits: 2
                    }
                  )
                }
                {' '}
                {metric.unit}
              </p>

            </section>

          )}


          {/* 활동 비교 차트 */}
          <section className="result-panel">

            <h2>
              최종 결과별 {metric.label}
            </h2>

            <p>
              세로축 단위:
              {' '}
              {metric.unit}
            </p>


            <ResponsiveContainer
              width="100%"
              height={360}
            >

              <BarChart
                data={chartData}
              >

                <CartesianGrid
                  stroke="#D9E6F2"
                  strokeDasharray="4 4"
                  vertical={false}
                />

                <XAxis
                  dataKey="finalResult"
                  stroke="#607D98"
                  tick={{
                    fill: '#607D98'
                  }}
                />

                <YAxis
                  stroke="#607D98"
                  tick={{
                    fill: '#607D98'
                  }}
                  tickFormatter={
                    (value) =>
                      Number(value)
                        .toLocaleString()
                  }
                  label={{
                    value:
                      metric.label
                      + ' ('
                      + metric.unit
                      + ')',
                    angle: -90,
                    position:
                      'insideLeft',
                    fill: '#607D98'
                  }}
                />

                <Tooltip
                  contentStyle={{
                    backgroundColor:
                      '#FFFFFF',
                    border:
                      '1px solid #C9DCEC',
                    borderRadius:
                      '10px'
                  }}
                  formatter={
                    (value) => [
                      Number(value)
                        .toLocaleString(
                          undefined,
                          {
                            maximumFractionDigits: 2
                          }
                        )
                      + ' '
                      + metric.unit,
                      metric.label
                    ]
                  }
                />

                <Legend />

                <Bar
                  dataKey="value"
                  name={
                    metric.label
                    + ' ('
                    + metric.unit
                    + ')'
                  }
                  fill="#2F80ED"
                  radius={[
                    7,
                    7,
                    0,
                    0
                  ]}
                  maxBarSize={70}
                />

              </BarChart>

            </ResponsiveContainer>

          </section>


          {/* 상세 테이블 */}
          <section className="result-panel">

            <h2>
              최종 결과 그룹 상세
            </h2>

            <div className="result-table-wrapper">

              <table>

                <thead>

                  <tr>
                    <th>최종 결과</th>
                    <th>학생 수</th>
                    <th>총 클릭</th>
                    <th>학생 평균 클릭</th>
                    <th>평균 활동 일수</th>
                  </tr>

                </thead>


                <tbody>

                  {
                    analysis
                      .resultActivities
                      .map(
                        (row) => (

                          <tr
                            key={
                              row.finalResult
                            }
                          >

                            <td>
                              {row.finalResult}
                            </td>

                            <td>
                              {
                                Number(
                                  row.studentCount
                                )
                                .toLocaleString()
                              }명
                            </td>

                            <td>
                              {
                                Number(
                                  row.totalClickCount
                                )
                                .toLocaleString()
                              }회
                            </td>

                            <td>
                              {
                                Number(
                                  row.avgClickCount
                                )
                                .toLocaleString(
                                  undefined,
                                  {
                                    maximumFractionDigits: 2
                                  }
                                )
                              }회/명
                            </td>

                            <td>
                              {
                                Number(
                                  row.avgActiveDayCount
                                )
                                .toLocaleString(
                                  undefined,
                                  {
                                    maximumFractionDigits: 2
                                  }
                                )
                              }일
                            </td>

                          </tr>

                        )
                      )
                  }

                </tbody>

              </table>

            </div>

          </section>


          <details className="analysis-source">

            <summary>
              분석 데이터 정보
            </summary>

            <p>
              Activity Result Job:
              {' '}
              #
              {
                analysis
                  .activityResultJobId
                ?? '-'
              }
            </p>

            <p>
              Course Result Job:
              {' '}
              #
              {
                analysis
                  .courseResultJobId
                ?? '-'
              }
            </p>

            <p>
              학습활동 비교는
              ACTIVITY_RESULT_STAT,
              최종성과 분포는
              COURSE_RESULT_STAT을
              사용합니다.
            </p>

          </details>

        </>
      )}

    </main>
  )
}


function ResultKpi({
  title,
  value,
  unit
}) {

  return (
    <article className="result-kpi">

      <span>
        {title}
      </span>

      <strong>
        {
          Number(value)
            .toLocaleString()
        }
      </strong>

      <small>
        {unit}
      </small>

    </article>
  )
}


export default ResultAnalysisPage