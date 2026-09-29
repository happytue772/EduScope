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
  getCourses,
  getCourseAnalysis
} from '../api/courseAnalysisApi'

import '../styles/courseAnalysis.css'


/**
 * COURSE_WEEKLY_ACTIVITY_STAT에서
 * 선택 가능한 주차별 분석 지표.
 */
const WEEKLY_METRICS = {

  totalClickCount: {
    label: '총 클릭 수',
    unit: '회',
    axisLabel: '총 클릭 수 (회)'
  },

  activeStudentCount: {
    label: '활동 학생 수',
    unit: '명',
    axisLabel: '활동 학생 수 (명)'
  },

  avgClickPerStudent: {
    label: '학생 평균 클릭',
    unit: '회/명',
    axisLabel: '학생 평균 클릭 (회/명)'
  },

  activeMaterialCount: {
    label: '활동 자료 수',
    unit: '개',
    axisLabel: '활동 자료 수 (개)'
  }
}


/**
 * OULAD Presentation 코드 설명.
 *
 * B = 2월 시작
 * J = 10월 시작
 */
function formatPresentation(
  codePresentation
) {

  if (!codePresentation) {
    return ''
  }

  const year =
    codePresentation.substring(0, 4)

  const period =
    codePresentation.substring(4)

  if (period === 'B') {
    return `${year}년 2월 시작`
  }

  if (period === 'J') {
    return `${year}년 10월 시작`
  }

  // 알 수 없는 코드는 임의 해석하지 않는다.
  return codePresentation
}


/**
 * 상대 주차 설명.
 */
function formatRelativeWeek(week) {

  if (week < 0) {
    return `강의 시작 ${Math.abs(week)}주 전`
  }

  if (week === 0) {
    return '공식 강의 시작 주'
  }

  return `강의 시작 후 ${week}주`
}


/**
 * 지표 값 + 단위 표시.
 */
function formatMetricValue(
  value,
  unit
) {

  const number =
    Number(value)

  if (unit === '회/명') {

    return (
      number.toLocaleString(
        undefined,
        {
          maximumFractionDigits: 1
        }
      )
      + ' 회/명'
    )
  }

  return (
    number.toLocaleString()
    + ' '
    + unit
  )
}


/**
 * EduScope 강의 종합 분석.
 *
 * 활용 DB:
 * COURSE_PRESENTATION
 * COURSE_ACTIVITY_STAT
 * COURSE_WEEKLY_ACTIVITY_STAT
 * COURSE_RESULT_STAT
 * REGISTRATION_STAT
 * ANALYSIS_JOB
 */
function CourseAnalysisPage() {

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
  ] = useState('totalClickCount')

  const [
    weekRange,
    setWeekRange
  ] = useState('ALL')

  const [
    showWeeklyTable,
    setShowWeeklyTable
  ] = useState(false)

  const [analysis, setAnalysis] =
    useState(null)

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState(null)


  /**
   * 최초 진입 시 강의 목록 조회.
   *
   * URL에 courseId가 있으면 유지하고,
   * 없으면 실제 첫 번째 강의를 선택한다.
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

        const courseExists =
          courseIdFromUrl
          &&
          data.some(
            (course) =>
              String(
                course.coursePresentationId
              )
              ===
              courseIdFromUrl
          )

        const initialCourseId =
          courseExists
            ? courseIdFromUrl
            : String(
                data[0]
                  .coursePresentationId
              )

        setSelectedCourseId(
          initialCourseId
        )

        /**
         * URL에도 실제 선택 강의를 반영한다.
         */
        if (
          initialCourseId
          !== courseIdFromUrl
        ) {

          setSearchParams(
            {
              courseId:
                initialCourseId
            },
            {
              replace: true
            }
          )
        }

      } catch (err) {

        setError(err.message)

      } finally {

        setLoading(false)
      }
    }

    loadCourses()

  }, [])


  /**
   * 선택 강의 변경 시
   * 해당 강의 분석을 다시 조회한다.
   */
  useEffect(() => {

    if (!selectedCourseId) {
      return
    }

    async function loadAnalysis() {

      try {

        setLoading(true)

        const data =
          await getCourseAnalysis(
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


  const metricConfig =
    WEEKLY_METRICS[
      selectedMetric
    ]


  /**
   * 선택한 주차 범위만 화면에 표시한다.
   *
   * 원본 데이터는 변경하지 않고
   * React에서 표시 데이터만 필터링한다.
   */
  const filteredWeeklyActivity =
    useMemo(() => {

      const weekly =
        analysis?.weeklyActivity ?? []

      if (weekRange === 'BEFORE') {

        return weekly.filter(
          (row) =>
            row.relativeWeekNo < 0
        )
      }

      if (weekRange === 'WEEK_0_4') {

        return weekly.filter(
          (row) =>
            row.relativeWeekNo >= 0
            &&
            row.relativeWeekNo <= 4
        )
      }

      if (weekRange === 'WEEK_5_8') {

        return weekly.filter(
          (row) =>
            row.relativeWeekNo >= 5
            &&
            row.relativeWeekNo <= 8
        )
      }

      if (weekRange === 'WEEK_9_PLUS') {

        return weekly.filter(
          (row) =>
            row.relativeWeekNo >= 9
        )
      }

      return weekly

    }, [
      analysis,
      weekRange
    ])


  /**
   * 현재 표시 범위에서
   * 선택 지표가 가장 높은 주차.
   */
  const peakWeek =
    useMemo(() => {

      if (
        filteredWeeklyActivity.length
        === 0
      ) {
        return null
      }

      return filteredWeeklyActivity.reduce(
        (maxRow, currentRow) => {

          return (
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
          )
            ? currentRow
            : maxRow
        }
      )

    }, [
      filteredWeeklyActivity,
      selectedMetric
    ])


  /**
   * 현재 표시 범위에서
   * 선택 지표가 가장 낮은 주차.
   */
  const lowestWeek =
    useMemo(() => {

      if (
        filteredWeeklyActivity.length
        === 0
      ) {
        return null
      }

      return filteredWeeklyActivity.reduce(
        (minRow, currentRow) => {

          return (
            Number(
              currentRow[
                selectedMetric
              ]
            )
            <
            Number(
              minRow[
                selectedMetric
              ]
            )
          )
            ? currentRow
            : minRow
        }
      )

    }, [
      filteredWeeklyActivity,
      selectedMetric
    ])


  /**
   * 현재 화면의 주차별 집계 결과를
   * CSV로 다운로드한다.
   */
  function downloadWeeklyCsv() {

    if (
      !analysis
      ||
      filteredWeeklyActivity.length
      === 0
    ) {
      return
    }

    const header = [
      'relativeWeekNo',
      'activeStudentCount',
      'totalClickCount',
      'avgClickPerStudent',
      'activeMaterialCount'
    ]

    const rows =
      filteredWeeklyActivity.map(
        (row) => [
          row.relativeWeekNo,
          row.activeStudentCount,
          row.totalClickCount,
          row.avgClickPerStudent,
          row.activeMaterialCount
        ]
      )

    const csvContent =
      [
        header,
        ...rows
      ]
        .map(
          (row) =>
            row.join(',')
        )
        .join('\n')

    /**
     * BOM을 붙여
     * Excel의 UTF-8 인식 문제를 줄인다.
     */
    const blob =
      new Blob(
        [
          '\uFEFF',
          csvContent
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
      analysis.course.codeModule
      + '_'
      + analysis.course.codePresentation
      + '_weekly_activity_'
      + weekRange
      + '.csv'

    document.body.appendChild(
      link
    )

    link.click()

    document.body.removeChild(
      link
    )

    URL.revokeObjectURL(
      url
    )
  }


  if (error) {

    return (
      <main className="course-analysis-page">

        <div className="page-error">
          {error}
        </div>

      </main>
    )
  }


  return (
    <main className="course-analysis-page">

      {/* 페이지 Header */}
      <div className="course-analysis-header">

        <div>

          <p className="page-subtitle">
            OULAD 강의 단위 분석
          </p>

          <h1>
            강의 분석
          </h1>

        </div>


        <select
          className="course-select"
          value={selectedCourseId}
          onChange={(event) => {

            const newCourseId =
              event.target.value

            setSelectedCourseId(
              newCourseId
            )

            /**
             * 선택 강의를 URL에도 유지.
             */
            setSearchParams({
              courseId:
                newCourseId
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
                {course.codePresentation}
                {' · '}
                {
                  formatPresentation(
                    course
                      .codePresentation
                  )
                }

              </option>

            )
          )}

        </select>

      </div>


      {/* OULAD 코드 안내 */}
      <section className="course-guide">

        <strong>
          OULAD 강의 코드 안내
        </strong>

        <p>
          AAA~GGG는 OULAD에서 실제 강의명을
          익명화하여 제공한 Module 코드입니다.
          실제 과목명을 임의로 부여하지 않습니다.
        </p>

        <p>
          Presentation의 B는 2월 시작,
          J는 10월 시작을 의미합니다.
          예: 2014B = 2014년 2월 시작
        </p>

      </section>


      {loading && (

        <div className="analysis-loading">
          강의 분석 데이터를
          불러오는 중입니다.
        </div>

      )}


      {!loading && analysis && (
        <>

          {/* 강의 기본 정보 */}
          <section className="course-title-card">

            <div>

              <span>
                익명 강의 코드
              </span>

              <strong>
                {
                  analysis
                    .course
                    .codeModule
                }
              </strong>

            </div>


            <div>

              <span>
                개설 시기
              </span>

              <strong>
                {
                  analysis
                    .course
                    .codePresentation
                }
              </strong>

              <small>
                {
                  formatPresentation(
                    analysis
                      .course
                      .codePresentation
                  )
                }
              </small>

            </div>


            <div>

              <span>
                강의 기간
              </span>

              <strong>
                {
                  analysis
                    .course
                    .modulePresentationLength
                }일
              </strong>

            </div>

          </section>


          {/* 강의 활동 KPI */}
          {analysis.activity && (

            <section className="analysis-card-grid">

              <MetricCard
                title="활동 학생"
                value={
                  analysis
                    .activity
                    .activeStudentCount
                }
                unit="명"
              />

              <MetricCard
                title="총 클릭"
                value={
                  analysis
                    .activity
                    .totalClickCount
                }
                unit="회"
              />

              <MetricCard
                title="학생 평균 클릭"
                value={
                  Number(
                    analysis
                      .activity
                      .avgClickPerStudent
                  ).toFixed(1)
                }
                unit="회/명"
              />

              <MetricCard
                title="활동 일수"
                value={
                  analysis
                    .activity
                    .activeDayCount
                }
                unit="일"
              />

            </section>

          )}


          {/* 주차별 학습활동 */}
          <section className="analysis-panel">

            <div className="analysis-panel-header">

              <div>

                <h2>
                  주차별 학습활동
                </h2>

                <p>
                  공식 강의 시작일을
                  0주 기준으로 분석합니다.
                </p>

              </div>


              <div className="chart-controls">

                {/* 지표 선택 */}
                <div className="metric-selector">

                  <label
                    htmlFor="weeklyMetric"
                  >
                    그래프 지표
                  </label>

                  <select
                    id="weeklyMetric"
                    value={selectedMetric}
                    onChange={(event) =>
                      setSelectedMetric(
                        event.target.value
                      )
                    }
                  >

                    <option
                      value="totalClickCount"
                    >
                      총 클릭 수
                    </option>

                    <option
                      value="activeStudentCount"
                    >
                      활동 학생 수
                    </option>

                    <option
                      value="avgClickPerStudent"
                    >
                      학생 평균 클릭
                    </option>

                    <option
                      value="activeMaterialCount"
                    >
                      활동 자료 수
                    </option>

                  </select>

                </div>


                {/* 주차 범위 */}
                <div className="metric-selector">

                  <label
                    htmlFor="weekRange"
                  >
                    조회 범위
                  </label>

                  <select
                    id="weekRange"
                    value={weekRange}
                    onChange={(event) =>
                      setWeekRange(
                        event.target.value
                      )
                    }
                  >

                    <option value="ALL">
                      전체 주차
                    </option>

                    <option value="BEFORE">
                      강의 시작 전
                    </option>

                    <option value="WEEK_0_4">
                      0 ~ 4주
                    </option>

                    <option value="WEEK_5_8">
                      5 ~ 8주
                    </option>

                    <option value="WEEK_9_PLUS">
                      9주 이후
                    </option>

                  </select>

                </div>

              </div>

            </div>


            {/* 선택 지표 요약 */}
            <div className="chart-summary">

              <div>

                <span>
                  현재 세로축
                </span>

                <strong>
                  {
                    metricConfig.axisLabel
                  }
                </strong>

              </div>


              {peakWeek && (

                <div>

                  <span>
                    최고 활동 주차
                  </span>

                  <strong>

                    {
                      peakWeek
                        .relativeWeekNo
                    }주

                    {' · '}

                    {
                      formatMetricValue(
                        peakWeek[
                          selectedMetric
                        ],
                        metricConfig.unit
                      )
                    }

                  </strong>

                </div>

              )}


              {lowestWeek && (

                <div>

                  <span>
                    최저 활동 주차
                  </span>

                  <strong>

                    {
                      lowestWeek
                        .relativeWeekNo
                    }주

                    {' · '}

                    {
                      formatMetricValue(
                        lowestWeek[
                          selectedMetric
                        ],
                        metricConfig.unit
                      )
                    }

                  </strong>

                </div>

              )}

            </div>


            {/* Line Chart */}
            <div className="chart-area">

              <ResponsiveContainer
                width="100%"
                height={360}
              >

                <LineChart
                  data={
                    filteredWeeklyActivity
                  }
                  margin={{
                    top: 30,
                    right: 35,
                    left: 45,
                    bottom: 40
                  }}
                >

                  <CartesianGrid
                    strokeDasharray="3 3"
                  />


                  <XAxis
                    dataKey="relativeWeekNo"
                    type="number"
                    domain={[
                      'dataMin',
                      'dataMax'
                    ]}
                    allowDecimals={false}
                    label={{
                      value:
                        '상대 주차 (주)',
                      position:
                        'insideBottom',
                      offset: -18
                    }}
                  />


                  <YAxis
                    width={95}
                    tickFormatter={
                      (value) =>
                        Number(
                          value
                        ).toLocaleString()
                    }
                    label={{
                      value:
                        metricConfig
                          .axisLabel,
                      angle: -90,
                      position:
                        'insideLeft'
                    }}
                  />


                  <Tooltip
                    labelFormatter={
                      (week) =>
                        `상대 주차 ${week}주 · `
                        +
                        formatRelativeWeek(
                          Number(week)
                        )
                    }
                    formatter={
                      (value) => [
                        formatMetricValue(
                          value,
                          metricConfig.unit
                        ),
                        metricConfig.label
                      ]
                    }
                  />


                  {/* 그래프 범례 */}
                  <Legend
                    verticalAlign="top"
                    align="right"
                  />


                  {/* 공식 강의 시작 시점 */}
                  <ReferenceLine
                    x={0}
                    strokeDasharray="5 5"
                    label="강의 시작"
                  />


                  <Line
                    type="monotone"
                    dataKey={
                      selectedMetric
                    }
                    name={
                      metricConfig.label
                      + ' ('
                      + metricConfig.unit
                      + ')'
                    }
                    strokeWidth={2}
                    dot={{
                      r: 3
                    }}
                    activeDot={{
                      r: 5
                    }}
                  />

                </LineChart>

              </ResponsiveContainer>

            </div>


            {/* 그래프 설명 */}
            <div className="chart-guide">

              <p>
                <strong>
                  가로축:
                </strong>
                {' '}
                공식 강의 시작 시점을
                기준으로 한 상대 주차입니다.
              </p>

              <p>
                <strong>
                  0주:
                </strong>
                {' '}
                공식 강의 시작 주입니다.
              </p>

              <p>
                <strong>
                  음수 주차:
                </strong>
                {' '}
                강의 시작 이전에 발생한
                학습 활동입니다.
              </p>

              <p>
                <strong>
                  현재 세로축:
                </strong>
                {' '}
                {
                  metricConfig.axisLabel
                }
              </p>

              {selectedMetric
                === 'totalClickCount'
                && (
                  <p>
                    총 클릭 수는 해당 주차에
                    VLE 학습 자료에서 발생한
                    클릭 활동의 합계이며
                    단위는 회입니다.
                  </p>
                )}

              {selectedMetric
                === 'activeStudentCount'
                && (
                  <p>
                    활동 학생 수는 해당 주차에
                    VLE 활동 기록이 존재하는
                    학생 수이며 단위는 명입니다.
                  </p>
                )}

              {selectedMetric
                === 'avgClickPerStudent'
                && (
                  <p>
                    학생 평균 클릭은 해당 주차의
                    총 클릭을 활동 학생 기준으로
                    집계한 값이며 단위는 회/명입니다.
                  </p>
                )}

              {selectedMetric
                === 'activeMaterialCount'
                && (
                  <p>
                    활동 자료 수는 해당 주차에
                    실제 학습활동이 발생한
                    VLE 자료 수이며 단위는 개입니다.
                  </p>
                )}

            </div>


            {/* 표 / CSV 편의 기능 */}
            <div className="weekly-actions">

              <button
                type="button"
                className="weekly-table-button"
                onClick={() =>
                  setShowWeeklyTable(
                    !showWeeklyTable
                  )
                }
              >

                {
                  showWeeklyTable
                    ? '주차별 데이터 표 닫기'
                    : '주차별 데이터 표 보기'
                }

              </button>


              <button
                type="button"
                className="weekly-table-button secondary"
                onClick={
                  downloadWeeklyCsv
                }
              >
                현재 범위 CSV 다운로드
              </button>

            </div>


            {/* 실제 집계 데이터 표 */}
            {showWeeklyTable && (

              <div className="weekly-table-wrapper">

                <table className="weekly-table">

                  <thead>

                    <tr>

                      <th>
                        상대 주차
                      </th>

                      <th>
                        의미
                      </th>

                      <th>
                        활동 학생
                      </th>

                      <th>
                        총 클릭
                      </th>

                      <th>
                        학생 평균 클릭
                      </th>

                      <th>
                        활동 자료
                      </th>

                    </tr>

                  </thead>

                  <tbody>

                    {
                      filteredWeeklyActivity
                        .map(
                          (week) => (

                            <tr
                              key={
                                week
                                  .relativeWeekNo
                              }
                            >

                              <td>
                                {
                                  week
                                    .relativeWeekNo
                                }주
                              </td>

                              <td>
                                {
                                  formatRelativeWeek(
                                    week
                                      .relativeWeekNo
                                  )
                                }
                              </td>

                              <td>
                                {
                                  Number(
                                    week
                                      .activeStudentCount
                                  )
                                    .toLocaleString()
                                }명
                              </td>

                              <td>
                                {
                                  Number(
                                    week
                                      .totalClickCount
                                  )
                                    .toLocaleString()
                                }회
                              </td>

                              <td>
                                {
                                  Number(
                                    week
                                      .avgClickPerStudent
                                  )
                                    .toLocaleString(
                                      undefined,
                                      {
                                        maximumFractionDigits: 1
                                      }
                                    )
                                }회/명
                              </td>

                              <td>
                                {
                                  Number(
                                    week
                                      .activeMaterialCount
                                  )
                                    .toLocaleString()
                                }개
                              </td>

                            </tr>

                          )
                        )
                    }

                  </tbody>

                </table>

              </div>

            )}

          </section>


          {/* 최종 결과 요약 */}
          {analysis.result && (

            <section className="analysis-panel">

              <h2>
                최종 결과 요약
              </h2>

              <div className="result-summary-grid">

                <ResultItem
                  label="Pass"
                  count={
                    analysis
                      .result
                      .passCount
                  }
                  rate={
                    analysis
                      .result
                      .passRate
                  }
                />

                <ResultItem
                  label="Fail"
                  count={
                    analysis
                      .result
                      .failCount
                  }
                  rate={
                    analysis
                      .result
                      .failRate
                  }
                />

                <ResultItem
                  label="Withdrawn"
                  count={
                    analysis
                      .result
                      .withdrawnCount
                  }
                  rate={
                    analysis
                      .result
                      .withdrawnRate
                  }
                />

                <ResultItem
                  label="Distinction"
                  count={
                    analysis
                      .result
                      .distinctionCount
                  }
                  rate={
                    analysis
                      .result
                      .distinctionRate
                  }
                />

              </div>

            </section>

          )}


          {/* 등록 / 철회 핵심 요약 */}
          {analysis.registration && (

            <section className="analysis-panel">

              <h2>
                등록 / 철회 핵심 요약
              </h2>

              <div className="registration-grid">

                <MetricCard
                  title="등록 인원"
                  value={
                    analysis
                      .registration
                      .registrationCount
                  }
                  unit="명"
                />

                <MetricCard
                  title="철회 인원"
                  value={
                    analysis
                      .registration
                      .unregistrationCount
                  }
                  unit="명"
                />

                <MetricCard
                  title="철회 결과 인원"
                  value={
                    analysis
                      .registration
                      .withdrawnResultCount
                  }
                  unit="명"
                />

                <MetricCard
                  title="철회율"
                  value={
                    (
                      Number(
                        analysis
                          .registration
                          .unregistrationRate
                      )
                      * 100
                    ).toFixed(1)
                  }
                  unit="%"
                />

              </div>

            </section>

          )}


          {/* 별도 상세 분석 페이지로 이동 */}
          <section className="analysis-panel">

            <h2>
              상세 분석
            </h2>

            <p className="detail-navigation-description">
              현재 선택한 강의를 유지한 채
              전문 분석 화면으로 이동합니다.
            </p>

            <div className="detail-navigation-grid">

              <Link
                to={
                  '/assessments?courseId='
                  + selectedCourseId
                }
                className="detail-navigation-card"
              >

                <strong>
                  평가 분석
                </strong>

                <span>
                  과제·시험의 평균 점수,
                  제출 건수와 실패율 분석
                </span>

              </Link>


              <Link
                to={
                  '/activities?courseId='
                  + selectedCourseId
                }
                className="detail-navigation-card"
              >

                <strong>
                  학습활동 분석
                </strong>

                <span>
                  VLE 자료별 클릭,
                  활동 학생 및 활동일 분석
                </span>

              </Link>


              <Link
                to={
                  '/registrations?courseId='
                  + selectedCourseId
                }
                className="detail-navigation-card"
              >

                <strong>
                  수강 분석
                </strong>

                <span>
                  등록·철회 시점과
                  중도포기 현황 분석
                </span>

              </Link>


              <Link
                to={
                  '/results?courseId='
                  + selectedCourseId
                }
                className="detail-navigation-card"
              >

                <strong>
                  성과 비교
                </strong>

                <span>
                  학습활동과 최종 결과의
                  관계 비교
                </span>

              </Link>


              <Link
                to={
                  '/courses/compare?courseId='
                  + selectedCourseId
                }
                className="detail-navigation-card"
              >

                <strong>
                  강의 비교
                </strong>

                <span>
                  다른 Presentation 및
                  강의 간 지표 비교
                </span>

              </Link>

            </div>

          </section>


          {/* 분석 결과의 출처 */}
          <details className="analysis-source">

            <summary>
              분석 데이터 정보
            </summary>

            <div>

              <p>
                강의 활동 분석 Job:
                {' '}
                #
                {
                  analysis
                    .activity
                    ?.jobId
                  ?? '-'
                }
              </p>

              <p>
                강의 결과 분석 Job:
                {' '}
                #
                {
                  analysis
                    .result
                    ?.jobId
                  ?? '-'
                }
              </p>

              <p>
                등록/철회 분석 Job:
                {' '}
                #
                {
                  analysis
                    .registration
                    ?.jobId
                  ?? '-'
                }
              </p>

              <p>
                표시된 값은 Hadoop MapReduce
                분석 결과를 Oracle에 적재한
                실제 통계 데이터를 기준으로
                합니다.
              </p>

            </div>

          </details>

        </>
      )}

    </main>
  )
}


/**
 * KPI 카드.
 */
function MetricCard({
  title,
  value,
  unit
}) {

  return (
    <article className="metric-card">

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


/**
 * 최종 결과 카드.
 *
 * DB의 0~1 비율을
 * 화면에서만 %로 변환한다.
 */
function ResultItem({
  label,
  count,
  rate
}) {

  const percent =
    (
      Number(rate)
      * 100
    ).toFixed(1)

  return (
    <article className="result-summary-item">

      <div>

        <span>
          {label}
        </span>

        <strong>
          {percent}%
        </strong>

      </div>

      <p>
        {
          Number(count)
            .toLocaleString()
        }명
      </p>

      <div className="result-progress">

        <div
          style={{
            width:
              `${percent}%`
          }}
        />

      </div>

    </article>
  )
}


export default CourseAnalysisPage