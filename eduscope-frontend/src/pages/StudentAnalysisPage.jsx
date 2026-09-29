import {
  useEffect,
  useMemo,
  useState
} from 'react'

import {
  createPortal
} from 'react-dom'

import {
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
  getStudentAnalysis,
  searchStudents
} from '../api/studentAnalysisApi'

import {
  CHART_COLORS,
  TOOLTIP_STYLE
} from '../constants/chartTheme'

import '../styles/studentAnalysis.css'


/**
 * 강의 시작일 기준 상대일을
 * 사용자가 읽기 쉬운 문자열로 변환한다.
 *
 * 음수 : 강의 시작 전
 * 0    : 강의 시작일
 * 양수 : 강의 시작 후
 */
function formatRelativeDay(day) {

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
      + '일 전'
    )
  }

  if (value === 0) {
    return '강의 시작일'
  }

  return (
    '시작 '
    + value
    + '일 후'
  )
}


/**
 * EduScope 학생 상세 분석 화면.
 *
 * 주요 활용 데이터:
 *
 * OULAD_STUDENT
 * STUDENT_COURSE
 * STUDENT_REGISTRATION
 * STUDENT_ACTIVITY_STAT
 * STUDENT_LEARNING_SUMMARY_STAT
 * STUDENT_ASSESSMENT
 * ASSESSMENT
 * ANALYSIS_JOB
 */
function StudentAnalysisPage() {

  const [
    searchParams,
    setSearchParams
  ] = useSearchParams()


  /**
   * URL에 studentCourseId가 존재하면
   * 새로고침 후에도 동일 학생을 조회한다.
   */
  const initialStudentCourseId =
    searchParams.get(
      'studentCourseId'
    )


  /* =======================================================
     State
     ======================================================= */

  const [
    courses,
    setCourses
  ] = useState([])


  /**
   * 학생 검색 강의 필터.
   *
   * 빈 문자열이면 전체 강의.
   */
  const [
    selectedCourseId,
    setSelectedCourseId
  ] = useState('')


  /**
   * OULAD SOURCE_STUDENT_ID 검색어.
   */
  const [
    keyword,
    setKeyword
  ] = useState('')


  /**
   * 학생 검색 결과.
   */
  const [
    searchResults,
    setSearchResults
  ] = useState([])


  /**
   * 현재 선택된 STUDENT_COURSE_ID.
   *
   * 동일 학생이 여러 강의를 수강할 수 있으므로
   * 학생 + 강의 단위를 상세조회 기준으로 사용한다.
   */
  const [
    selectedStudentCourseId,
    setSelectedStudentCourseId
  ] = useState(
    initialStudentCourseId
    ?? ''
  )


  /**
   * 현재 선택된 학생 상세 분석 결과.
   */
  const [
    analysis,
    setAnalysis
  ] = useState(null)


  const [
    loading,
    setLoading
  ] = useState(false)


  const [
    error,
    setError
  ] = useState(null)


  /**
   * 전체화면 학생 상세 표시 여부.
   *
   * false:
   * 기존 Dashboard 학생 상세 Panel
   *
   * true:
   * document.body Portal 전체화면
   */
  const [
    isDetailExpanded,
    setIsDetailExpanded
  ] = useState(false)


  /* =======================================================
     최초 강의 목록 조회
     ======================================================= */

  useEffect(() => {

    async function loadCourses() {

      try {

        const data =
          await getCourses()

        setCourses(data)

      } catch (err) {

        setError(err.message)
      }
    }

    loadCourses()

  }, [])


  /* =======================================================
     최초 학생 목록 조회
     ======================================================= */

  useEffect(() => {

    async function loadInitialStudents() {

      try {

        /*
         * 임의 데이터를 만들지 않고
         * 실제 DB의 학생 수강정보를 조회한다.
         */
        const data =
          await searchStudents(
            '',
            null
          )

        setSearchResults(data)

      } catch (err) {

        setError(err.message)
      }
    }

    loadInitialStudents()

  }, [])


  /* =======================================================
     학생 선택 시 상세 분석 조회
     ======================================================= */

  useEffect(() => {

    if (!selectedStudentCourseId) {
      return
    }


    async function loadStudent() {

      try {

        setLoading(true)

        const data =
          await getStudentAnalysis(
            selectedStudentCourseId
          )

        setAnalysis(data)

        setError(null)

      } catch (err) {

        setError(err.message)

      } finally {

        setLoading(false)
      }
    }


    loadStudent()

  }, [
    selectedStudentCourseId
  ])


  /* =======================================================
     전체화면 제어
     ======================================================= */

  useEffect(() => {

    if (!isDetailExpanded) {
      return
    }


    /**
     * 전체화면을 연 상태에서는
     * 뒤쪽 EduScope 페이지가 스크롤되지 않게 한다.
     */
    const previousOverflow =
      document.body.style.overflow


    function handleEscape(event) {

      if (event.key === 'Escape') {

        setIsDetailExpanded(false)
      }
    }


    document.body.style.overflow =
      'hidden'


    window.addEventListener(
      'keydown',
      handleEscape
    )


    return () => {

      document.body.style.overflow =
        previousOverflow

      window.removeEventListener(
        'keydown',
        handleEscape
      )
    }

  }, [
    isDetailExpanded
  ])


  /* =======================================================
     학생 검색
     ======================================================= */

  async function handleSearch() {

    try {

      setLoading(true)

      const data =
        await searchStudents(
          keyword,
          selectedCourseId || null
        )

      setSearchResults(data)

      setError(null)

    } catch (err) {

      setError(err.message)

    } finally {

      setLoading(false)
    }
  }


  /* =======================================================
     학생 선택
     ======================================================= */

  function selectStudent(
    studentCourseId
  ) {

    const id =
      String(studentCourseId)


    setSelectedStudentCourseId(id)


    /*
     * 학생이 변경되면
     * 기존 전체화면은 닫는다.
     */
    setIsDetailExpanded(false)


    setSearchParams({
      studentCourseId: id
    })
  }


  /* =======================================================
     학생 상세 클릭 → 전체화면
     ======================================================= */

  function handleDetailPanelClick(
    event
  ) {

    if (
      !analysis
      ||
      loading
      ||
      isDetailExpanded
    ) {
      return
    }


    /**
     * 버튼 / 링크 / details 등
     * 실제 조작 요소를 클릭한 경우에는
     * 전체화면 전환을 실행하지 않는다.
     */
    const interactiveElement =
      event.target.closest(
        'button, a, input, select, textarea, summary'
      )


    if (interactiveElement) {
      return
    }


    setIsDetailExpanded(true)
  }


  /**
   * 키보드로 학생 상세 Panel을 선택한 뒤
   * Enter / Space를 눌러도 전체화면을 연다.
   */
  function handleDetailPanelKeyDown(
    event
  ) {

    if (
      !analysis
      ||
      loading
      ||
      isDetailExpanded
    ) {
      return
    }


    if (
      event.key === 'Enter'
      ||
      event.key === ' '
    ) {

      event.preventDefault()

      setIsDetailExpanded(true)
    }
  }


  /* =======================================================
     평가 Score 차트 데이터
     ======================================================= */

  const scoreChartData =
    useMemo(() => {

      return (
        analysis?.assessments
        ?? []
      )
        .filter(
          (row) =>
            row.score !== null
            &&
            row.score !== undefined
        )
        .map(
          (row) => ({

            assessment:
              String(
                row.sourceAssessmentId
              ),

            score:
              Number(
                row.score
              )
          })
        )

    }, [
      analysis
    ])


  /* =======================================================
     평가 CSV 다운로드
     ======================================================= */

  function downloadAssessmentCsv() {

    const rows =
      analysis?.assessments
      ?? []


    if (rows.length === 0) {
      return
    }


    const header = [

      'sourceAssessmentId',
      'assessmentType',
      'assessmentDueDay',
      'submittedDay',
      'assessmentWeight',
      'score',
      'isBanked'

    ]


    const dataRows =
      rows.map(
        (row) => [

          row.sourceAssessmentId,
          row.assessmentType,
          row.assessmentDueDay ?? '',
          row.submittedDay ?? '',
          row.assessmentWeight,
          row.score ?? '',
          row.isBanked

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


    /**
     * UTF-8 BOM 추가.
     * Excel에서 한글 표시 문제를 줄인다.
     */
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
      document.createElement(
        'a'
      )


    link.href = url

    link.download =
      'student_assessment_'
      + selectedStudentCourseId
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


  /* =======================================================
     학생 상세 공통 Render

     일반 Dashboard 상세와
     전체화면 Portal이 동일한 JSX를 사용한다.

     따라서 추후 기능을 추가해도
     두 화면을 따로 수정할 필요가 없다.
     ======================================================= */

  function renderStudentDetailContent() {

    if (!analysis && !loading) {

      return (
        <div className="student-empty">

          학생을 선택하면
          상세 분석이 표시됩니다.

        </div>
      )
    }


    if (loading) {

      return (
        <div className="analysis-loading">

          학생 정보를 불러오는 중입니다.

        </div>
      )
    }


    if (!analysis) {
      return null
    }


    return (
      <>


        {/* ===============================================
            학생 핵심정보
            =============================================== */}

        <section className="student-profile">


          <div>

            <span>
              Student ID
            </span>

            <strong>
              {
                analysis
                  .student
                  .sourceStudentId
              }
            </strong>

          </div>


          <div>

            <span>
              강의
            </span>

            <strong>

              {
                analysis
                  .student
                  .codeModule
              }

              {' / '}

              {
                analysis
                  .student
                  .codePresentation
              }

            </strong>

          </div>


          <div>

            <span>
              최종 결과
            </span>

            <strong>
              {
                analysis
                  .student
                  .finalResult
              }
            </strong>

          </div>

        </section>


        {/* ===============================================
            학생 기본정보
            =============================================== */}

        <section className="student-panel">


          <h2>
            학생 기본정보
          </h2>


          <div className="student-info-grid">


            <InfoItem
              label="Gender"
              value={
                analysis
                  .student
                  .gender
              }
            />


            <InfoItem
              label="Region"
              value={
                analysis
                  .student
                  .region
              }
            />


            <InfoItem
              label="Education"
              value={
                analysis
                  .student
                  .highestEducation
              }
            />


            <InfoItem
              label="IMD Band"
              value={
                analysis
                  .student
                  .imdBand
              }
              description={
                'Index of Multiple Deprivation의 약자로, '
                + '학생이 거주한 지역의 상대적인 사회·경제적 '
                + '박탈 수준을 구간으로 나타낸 값입니다. '
                + '낮은 구간일수록 상대적으로 박탈도가 높은 '
                + '지역을 의미합니다.'
              }
            />


            <InfoItem
              label="Age Band"
              value={
                analysis
                  .student
                  .ageBand
              }
              description={
                '학생의 실제 나이를 직접 표시하지 않고 '
                + '연령 범위를 구간으로 제공한 값입니다. '
                + 'OULAD에서는 0-35, 35-55, 55 이상 '
                + '형태의 연령 구간을 사용합니다.'
              }
            />


            <InfoItem
              label="이전 시도"
              value={
                analysis
                  .student
                  .numOfPrevAttempts
              }
            />


            <InfoItem
              label="수강 Credits"
              value={
                analysis
                  .student
                  .studiedCredits
              }
            />


            <InfoItem
              label="Disability"
              value={
                analysis
                  .student
                  .disability
              }
            />

          </div>

        </section>


        {/* ===============================================
            등록 / 철회
            =============================================== */}

        <section className="student-panel">


          <h2>
            등록 / 철회
          </h2>


          <div className="student-info-grid">


            <InfoItem
              label="등록 시점"
              value={
                formatRelativeDay(
                  analysis
                    .registration
                    ?.registrationDay
                )
              }
            />


            <InfoItem
              label="철회 시점"
              value={
                formatRelativeDay(
                  analysis
                    .registration
                    ?.unregistrationDay
                )
              }
            />

          </div>

        </section>


        {/* ===============================================
            학생 학습활동 KPI
            =============================================== */}

        {analysis.activity && (

          <section className="student-kpi-grid">


            <StudentKpi
              title="총 클릭"
              value={
                analysis
                  .activity
                  .totalClickCount
              }
              unit="회"
            />


            <StudentKpi
              title="활동 일수"
              value={
                analysis
                  .activity
                  .activeDayCount
              }
              unit="일"
            />


            <StudentKpi
              title="사용 자료"
              value={
                analysis
                  .activity
                  .usedMaterialCount
              }
              unit="개"
            />


            <StudentKpi
              title="일평균 클릭"
              value={
                analysis
                  .activity
                  .avgDailyClickCount
              }
              unit="회/일"
            />

          </section>

        )}


        {/* ===============================================
            학습 종합
            =============================================== */}

        {analysis.learningSummary && (

          <section className="student-panel">


            <h2>
              학습 종합
            </h2>


            <div className="student-kpi-grid inside">


              <StudentKpi
                title="제출 평가"
                value={
                  analysis
                    .learningSummary
                    .submittedAssessmentCount
                }
                unit="건"
              />


              <StudentKpi
                title="평균 평가점수"
                value={
                  analysis
                    .learningSummary
                    .avgAssessmentScore
                }
                unit="점"
              />


              <StudentKpi
                title="실패 평가"
                value={
                  analysis
                    .learningSummary
                    .failedAssessmentCount
                }
                unit="건"
              />


              <StudentKpi
                title="최초 활동"
                value={
                  formatRelativeDay(
                    analysis
                      .learningSummary
                      .firstActivityDay
                  )
                }
              />


              <StudentKpi
                title="마지막 활동"
                value={
                  formatRelativeDay(
                    analysis
                      .learningSummary
                      .lastActivityDay
                  )
                }
              />

            </div>

          </section>

        )}


        {/* ===============================================
            평가 점수 그래프
            =============================================== */}

        {scoreChartData.length > 0 && (

          <section className="student-panel">


            <h2>
              평가 점수 이력
            </h2>


            <ResponsiveContainer
              width="100%"
              height={320}
            >

              <BarChart
                data={
                  scoreChartData
                }
              >


                <CartesianGrid
                  stroke={CHART_COLORS.grid}
                  strokeDasharray="4 4"
                  vertical={false}
                />


                <XAxis
                  dataKey="assessment"
                  stroke={CHART_COLORS.axis}
                />


                <YAxis
                  domain={[
                    0,
                    100
                  ]}
                  stroke={CHART_COLORS.axis}
                  label={{
                    value:
                      '점수 (점)',

                    angle:
                      -90,

                    position:
                      'insideLeft',

                    fill:
                      CHART_COLORS.axis
                  }}
                />


                <Tooltip
                  contentStyle={
                    TOOLTIP_STYLE
                  }
                  formatter={
                    (value) => [

                      Number(value)
                        .toFixed(2)
                      + '점',

                      'Score'

                    ]
                  }
                />


                <Legend />


                <Bar
                  dataKey="score"
                  name="평가 점수"
                  fill={CHART_COLORS.primary}
                  radius={[
                    7,
                    7,
                    0,
                    0
                  ]}
                  maxBarSize={45}
                />

              </BarChart>

            </ResponsiveContainer>

          </section>

        )}


        {/* ===============================================
            평가 제출 이력
            =============================================== */}

        <section className="student-panel">


          <div className="student-panel-header">


            <h2>
              평가 제출 이력
            </h2>


            <button
              type="button"
              onClick={(event) => {

                event.stopPropagation()

                downloadAssessmentCsv()
              }}
            >
              CSV 다운로드
            </button>

          </div>


          <div className="student-assessment-table">


            <table>


              <thead>

                <tr>

                  <th>
                    Assessment ID
                  </th>

                  <th>
                    유형
                  </th>

                  <th>
                    마감일
                  </th>

                  <th>
                    제출일
                  </th>

                  <th>
                    가중치
                  </th>

                  <th>
                    Score
                  </th>

                  <th>
                    Banked
                  </th>

                </tr>

              </thead>


              <tbody>

                {
                  analysis
                    .assessments
                    .map(
                      (row) => (

                        <tr
                          key={
                            row
                              .assessmentId
                          }
                        >


                          <td>
                            {
                              row
                                .sourceAssessmentId
                            }
                          </td>


                          <td>
                            {
                              row
                                .assessmentType
                            }
                          </td>


                          <td>
                            {
                              formatRelativeDay(
                                row
                                  .assessmentDueDay
                              )
                            }
                          </td>


                          <td>
                            {
                              formatRelativeDay(
                                row
                                  .submittedDay
                              )
                            }
                          </td>


                          <td>

                            {
                              Number(
                                row
                                  .assessmentWeight
                              )
                            }

                            %

                          </td>


                          <td>

                            {
                              row.score
                              == null
                                ? '-'
                                : Number(
                                    row.score
                                  )
                                  .toFixed(2)
                            }

                          </td>


                          <td>
                            {
                              row
                                .isBanked
                            }
                          </td>

                        </tr>

                      )
                    )
                }

              </tbody>

            </table>

          </div>

        </section>


        {/* ===============================================
            분석 Job 정보
            =============================================== */}

        <details
          className="analysis-source"

          onClick={(event) =>
            event.stopPropagation()
          }
        >


          <summary>
            분석 데이터 정보
          </summary>


          <p>

            Student Activity Job:
            {' #'}

            {
              analysis
                .studentActivityJobId
              ?? '-'
            }

          </p>


          <p>

            Student Learning Summary Job:
            {' #'}

            {
              analysis
                .studentLearningSummaryJobId
              ??
              analysis
                .learningSummaryJobId
              ??
              '-'
            }

          </p>


          <p>
            학생 활동 데이터는
            STUDENT_ACTIVITY_STAT,
            종합 학습 데이터는
            STUDENT_LEARNING_SUMMARY_STAT,
            평가 제출 이력은
            STUDENT_ASSESSMENT를 사용합니다.
          </p>

        </details>

      </>
    )
  }


  /* =======================================================
     Render
     ======================================================= */

  return (

    <main className="student-analysis-page">


      {/* ===================================================
          상단 Layout

          왼쪽:
          페이지 설명 + 검색

          오른쪽:
          Quick Guide
          =================================================== */}

      <section className="student-top-layout">


        {/* =================================================
            상단 왼쪽
            ================================================= */}

        <div className="student-top-main">


          <header className="student-header">


            <p>
              OULAD 익명 학생 학습 분석
            </p>


            <h1>
              학생 분석
            </h1>


            <span className="student-header-description">

              익명 학생의 수강·학습활동·평가 결과를
              강의 단위로 확인합니다.

            </span>

          </header>


          {/* 분석 기준 */}

          <div className="student-guide compact">


            <p>
              학생 ID는 OULAD에서 제공된 익명화된
              SOURCE_STUDENT_ID이며 실제 개인
              식별정보가 아닙니다.
            </p>


            <p>
              동일 학생이 여러 강의를 수강할 수 있으므로
              상세 분석은 학생 + 강의 단위인
              STUDENT_COURSE_ID를 기준으로 수행합니다.
            </p>

          </div>


          {/* 검색 */}

          <div className="student-search-controls">


            <div>


              <label>
                강의
              </label>


              <select
                value={
                  selectedCourseId
                }

                onChange={(event) =>
                  setSelectedCourseId(
                    event.target.value
                  )
                }
              >


                <option value="">
                  전체 강의
                </option>


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

                      {
                        course
                          .codeModule
                      }

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
                익명 학생 ID
              </label>


              <input
                type="text"

                value={
                  keyword
                }

                placeholder="Student ID 검색"

                onChange={(event) =>
                  setKeyword(
                    event.target.value
                  )
                }

                onKeyDown={(event) => {

                  if (
                    event.key
                    ===
                    'Enter'
                  ) {

                    handleSearch()
                  }
                }}
              />

            </div>


            <button
              type="button"

              onClick={
                handleSearch
              }
            >
              검색
            </button>

          </div>

        </div>


        {/* =================================================
            상단 오른쪽 Quick Guide
            ================================================= */}

        <aside className="student-quick-guide">


          <div className="quick-guide-header">


            <span>
              QUICK GUIDE
            </span>


            <h2>
              학생 분석 빠른 안내
            </h2>

          </div>


          {/* 화면 활용 순서 */}

          <div className="quick-guide-section">


            <h3>
              화면 활용 순서
            </h3>


            <div className="quick-guide-flow">


              <div className="quick-guide-step">


                <span className="quick-guide-step-number">
                  1
                </span>


                <div>

                  <strong>
                    학생 검색
                  </strong>

                  <p>
                    강의를 선택하거나
                    익명 Student ID를 검색합니다.
                  </p>

                </div>

              </div>


              <div className="quick-guide-step">


                <span className="quick-guide-step-number">
                  2
                </span>


                <div>

                  <strong>
                    학생 선택
                  </strong>

                  <p>
                    검색 결과에서 학생의 특정
                    강의 수강 기록을 선택합니다.
                  </p>

                </div>

              </div>


              <div className="quick-guide-step">


                <span className="quick-guide-step-number">
                  3
                </span>


                <div>

                  <strong>
                    학습 결과 확인
                  </strong>

                  <p>
                    활동량·평가점수·등록/철회·
                    최종 결과를 종합적으로 확인합니다.
                  </p>

                </div>

              </div>

            </div>

          </div>


          {/* 주요 용어 */}

          <div className="quick-guide-section terminology">


            <h3>
              주요 용어
            </h3>


            <div className="quick-term-grid">


              <div className="quick-term-item">

                <strong>
                  Age Band
                </strong>

                <span>
                  학생의 실제 나이가 아닌
                  연령 구간
                </span>

              </div>


              <div className="quick-term-item">

                <strong>
                  IMD Band
                </strong>

                <span>
                  거주 지역의 상대적
                  사회·경제적 박탈 수준
                </span>

              </div>


              <div className="quick-term-item">

                <strong>
                  Credits
                </strong>

                <span>
                  해당 학생의 수강 학점
                </span>

              </div>


              <div className="quick-term-item">

                <strong>
                  Previous Attempts
                </strong>

                <span>
                  해당 강의 모듈의
                  이전 수강 시도 횟수
                </span>

              </div>

            </div>

          </div>


          {/* 해석 Tip */}

          <div className="quick-guide-tip">


            <strong>
              해석 Tip
            </strong>


            <p>
              클릭 수나 활동 일수가 높다고 해서
              반드시 성적이 높은 것을 의미하지는 않습니다.
              학습활동과 평가 결과를 함께 확인하세요.
            </p>

          </div>

        </aside>

      </section>


      {/* ===================================================
          Error
          =================================================== */}

      {error && (

        <div className="page-error">

          {error}

        </div>

      )}


      {/* ===================================================
          검색결과 + 일반 학생 상세
          =================================================== */}

      <section className="student-result-layout">


        {/* =================================================
            검색 결과
            ================================================= */}

        <div className="student-search-result">


          <h2>
            검색 결과
          </h2>


          <p>
            최대 100개의 수강 정보를 표시합니다.
          </p>


          <div className="student-result-table-wrapper">


            <table>


              <thead>

                <tr>

                  <th>
                    Student ID
                  </th>

                  <th>
                    강의
                  </th>

                  <th>
                    결과
                  </th>

                </tr>

              </thead>


              <tbody>

                {searchResults.map(
                  (row) => (

                    <tr
                      key={
                        row
                          .studentCourseId
                      }

                      className={
                        String(
                          row
                            .studentCourseId
                        )
                        ===
                        selectedStudentCourseId
                          ? 'selected'
                          : ''
                      }

                      onClick={() =>
                        selectStudent(
                          row
                            .studentCourseId
                        )
                      }
                    >


                      <td>
                        {
                          row
                            .sourceStudentId
                        }
                      </td>


                      <td>

                        {
                          row
                            .codeModule
                        }

                        {' / '}

                        {
                          row
                            .codePresentation
                        }

                      </td>


                      <td>
                        {
                          row
                            .finalResult
                        }
                      </td>

                    </tr>

                  )
                )}

              </tbody>

            </table>

          </div>

        </div>


        {/* =================================================
            일반 학생 상세 Panel

            이 영역 전체를 클릭하면
            Portal 전체화면을 연다.
            ================================================= */}

        <div
          className={
            analysis
              ? 'student-detail student-detail-clickable'
              : 'student-detail'
          }

          onClick={
            handleDetailPanelClick
          }

          onKeyDown={
            handleDetailPanelKeyDown
          }

          tabIndex={
            analysis
            &&
            !loading
              ? 0
              : undefined
          }

          aria-label={
            analysis
              ? '학생 상세 전체화면 열기'
              : undefined
          }
        >

          {renderStudentDetailContent()}

        </div>

      </section>


      {/* ===================================================
          전체화면 Portal

          document.body에 직접 렌더링되므로
          Sidebar / Header / AppLayout 범위에 갇히지 않는다.
          =================================================== */}

      {
        isDetailExpanded
        &&
        analysis
        &&
        createPortal(

          <div
            className="student-fullscreen-overlay"
          >


            <section
              className="student-fullscreen-modal"

              role="dialog"

              aria-modal="true"

              aria-label="학생 상세 분석 전체화면"
            >


              {/* =============================================
                  전체화면 Header
                  ============================================= */}

              <header className="student-fullscreen-header">


                <div>


                  <span>
                    STUDENT DETAIL
                  </span>


                  <h2>

                    Student #

                    {
                      analysis
                        .student
                        .sourceStudentId
                    }

                  </h2>


                  <p>

                    {
                      analysis
                        .student
                        .codeModule
                    }

                    {' / '}

                    {
                      analysis
                        .student
                        .codePresentation
                    }

                    {' · '}

                    {
                      analysis
                        .student
                        .finalResult
                    }

                  </p>

                </div>


                <button
                  type="button"

                  autoFocus

                  onClick={() =>
                    setIsDetailExpanded(
                      false
                    )
                  }
                >
                  닫기 ×
                </button>

              </header>


              {/* =============================================
                  전체화면 Scroll 영역
                  ============================================= */}

              <div className="student-fullscreen-content">

                {renderStudentDetailContent()}

              </div>

            </section>

          </div>,

          document.body
        )
      }

    </main>
  )
}


/**
 * 학생 기본정보 Card.
 *
 * description이 있는 경우
 * Hover / Focus 시 Tooltip 표시.
 *
 * 별도의 ? 아이콘은 표시하지 않는다.
 */
function InfoItem({
  label,
  value,
  description
}) {

  return (

    <div
      className={
        description
          ? 'student-info-item has-tooltip'
          : 'student-info-item'
      }

      tabIndex={
        description
          ? 0
          : undefined
      }
    >


      <span>
        {label}
      </span>


      <strong>
        {
          value
          ?? '-'
        }
      </strong>


      {description && (

        <div
          className="student-info-tooltip"

          role="tooltip"
        >


          <strong>
            {label}
          </strong>


          <p>
            {description}
          </p>

        </div>

      )}

    </div>
  )
}


/**
 * 학생 KPI Card.
 */
function StudentKpi({
  title,
  value,
  unit
}) {

  let displayValue =
    value ?? '-'


  /**
   * 숫자는 천 단위 구분 및
   * 최대 소수점 두 자리까지 표시.
   */
  if (
    typeof value
    ===
    'number'
  ) {

    displayValue =
      value.toLocaleString(
        undefined,
        {
          maximumFractionDigits:
            2
        }
      )
  }


  return (

    <article className="student-kpi">


      <span>
        {title}
      </span>


      <strong>
        {displayValue}
      </strong>


      {unit && (

        <small>
          {unit}
        </small>

      )}

    </article>
  )
}


export default StudentAnalysisPage