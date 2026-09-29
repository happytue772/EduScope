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
  getCourses,
  getCourseAnalysis
} from '../api/courseAnalysisApi'

import '../styles/courseCompare.css'


/**
 * DB의 0~1 비율을 화면에서 %로 표시.
 */
function toPercent(value) {

  return (
    Number(value ?? 0)
    * 100
  )
}


/**
 * 강의 표시명.
 */
function courseLabel(course) {

  if (!course) {
    return '-'
  }

  return (
    course.codeModule
    + ' / '
    + course.codePresentation
  )
}


/**
 * 두 강의 Presentation 비교 화면.
 *
 * 기존 Course Analysis API를 재사용하므로
 * DB/Backend 구조 변경이 필요 없다.
 */
function CourseComparePage() {

  const [searchParams] =
    useSearchParams()

  const initialCourseId =
    searchParams.get('courseId')

  const [courses, setCourses] =
    useState([])

  const [baseCourseId, setBaseCourseId] =
    useState('')

  const [
    compareCourseId,
    setCompareCourseId
  ] = useState('')

  const [
    sameModuleOnly,
    setSameModuleOnly
  ] = useState(true)

  const [
    baseAnalysis,
    setBaseAnalysis
  ] = useState(null)

  const [
    compareAnalysis,
    setCompareAnalysis
  ] = useState(null)

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
          initialCourseId
          &&
          data.some(
            (course) =>
              String(
                course.coursePresentationId
              )
              === initialCourseId
          )

        const baseId =
          exists
            ? initialCourseId
            : String(
                data[0]
                  .coursePresentationId
              )

        setBaseCourseId(baseId)

      } catch (err) {

        setError(err.message)

      } finally {

        setLoading(false)
      }
    }

    loadCourses()

  }, [])


  const baseCourse =
    useMemo(
      () =>
        courses.find(
          (course) =>
            String(
              course.coursePresentationId
            )
            === baseCourseId
        ),
      [
        courses,
        baseCourseId
      ]
    )


  /**
   * 같은 Module만 비교하는 편의기능.
   */
  const compareCandidates =
    useMemo(() => {

      return courses.filter(
        (course) => {

          if (
            String(
              course.coursePresentationId
            )
            === baseCourseId
          ) {
            return false
          }

          if (
            sameModuleOnly
            &&
            baseCourse
          ) {

            return (
              course.codeModule
              ===
              baseCourse.codeModule
            )
          }

          return true
        }
      )

    }, [
      courses,
      baseCourse,
      baseCourseId,
      sameModuleOnly
    ])

	/**
	 * 현재 선택한 비교 강의가
	 * 비교 후보에 존재하면 그대로 사용한다.
	 *
	 * 존재하지 않으면 첫 번째 실제 후보를 사용한다.
	 *
	 * Effect 안에서 setState를 호출하지 않기 때문에
	 * 불필요한 추가 렌더링도 발생하지 않는다.
	 */
	const effectiveCompareCourseId =
	  useMemo(() => {

	    const currentStillValid =
	      compareCandidates.some(
	        (course) =>
	          String(
	            course.coursePresentationId
	          )
	          === compareCourseId
	      )

	    if (currentStillValid) {
	      return compareCourseId
	    }

	    if (compareCandidates.length > 0) {

	      return String(
	        compareCandidates[0]
	          .coursePresentationId
	      )
	    }

	    return ''

	  }, [
	    compareCandidates,
	    compareCourseId
	  ])
  /**
   * 기준강의 또는 필터가 바뀌면
   * 비교 후보를 자동 조정.
   */
  


  /**
   * 두 강의 분석 API 동시 조회.
   */
  /**
   * 기준 강의와 실제 유효한 비교 강의를
   * 동시에 조회한다.
   */
  useEffect(() => {

    if (
      !baseCourseId
      ||
      !effectiveCompareCourseId
    ) {
      return
    }

    async function loadComparison() {

      try {

        setLoading(true)

        const [
          base,
          target
        ] =
          await Promise.all([
            getCourseAnalysis(
              baseCourseId
            ),
            getCourseAnalysis(
              effectiveCompareCourseId
            )
          ])

        setBaseAnalysis(base)
        setCompareAnalysis(target)

        setError(null)

      } catch (err) {

        setError(err.message)

      } finally {

        setLoading(false)
      }
    }

    loadComparison()

  }, [
    baseCourseId,
    effectiveCompareCourseId
  ])


  const resultChartData =
    useMemo(() => {

      if (
        !baseAnalysis?.result
        ||
        !compareAnalysis?.result
      ) {
        return []
      }

      const baseName =
        courseLabel(
          baseAnalysis.course
        )

      const compareName =
        courseLabel(
          compareAnalysis.course
        )

      return [
        {
          category: 'Pass',
          [baseName]:
            toPercent(
              baseAnalysis
                .result
                .passRate
            ),
          [compareName]:
            toPercent(
              compareAnalysis
                .result
                .passRate
            )
        },
        {
          category: 'Fail',
          [baseName]:
            toPercent(
              baseAnalysis
                .result
                .failRate
            ),
          [compareName]:
            toPercent(
              compareAnalysis
                .result
                .failRate
            )
        },
        {
          category: 'Withdrawn',
          [baseName]:
            toPercent(
              baseAnalysis
                .result
                .withdrawnRate
            ),
          [compareName]:
            toPercent(
              compareAnalysis
                .result
                .withdrawnRate
            )
        },
        {
          category: 'Distinction',
          [baseName]:
            toPercent(
              baseAnalysis
                .result
                .distinctionRate
            ),
          [compareName]:
            toPercent(
              compareAnalysis
                .result
                .distinctionRate
            )
        }
      ]

    }, [
      baseAnalysis,
      compareAnalysis
    ])


  if (error) {

    return (
      <main className="course-compare-page">

        <div className="page-error">
          {error}
        </div>

      </main>
    )
  }


  return (
    <main className="course-compare-page">

      <header className="compare-header">

        <div>

          <p>
            Presentation 간 비교
          </p>

          <h1>
            강의 비교
          </h1>

        </div>

        <Link
          to={
            baseCourseId
              ? '/courses?courseId='
                + baseCourseId
              : '/courses'
          }
        >
          강의 분석으로 돌아가기
        </Link>

      </header>


      <section className="compare-controls">

        <div>

          <label>
            기준 강의
          </label>

          <select
            value={baseCourseId}
            onChange={(event) =>
              setBaseCourseId(
                event.target.value
              )
            }
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

                  {
                    courseLabel(
                      course
                    )
                  }

                </option>

              )
            )}

          </select>

        </div>


        <div>

          <label>
            비교 강의
          </label>

		  <select
		    value={effectiveCompareCourseId}
		    onChange={(event) =>
		      setCompareCourseId(
		        event.target.value
		      )
		    }
            disabled={
              compareCandidates.length
              === 0
            }
          >

            {compareCandidates.map(
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
                    courseLabel(
                      course
                    )
                  }

                </option>

              )
            )}

          </select>

        </div>


        <label className="same-module-option">

          <input
            type="checkbox"
            checked={sameModuleOnly}
            onChange={(event) =>
              setSameModuleOnly(
                event.target.checked
              )
            }
          />

          같은 Module만 비교

        </label>

      </section>


      {sameModuleOnly
        &&
        compareCandidates.length === 0
        && (

          <div className="compare-notice">

            현재 Module에는 비교 가능한
            다른 Presentation이 없습니다.
            '같은 Module만 비교'를 해제하면
            다른 Module과 비교할 수 있습니다.

          </div>

        )}


      {loading && (

        <div className="analysis-loading">
          비교 데이터를 불러오는 중입니다.
        </div>

      )}


      {!loading
        &&
        baseAnalysis
        &&
        compareAnalysis
        && (
          <>

            <section className="compare-summary-grid">

              <ComparisonCard
                title="활동 학생"
                unit="명"
                left={
                  baseAnalysis
                    .activity
                    ?.activeStudentCount
                }
                right={
                  compareAnalysis
                    .activity
                    ?.activeStudentCount
                }
              />

              <ComparisonCard
                title="총 클릭"
                unit="회"
                left={
                  baseAnalysis
                    .activity
                    ?.totalClickCount
                }
                right={
                  compareAnalysis
                    .activity
                    ?.totalClickCount
                }
              />

              <ComparisonCard
                title="학생 평균 클릭"
                unit="회/명"
                left={
                  baseAnalysis
                    .activity
                    ?.avgClickPerStudent
                }
                right={
                  compareAnalysis
                    .activity
                    ?.avgClickPerStudent
                }
              />

              <ComparisonCard
                title="철회율"
                unit="%"
                left={
                  toPercent(
                    baseAnalysis
                      .registration
                      ?.unregistrationRate
                  )
                }
                right={
                  toPercent(
                    compareAnalysis
                      .registration
                      ?.unregistrationRate
                  )
                }
              />

            </section>


            <section className="compare-panel">

              <h2>
                최종 결과 비율 비교
              </h2>

              <p>
                동일한 최종 결과 기준으로
                두 강의 Presentation의
                비율을 비교합니다.
              </p>

              <ResponsiveContainer
                width="100%"
                height={360}
              >

                <BarChart
                  data={resultChartData}
                >

                  <CartesianGrid
                    strokeDasharray="3 3"
                  />

                  <XAxis
                    dataKey="category"
                  />

                  <YAxis
                    domain={[0, 100]}
                    unit="%"
                  />

                  <Tooltip
                    formatter={
                      (value) =>
                        Number(value)
                          .toFixed(1)
                        + '%'
                    }
                  />

                  <Legend />

                  <Bar
                    dataKey={
                      courseLabel(
                        baseAnalysis.course
                      )
                    }
                  />

                  <Bar
                    dataKey={
                      courseLabel(
                        compareAnalysis.course
                      )
                    }
                  />

                </BarChart>

              </ResponsiveContainer>

            </section>

          </>
        )}

    </main>
  )
}


/**
 * 숫자 차이를 명확하게 보여주는 카드.
 */
function ComparisonCard({
  title,
  unit,
  left,
  right
}) {

  const leftValue =
    Number(left ?? 0)

  const rightValue =
    Number(right ?? 0)

  const difference =
    rightValue - leftValue

  return (
    <article className="comparison-card">

      <h3>
        {title}
      </h3>

      <div className="comparison-values">

        <div>
          <span>기준</span>
          <strong>
            {
              leftValue
                .toLocaleString(
                  undefined,
                  {
                    maximumFractionDigits: 1
                  }
                )
            }
            {' '}
            {unit}
          </strong>
        </div>

        <div>
          <span>비교</span>
          <strong>
            {
              rightValue
                .toLocaleString(
                  undefined,
                  {
                    maximumFractionDigits: 1
                  }
                )
            }
            {' '}
            {unit}
          </strong>
        </div>

      </div>

      <p>
        차이:
        {' '}
        {
          difference > 0
            ? '+'
            : ''
        }
        {
          difference
            .toLocaleString(
              undefined,
              {
                maximumFractionDigits: 1
              }
            )
        }
        {' '}
        {unit}
      </p>

    </article>
  )
}


export default CourseComparePage