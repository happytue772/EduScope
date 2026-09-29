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
  getActivityAnalysis
} from '../api/activityAnalysisApi'

import '../styles/activityAnalysis.css'


/**
 * VLE 학습활동 분석 화면.
 */
function ActivityAnalysisPage() {

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
    selectedActivityType,
    setSelectedActivityType
  ] = useState('ALL')

  const [
    searchKeyword,
    setSearchKeyword
  ] = useState('')

  const [
    topLimit,
    setTopLimit
  ] = useState(10)

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
   * 강의 변경 시 활동 분석 조회.
   */
  useEffect(() => {

    if (!selectedCourseId) {
      return
    }

    async function loadActivity() {

      try {

        setLoading(true)

        const data =
          await getActivityAnalysis(
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

    loadActivity()

  }, [selectedCourseId])


  /**
   * 실제 데이터에서 Activity Type 목록 생성.
   *
   * 임의 종류를 생성하지 않는다.
   */
  const activityTypes =
    useMemo(() => {

      const rows =
        analysis?.materials ?? []

      return [
        ...new Set(
          rows
            .map(
              (row) =>
                row.activityType
            )
            .filter(Boolean)
        )
      ]
        .sort()

    }, [analysis])


  /**
   * Activity Type + 검색어 필터.
   */
  const filteredMaterials =
    useMemo(() => {

      const rows =
        analysis?.materials ?? []

      const keyword =
        searchKeyword
          .trim()
          .toLowerCase()

      return rows.filter(
        (row) => {

          const typeMatches =
            selectedActivityType
            === 'ALL'
            ||
            row.activityType
            === selectedActivityType

          const searchMatches =
            keyword.length === 0
            ||
            String(
              row.sourceSiteId
            ).includes(keyword)
            ||
            String(
              row.activityType ?? ''
            )
              .toLowerCase()
              .includes(keyword)

          return (
            typeMatches
            &&
            searchMatches
          )
        }
      )

    }, [
      analysis,
      selectedActivityType,
      searchKeyword
    ])


  /**
   * 현재 필터 결과 KPI.
   */
  const summary =
    useMemo(() => {

      const totalClicks =
        filteredMaterials.reduce(
          (sum, row) =>
            sum
            +
            Number(
              row.totalClickCount
            ),
          0
        )

      const typeCount =
        new Set(
          filteredMaterials.map(
            (row) =>
              row.activityType
          )
        ).size

      const topMaterial =
        filteredMaterials.length > 0
          ? filteredMaterials.reduce(
              (maxRow, currentRow) =>
                Number(
                  currentRow
                    .totalClickCount
                )
                >
                Number(
                  maxRow
                    .totalClickCount
                )
                  ? currentRow
                  : maxRow
            )
          : null

      return {
        materialCount:
          filteredMaterials.length,

        totalClicks,

        typeCount,

        topMaterial
      }

    }, [filteredMaterials])


  /**
   * 클릭 Top N.
   */
  const topMaterials =
    useMemo(() => {

      return [
        ...filteredMaterials
      ]
        .sort(
          (a, b) =>
            Number(
              b.totalClickCount
            )
            -
            Number(
              a.totalClickCount
            )
        )
        .slice(
          0,
          topLimit
        )

    }, [
      filteredMaterials,
      topLimit
    ])


  const topChartData =
    topMaterials.map(
      (row) => ({

        site:
          String(
            row.sourceSiteId
          ),

        clicks:
          Number(
            row.totalClickCount
          ),

        activityType:
          row.activityType
      })
    )


  /**
   * 현재 필터 결과 CSV 다운로드.
   */
  function downloadCsv() {

    if (
      filteredMaterials.length
      === 0
    ) {
      return
    }

    const header = [
      'sourceSiteId',
      'activityType',
      'weekFrom',
      'weekTo',
      'totalClickCount',
      'activeStudentCount',
      'activeDayCount'
    ]

    const rows =
      filteredMaterials.map(
        (row) => [

          row.sourceSiteId,
          row.activityType,
          row.weekFrom ?? '',
          row.weekTo ?? '',
          row.totalClickCount,
          row.activeStudentCount,
          row.activeDayCount

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
      'vle_activity_'
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
      <main className="activity-page">

        <div className="page-error">
          {error}
        </div>

      </main>
    )
  }


  return (
    <main className="activity-page">

      <header className="activity-header">

        <div>

          <p>
            OULAD VLE 학습자료 분석
          </p>

          <h1>
            학습활동 분석
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
      <section className="activity-controls">

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
            Activity Type
          </label>

          <select
            value={
              selectedActivityType
            }
            onChange={(event) =>
              setSelectedActivityType(
                event.target.value
              )
            }
          >

            <option value="ALL">
              전체
            </option>

            {activityTypes.map(
              (type) => (

                <option
                  key={type}
                  value={type}
                >
                  {type}
                </option>

              )
            )}

          </select>

        </div>


        <div>

          <label>
            자료 검색
          </label>

          <input
            type="text"
            value={searchKeyword}
            placeholder="Site ID 또는 유형"
            onChange={(event) =>
              setSearchKeyword(
                event.target.value
              )
            }
          />

        </div>


        <div>

          <label>
            Top 자료
          </label>

          <select
            value={topLimit}
            onChange={(event) =>
              setTopLimit(
                Number(
                  event.target.value
                )
              )
            }
          >

            <option value={5}>
              Top 5
            </option>

            <option value={10}>
              Top 10
            </option>

            <option value={20}>
              Top 20
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
          학습활동 데이터를
          불러오는 중입니다.
        </div>

      )}


      {!loading && analysis && (
        <>

          <section className="activity-guide">

            <p>
              VLE는 OULAD의 온라인 학습환경
              자료를 의미합니다.
            </p>

            <p>
              Site ID는 원본 OULAD의
              학습자료 식별자이며,
              Activity Type은 해당 자료의
              실제 활동 유형입니다.
            </p>

            <p>
              학생 수는 자료별 활동 학생 수이므로,
              여러 자료의 학생 수를 단순 합산하면
              고유 학생 수와 같지 않을 수 있습니다.
            </p>

          </section>


          {/* KPI */}
          <section className="activity-kpi-grid">

            <ActivityKpi
              title="표시 자료"
              value={
                summary.materialCount
              }
              unit="개"
            />

            <ActivityKpi
              title="총 클릭"
              value={
                summary.totalClicks
              }
              unit="회"
            />

            <ActivityKpi
              title="Activity Type"
              value={
                summary.typeCount
              }
              unit="종"
            />

            <ActivityKpi
              title="최다 클릭 Site"
              value={
                summary.topMaterial
                  ?.sourceSiteId
                ?? '-'
              }
              unit=""
            />

          </section>


          {/* Top N */}
          <section className="activity-panel">

            <h2>
              클릭 상위 VLE 자료
            </h2>

            <p>
              현재 필터를 기준으로
              총 클릭 수가 높은 자료입니다.
            </p>

            <ResponsiveContainer
              width="100%"
              height={380}
            >

              <BarChart
                data={topChartData}
                layout="vertical"
                margin={{
                  left: 30,
                  right: 30
                }}
              >

			  <CartesianGrid
			    stroke="#D9E6F2"
			    strokeDasharray="4 4"
			    horizontal={false}
			  />

			  <XAxis
			    type="number"
			    stroke="#607D98"
			    tick={{
			      fill: '#607D98',
			      fontSize: 12
			    }}
			    tickFormatter={
			      (value) =>
			        Number(value).toLocaleString()
			    }
			    label={{
			      value: '총 클릭 수 (회)',
			      position: 'insideBottom',
			      offset: -10,
			      fill: '#607D98'
			    }}
			  />

			  <YAxis
			    type="category"
			    dataKey="site"
			    width={90}
			    stroke="#607D98"
			    tick={{
			      fill: '#607D98',
			      fontSize: 12
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
			        Number(value).toLocaleString()
			        + '회',
			        '총 클릭 수'
			      ]
			    }
			  />

                <Legend />

				<Bar
				  dataKey="clicks"
				  name="총 클릭 수 (회)"
				  fill="#2F80ED"
				  radius={[0, 8, 8, 0]}
				  maxBarSize={30}
				/>

              </BarChart>

            </ResponsiveContainer>

          </section>


          {/* 자료 상세 */}
          <section className="activity-panel">

            <h2>
              VLE 자료 상세
            </h2>

            <div className="activity-table-wrapper">

              <table>

                <thead>

                  <tr>
                    <th>Site ID</th>
                    <th>Activity Type</th>
                    <th>Week From</th>
                    <th>Week To</th>
                    <th>총 클릭</th>
                    <th>활동 학생</th>
                    <th>활동 일수</th>
                  </tr>

                </thead>


                <tbody>

                  {filteredMaterials.map(
                    (row) => (

                      <tr
                        key={
                          row.vleMaterialId
                        }
                      >

                        <td>
                          {row.sourceSiteId}
                        </td>

                        <td>
                          {row.activityType}
                        </td>

                        <td>
                          {
                            row.weekFrom
                            ?? '-'
                          }
                        </td>

                        <td>
                          {
                            row.weekTo
                            ?? '-'
                          }
                        </td>

                        <td>
                          {
                            Number(
                              row
                                .totalClickCount
                            )
                            .toLocaleString()
                          }회
                        </td>

                        <td>
                          {
                            Number(
                              row
                                .activeStudentCount
                            )
                            .toLocaleString()
                          }명
                        </td>

                        <td>
                          {
                            Number(
                              row
                                .activeDayCount
                            )
                            .toLocaleString()
                          }일
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
              VLE Activity Analysis Job:
              {' '}
              #{analysis.jobId ?? '-'}
            </p>

            <p>
              VLE 원본 전체 활동 로그가 아니라
              Hadoop MapReduce로 집계한
              VLE_ACTIVITY_STAT 결과를
              사용합니다.
            </p>

          </details>

        </>
      )}

    </main>
  )
}


function ActivityKpi({
  title,
  value,
  unit
}) {

  return (
    <article className="activity-kpi">

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


export default ActivityAnalysisPage