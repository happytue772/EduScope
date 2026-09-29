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
  getAssessmentAnalysis
} from '../api/assessmentAnalysisApi'

import '../styles/assessmentAnalysis.css'


function AssessmentAnalysisPage() {

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
    assessmentType,
    setAssessmentType
  ] = useState('ALL')

  const [analysis, setAnalysis] =
    useState(null)

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState(null)


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


  useEffect(() => {

    if (!selectedCourseId) {
      return
    }

    async function loadAnalysis() {

      try {

        setLoading(true)

        const data =
          await getAssessmentAnalysis(
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


  const filteredAssessments =
    useMemo(() => {

      const rows =
        analysis?.assessments ?? []

      if (assessmentType === 'ALL') {
        return rows
      }

      return rows.filter(
        (row) =>
          row.assessmentType
          === assessmentType
      )

    }, [
      analysis,
      assessmentType
    ])


  /**
   * ASSESSMENT_STAT의 실제 건수를
   * 합산한 화면 KPI.
   */
  const summary =
    useMemo(() => {

      return filteredAssessments.reduce(
        (acc, row) => {

          acc.submissions +=
            Number(
              row.submissionCount
            )

          acc.failures +=
            Number(
              row.failCount
            )

          acc.banked +=
            Number(
              row.bankedCount
            )

          return acc

        },
        {
          submissions: 0,
          failures: 0,
          banked: 0
        }
      )

    }, [
      filteredAssessments
    ])


  const scoreChartData =
    filteredAssessments.map(
      (row) => ({

        name:
          String(
            row.sourceAssessmentId
          ),

        avgScore:
          Number(
            row.avgScore
          )
      })
    )


  const failChartData =
    filteredAssessments.map(
      (row) => ({

        name:
          String(
            row.sourceAssessmentId
          ),

        failRate:
          Number(
            row.failRate
          )
          * 100
      })
    )


  function downloadAssessmentCsv() {

    if (
      filteredAssessments.length
      === 0
    ) {
      return
    }

    const header = [
      'sourceAssessmentId',
      'assessmentType',
      'assessmentDueDay',
      'assessmentWeight',
      'submissionCount',
      'avgScore',
      'failCount',
      'failRate',
      'bankedCount',
      'avgSubmissionDay'
    ]

    const rows =
      filteredAssessments.map(
        (row) => [

          row.sourceAssessmentId,
          row.assessmentType,
          row.assessmentDueDay ?? '',
          row.assessmentWeight,
          row.submissionCount,
          row.avgScore,
          row.failCount,
          row.failRate,
          row.bankedCount,
          row.avgSubmissionDay ?? ''

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
      'assessment_analysis_'
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
      <main className="assessment-page">
        {error}
      </main>
    )
  }


  return (
    <main className="assessment-page">

      <header className="assessment-header">

        <div>

          <p>
            OULAD 평가 데이터 분석
          </p>

          <h1>
            평가 분석
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


      <section className="assessment-controls">

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
            평가 유형
          </label>

          <select
            value={assessmentType}
            onChange={(event) =>
              setAssessmentType(
                event.target.value
              )
            }
          >

            <option value="ALL">
              전체
            </option>

            <option value="TMA">
              TMA
            </option>

            <option value="CMA">
              CMA
            </option>

            <option value="Exam">
              Exam
            </option>

          </select>

        </div>


        <button
          type="button"
          onClick={
            downloadAssessmentCsv
          }
        >
          CSV 다운로드
        </button>

      </section>


      {loading && (
        <div className="analysis-loading">
          평가 분석을 불러오는 중입니다.
        </div>
      )}


      {!loading && analysis && (
        <>

          <section className="assessment-guide">

            <p>
              마감일과 평균 제출일은
              실제 달력 날짜가 아니라
              강의 시작일을 0일로 한
              상대 일수입니다.
            </p>

            <p>
              Fail은 EduScope 분석 기준인
              score &lt; 40을 사용하며,
              학생의 최종 결과를 의미하지는 않습니다.
            </p>

          </section>


          <section className="assessment-kpi-grid">

            <AssessmentKpi
              title="평가 수"
              value={
                filteredAssessments.length
              }
              unit="개"
            />

            <AssessmentKpi
              title="총 제출"
              value={
                summary.submissions
              }
              unit="건"
            />

            <AssessmentKpi
              title="Fail 건수"
              value={
                summary.failures
              }
              unit="건"
            />

            <AssessmentKpi
              title="Banked"
              value={
                summary.banked
              }
              unit="건"
            />

          </section>


          <section className="assessment-panel">

            <h2>
              평가별 평균 점수
            </h2>

            <ResponsiveContainer
              width="100%"
              height={320}
            >

              <BarChart
                data={scoreChartData}
              >

			  <CartesianGrid
			    stroke="#D9E6F2"
			    strokeDasharray="4 4"
			    vertical={false}
			  />

			  <XAxis
			    dataKey="name"
			    stroke="#607D98"
			    tick={{
			      fill: '#607D98',
			      fontSize: 12
			    }}
			    label={{
			      value: 'Assessment ID',
			      position: 'insideBottom',
			      offset: -5,
			      fill: '#607D98'
			    }}
			  />

			  <YAxis
			    domain={[0, 100]}
			    stroke="#607D98"
			    tick={{
			      fill: '#607D98',
			      fontSize: 12
			    }}
			    label={{
			      value: '평균 점수 (점)',
			      angle: -90,
			      position: 'insideLeft',
			      fill: '#607D98'
			    }}
			  />

			  <Tooltip
			    contentStyle={{
			      backgroundColor: '#FFFFFF',
			      border: '1px solid #C9DCEC',
			      borderRadius: '10px',
			      boxShadow:
			        '0 6px 18px rgba(31, 78, 120, 0.12)'
			    }}
			    cursor={{
			      fill: 'rgba(47, 128, 237, 0.06)'
			    }}
			    formatter={
			      (value) => [
			        Number(value).toFixed(2) + '점',
			        '평균 점수'
			      ]
			    }
			  />

                <Legend />

				<Bar
				  dataKey="avgScore"
				  name="평균 점수 (점)"
				  fill="#2F80ED"
				  radius={[7, 7, 0, 0]}
				  maxBarSize={42}
				/>

              </BarChart>

            </ResponsiveContainer>

          </section>


          <section className="assessment-panel">

            <h2>
              평가별 Fail 비율
            </h2>

            <ResponsiveContainer
              width="100%"
              height={320}
            >

              <BarChart
                data={failChartData}
              >

                <CartesianGrid
                  strokeDasharray="3 3"
                />

                <XAxis
                  dataKey="name"
                />

                <YAxis
                  domain={[0, 100]}
                  unit="%"
                />

                <Tooltip
                  formatter={
                    (value) => [
                      Number(value)
                        .toFixed(2)
                      + '%',
                      'Fail 비율'
                    ]
                  }
                />

                <Legend />

				<Bar
				  dataKey="failRate"
				  name="Fail 비율 (%)"
				  fill="#EB5757"
				  radius={[7, 7, 0, 0]}
				  maxBarSize={42}
				/>

              </BarChart>

            </ResponsiveContainer>

          </section>


          <section className="assessment-panel">

            <h2>
              평가 상세
            </h2>

            <div className="assessment-table-wrapper">

              <table>

                <thead>

                  <tr>
                    <th>ID</th>
                    <th>유형</th>
                    <th>마감 상대일</th>
                    <th>가중치</th>
                    <th>제출</th>
                    <th>평균점수</th>
                    <th>Fail</th>
                    <th>Fail율</th>
                    <th>Banked</th>
                    <th>평균 제출일</th>
                  </tr>

                </thead>


                <tbody>

                  {filteredAssessments.map(
                    (row) => (

                      <tr
                        key={
                          row.assessmentId
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
                            row.assessmentDueDay
                            ?? '-'
                          }
                        </td>

                        <td>
                          {
                            Number(
                              row
                                .assessmentWeight
                            )
                          }%
                        </td>

                        <td>
                          {
                            Number(
                              row
                                .submissionCount
                            )
                            .toLocaleString()
                          }
                        </td>

                        <td>
                          {
                            Number(
                              row.avgScore
                            )
                            .toFixed(2)
                          }점
                        </td>

                        <td>
                          {
                            Number(
                              row.failCount
                            )
                            .toLocaleString()
                          }
                        </td>

                        <td>
                          {
                            (
                              Number(
                                row.failRate
                              )
                              * 100
                            )
                            .toFixed(2)
                          }%
                        </td>

                        <td>
                          {
                            Number(
                              row.bankedCount
                            )
                            .toLocaleString()
                          }
                        </td>

                        <td>
                          {
                            row.avgSubmissionDay
                            == null
                              ? '-'
                              : Number(
                                  row
                                    .avgSubmissionDay
                                )
                                .toFixed(2)
                          }
                        </td>

                      </tr>

                    )
                  )}

                </tbody>

              </table>

            </div>

          </section>


          <details className="analysis-source">

            <summary>
              분석 데이터 정보
            </summary>

            <p>
              Assessment Analysis Job:
              {' '}
              #{analysis.jobId ?? '-'}
            </p>

          </details>

        </>
      )}

    </main>
  )
}


function AssessmentKpi({
  title,
  value,
  unit
}) {

  return (
    <article className="assessment-kpi">

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


export default AssessmentAnalysisPage