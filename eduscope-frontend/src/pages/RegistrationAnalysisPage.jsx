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
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from 'recharts'

import {
  getCourses
} from '../api/courseAnalysisApi'

import {
  getRegistrationAnalysis
} from '../api/registrationAnalysisApi'

import '../styles/registrationAnalysis.css'


/**
 * 상대일 표시.
 */
function formatRelativeDay(day) {

  const value =
    Number(day)

  if (value < 0) {

    return (
      '강의 시작 '
      + Math.abs(value)
      + '일 전'
    )
  }

  if (value === 0) {
    return '강의 시작일'
  }

  return (
    '강의 시작 '
    + value
    + '일 후'
  )
}


/**
 * 평균 상대일 표시.
 */
function formatAverageDay(day) {

  if (
    day === null
    ||
    day === undefined
  ) {
    return '-'
  }

  const value =
    Number(day)

  if (value < 0) {

    return (
      '시작 '
      + Math.abs(value)
        .toFixed(1)
      + '일 전'
    )
  }

  if (value === 0) {
    return '강의 시작일'
  }

  return (
    '시작 '
    + value.toFixed(1)
    + '일 후'
  )
}


/**
 * 수강/등록 분석 화면.
 */
function RegistrationAnalysisPage() {

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
    chartMode,
    setChartMode
  ] = useState('BOTH')

  const [
    dayRange,
    setDayRange
  ] = useState('ALL')

  const [
    showTable,
    setShowTable
  ] = useState(false)

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
   * 선택 강의 분석 조회.
   */
  useEffect(() => {

    if (!selectedCourseId) {
      return
    }

    async function loadAnalysis() {

      try {

        setLoading(true)

        const data =
          await getRegistrationAnalysis(
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


  /**
   * 등록/철회 일별 데이터를
   * 하나의 그래프 데이터로 병합한다.
   */
  const chartData =
    useMemo(() => {

      const map =
        new Map()

      const registrations =
        analysis?.registrationByDay
        ?? []

      const unregistrations =
        analysis?.unregistrationByDay
        ?? []

      registrations.forEach(
        (row) => {

          const day =
            Number(
              row.relativeDay
            )

          map.set(
            day,
            {
              relativeDay: day,
              registrationCount:
                Number(row.count),
              unregistrationCount: 0
            }
          )
        }
      )

      unregistrations.forEach(
        (row) => {

          const day =
            Number(
              row.relativeDay
            )

          const current =
            map.get(day)
            ?? {
              relativeDay: day,
              registrationCount: 0,
              unregistrationCount: 0
            }

          current.unregistrationCount =
            Number(row.count)

          map.set(
            day,
            current
          )
        }
      )

      return Array
        .from(map.values())
        .sort(
          (a, b) =>
            a.relativeDay
            -
            b.relativeDay
        )

    }, [analysis])


  /**
   * 시작 전/후 필터.
   */
  const filteredChartData =
    useMemo(() => {

      if (dayRange === 'BEFORE') {

        return chartData.filter(
          (row) =>
            row.relativeDay < 0
        )
      }

      if (dayRange === 'AFTER') {

        return chartData.filter(
          (row) =>
            row.relativeDay >= 0
        )
      }

      return chartData

    }, [
      chartData,
      dayRange
    ])


  /**
   * 현재 화면 데이터 CSV.
   */
  function downloadCsv() {

    if (
      filteredChartData.length
      === 0
    ) {
      return
    }

    const header = [
      'relativeDay',
      'registrationCount',
      'unregistrationCount'
    ]

    const rows =
      filteredChartData.map(
        (row) => [
          row.relativeDay,
          row.registrationCount,
          row.unregistrationCount
        ]
      )

    const csv =
      [
        header,
        ...rows
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
      URL.createObjectURL(
        blob
      )

    const link =
      document.createElement('a')

    link.href = url

    link.download =
      'registration_analysis_'
      + selectedCourseId
      + '.csv'

    document.body.appendChild(
      link
    )

    link.click()

    document.body.removeChild(
      link
    )

    URL.revokeObjectURL(url)
  }


  if (error) {

    return (
      <main className="registration-page">

        <div className="page-error">
          {error}
        </div>

      </main>
    )
  }


  return (
    <main className="registration-page">

      <header className="registration-header">

        <div>

          <p>
            OULAD 수강 등록 및 철회 분석
          </p>

          <h1>
            수강 분석
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


      {/* 필터 */}
      <section className="registration-controls">

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
                    course
                      .coursePresentationId
                  }
                  value={
                    course
                      .coursePresentationId
                  }
                >

                  {course.codeModule}
                  {' / '}
                  {
                    course
                      .codePresentation
                  }

                </option>

              )
            )}

          </select>

        </div>


        <div>

          <label>
            그래프 표시
          </label>

          <select
            value={chartMode}
            onChange={(event) =>
              setChartMode(
                event.target.value
              )
            }
          >

            <option value="BOTH">
              등록 + 철회
            </option>

            <option value="REGISTRATION">
              등록만
            </option>

            <option value="UNREGISTRATION">
              철회만
            </option>

          </select>

        </div>


        <div>

          <label>
            상대일 범위
          </label>

          <select
            value={dayRange}
            onChange={(event) =>
              setDayRange(
                event.target.value
              )
            }
          >

            <option value="ALL">
              전체
            </option>

            <option value="BEFORE">
              강의 시작 전
            </option>

            <option value="AFTER">
              강의 시작일 이후
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
          수강 데이터를
          불러오는 중입니다.
        </div>

      )}


      {!loading && analysis && (
        <>

          <section className="registration-guide">

            <p>
              모든 날짜는 실제 달력 날짜가 아니라
              공식 강의 시작일을 0일로 한
              상대 일수입니다.
            </p>

            <p>
              음수는 강의 시작 전,
              양수는 강의 시작 후를 의미합니다.
            </p>

            <p>
              실제 등록 철회 기록과
              최종 학업 결과 Withdrawn은
              서로 다른 의미이므로
              두 수치는 다를 수 있습니다.
            </p>

          </section>


          {analysis.summary && (

            <section className="registration-kpi-grid">

              <RegistrationKpi
                title="등록 인원"
                value={
                  analysis
                    .summary
                    .registrationCount
                }
                unit="명"
              />

              <RegistrationKpi
                title="철회 인원"
                value={
                  analysis
                    .summary
                    .unregistrationCount
                }
                unit="명"
              />

              <RegistrationKpi
                title="최종 Withdrawn"
                value={
                  analysis
                    .summary
                    .withdrawnResultCount
                }
                unit="명"
              />

              <RegistrationKpi
                title="철회율"
                value={
                  (
                    Number(
                      analysis
                        .summary
                        .unregistrationRate
                    )
                    * 100
                  ).toFixed(1)
                }
                unit="%"
              />

              <RegistrationKpi
                title="평균 등록 시점"
                value={
                  formatAverageDay(
                    analysis
                      .summary
                      .avgRegistrationDay
                  )
                }
              />

              <RegistrationKpi
                title="평균 철회 시점"
                value={
                  formatAverageDay(
                    analysis
                      .summary
                      .avgUnregistrationDay
                  )
                }
              />

            </section>

          )}


          {/* 등록/철회 분포 */}
          <section className="registration-panel">

            <h2>
              등록 / 철회 상대일 분포
            </h2>

            <p>
              가로축은 공식 강의 시작일 기준
              상대 일수이며,
              세로축은 해당 날짜의 학생 수입니다.
            </p>


			<ResponsiveContainer
			  width="100%"
			  height={420}
			>
			  <LineChart
			    data={filteredChartData}
			    margin={{
			      top: 25,
			      right: 35,
			      left: 20,
			      bottom: 55
			    }}
			  >

			    <CartesianGrid
			      stroke="#D9E6F2"
			      strokeDasharray="4 4"
			      vertical={false}
			    />

			    <XAxis
			      dataKey="relativeDay"
			      type="number"
			      domain={[
			        'dataMin',
			        'dataMax'
			      ]}
			      stroke="#607D98"
			      tick={{
			        fill: '#607D98',
			        fontSize: 12
			      }}
			      label={{
			        value:
			          '강의 시작 기준 상대일 (일)',
			        position:
			          'insideBottom',
			        offset: -18,
			        fill: '#607D98'
			      }}
			    />

			    <YAxis
			      allowDecimals={false}
			      stroke="#607D98"
			      tick={{
			        fill: '#607D98',
			        fontSize: 12
			      }}
			      label={{
			        value: '학생 수 (명)',
			        angle: -90,
			        position: 'insideLeft',
			        fill: '#607D98'
			      }}
			    />

			    <Tooltip
			      contentStyle={{
			        backgroundColor: '#FFFFFF',
			        border:
			          '1px solid #C9DCEC',
			        borderRadius: '10px'
			      }}
			      labelFormatter={
			        (day) =>
			          Number(day)
			          + '일 · '
			          + formatRelativeDay(
			              Number(day)
			            )
			      }
			      formatter={
			        (value, name) => [
			          Number(value)
			            .toLocaleString()
			          + '명',
			          name
			        ]
			      }
			    />

			    {/* 범례는 위쪽으로 이동 */}
			    <Legend
			      verticalAlign="top"
			      align="center"
			      wrapperStyle={{
			        paddingBottom: '16px'
			      }}
			    />

			    <ReferenceLine
			      x={0}
			      stroke="#EB5757"
			      strokeDasharray="5 5"
			      label={{
			        value: '강의 시작',
			        fill: '#EB5757',
			        position: 'top'
			      }}
			    />

			    {(
			      chartMode === 'BOTH'
			      ||
			      chartMode === 'REGISTRATION'
			    ) && (

			      <Line
			        type="monotone"
			        dataKey="registrationCount"
			        name="등록 학생 수"
			        stroke="#2F80ED"
			        strokeWidth={2.5}
			        dot={{
			          r: 2,
			          fill: '#FFFFFF',
			          stroke: '#2F80ED'
			        }}
			        activeDot={{
			          r: 5
			        }}
			      />

			    )}

			    {(
			      chartMode === 'BOTH'
			      ||
			      chartMode === 'UNREGISTRATION'
			    ) && (

			      <Line
			        type="monotone"
			        dataKey="unregistrationCount"
			        name="철회 학생 수"
			        stroke="#EB5757"
			        strokeWidth={2.5}
			        dot={{
			          r: 2,
			          fill: '#FFFFFF',
			          stroke: '#EB5757'
			        }}
			        activeDot={{
			          r: 5
			        }}
			      />

			    )}

			  </LineChart>
			</ResponsiveContainer>

            <div className="registration-actions">

              <button
                type="button"
                onClick={() =>
                  setShowTable(
                    !showTable
                  )
                }
              >

                {
                  showTable
                    ? '일별 데이터 표 닫기'
                    : '일별 데이터 표 보기'
                }

              </button>

            </div>


            {showTable && (

              <div className="registration-table-wrapper">

                <table>

                  <thead>

                    <tr>
                      <th>상대일</th>
                      <th>의미</th>
                      <th>등록 학생</th>
                      <th>철회 학생</th>
                    </tr>

                  </thead>


                  <tbody>

                    {filteredChartData.map(
                      (row) => (

                        <tr
                          key={
                            row.relativeDay
                          }
                        >

                          <td>
                            {
                              row.relativeDay
                            }일
                          </td>

                          <td>
                            {
                              formatRelativeDay(
                                row.relativeDay
                              )
                            }
                          </td>

                          <td>
                            {
                              row
                                .registrationCount
                                .toLocaleString()
                            }명
                          </td>

                          <td>
                            {
                              row
                                .unregistrationCount
                                .toLocaleString()
                            }명
                          </td>

                        </tr>

                      )
                    )}

                  </tbody>

                </table>

              </div>

            )}

          </section>


          <details className="analysis-source">

            <summary>
              분석 데이터 정보
            </summary>

            <p>
              Registration Analysis Job:
              {' '}
              #{analysis.jobId ?? '-'}
            </p>

            <p>
              요약 지표는 REGISTRATION_STAT,
              일별 분포는 STUDENT_REGISTRATION의
              실제 상대일 데이터를 사용합니다.
            </p>

          </details>

        </>
      )}

    </main>
  )
}


/**
 * 수강분석 KPI.
 */
function RegistrationKpi({
  title,
  value,
  unit
}) {

  return (
    <article className="registration-kpi">

      <span>
        {title}
      </span>

      <strong>
        {
          typeof value === 'number'
            ? value.toLocaleString()
            : value
        }
      </strong>

      {unit && (
        <small>
          {unit}
        </small>
      )}

    </article>
  )
}


export default RegistrationAnalysisPage