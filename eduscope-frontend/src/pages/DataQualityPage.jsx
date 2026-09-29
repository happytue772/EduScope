import {
  useEffect,
  useMemo,
  useState
} from 'react'

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
  getDataQualitySummary
} from '../api/dataQualityApi'

import {
  getDatasets
} from '../api/datasetApi'

import '../styles/dataQuality.css'


/**
 * 품질 결과가 없는 파일은 "정상"이 아니라
 * "탐지 없음"으로 표시한다.
 */
function getQualityLabel(item) {

  return item.qualityType
    ? item.qualityType
    : '탐지 없음'
}


/**
 * 기존 Backend DTO에는 detected 필드가 없으므로
 * 실제 qualityType + recordCount 기준으로 판단한다.
 */
function hasDetection(item) {

  return (
    item.qualityType != null
    &&
    item.qualityType !== ''
    &&
    Number(
      item.recordCount
      ?? 0
    ) > 0
  )
}


/**
 * EduScope Data Quality 화면.
 */
function DataQualityPage() {

  const [
    datasets,
    setDatasets
  ] = useState([])


  const [
    selectedDatasetId,
    setSelectedDatasetId
  ] = useState('')


  const [
    data,
    setData
  ] = useState(null)


  const [
    loading,
    setLoading
  ] = useState(true)


  const [
    error,
    setError
  ] = useState(null)


  /**
   * DB에 실제 등록된 Dataset 목록 조회.
   *
   * 기존 datasetId = 1 하드코딩 제거.
   */
  useEffect(() => {

    let cancelled = false


    async function loadDatasets() {

      try {

        const result =
          await getDatasets()


        if (cancelled) {
          return
        }


        const normalized =
          Array.isArray(result)
            ? result
            : []


        setDatasets(
          normalized
        )


        if (
          normalized.length > 0
        ) {

          setSelectedDatasetId(
            String(
              normalized[0].datasetId
            )
          )

        } else {

          setLoading(false)
        }

      } catch (err) {

        if (!cancelled) {

          setError(
            err.message
          )

          setLoading(false)
        }
      }
    }


    loadDatasets()


    return () => {
      cancelled = true
    }

  }, [])


  /**
   * 선택 Dataset의 최신 Data Quality Summary 조회.
   */
  useEffect(() => {

    if (!selectedDatasetId) {
      return
    }


    let cancelled = false


    async function loadData() {

      try {

        setLoading(true)


        const result =
          await getDataQualitySummary(
            selectedDatasetId
          )


        if (cancelled) {
          return
        }


        setData(
          result
        )

        setError(
          null
        )

      } catch (err) {

        if (!cancelled) {

          setData(
            null
          )

          setError(
            err.message
          )
        }

      } finally {

        if (!cancelled) {

          setLoading(
            false
          )
        }
      }
    }


    loadData()


    return () => {
      cancelled = true
    }

  }, [
    selectedDatasetId
  ])


  /**
   * 파일별 품질 탐지 건수.
   *
   * 동일 파일에 품질유형이 여러 개 존재할 수 있으므로
   * Dataset File 단위로 합산한다.
   *
   * 단, 서로 다른 품질 규칙이 동일 원본 행을
   * 중복 탐지할 수 있으므로 원본 Record Count와
   * 직접 비교하지 않는다.
   */
  const fileChartData =
    useMemo(() => {

      const items =
        data?.items
        ?? []


      const fileMap =
        new Map()


      items.forEach(
        item => {

          const key =
            item.datasetFileId


          const current =
            fileMap.get(key)
            ?? {
              fileName:
                item.originalFileName,

              recordCount:
                0
            }


          current.recordCount +=
            Number(
              item.recordCount
              ?? 0
            )


          fileMap.set(
            key,
            current
          )
        }
      )


      return Array.from(
        fileMap.values()
      )

    }, [
      data
    ])


  if (loading) {

    return (
      <main className="dq-page">

        <div className="dq-loading">
          데이터 품질 정보를 불러오는 중입니다.
        </div>

      </main>
    )
  }


  if (error) {

    return (
      <main className="dq-page">

        <div className="page-error">
          {error}
        </div>

      </main>
    )
  }


  if (
    datasets.length === 0
  ) {

    return (
      <main className="dq-page">

        <div className="dq-empty">
          조회 가능한 Dataset이 없습니다.
        </div>

      </main>
    )
  }


  if (!data) {
    return null
  }


  return (
    <main className="dq-page">


      {/* Header */}
      <header className="dq-header">

        <div>

          <p>
            OULAD 데이터 검증 및 품질 분석
          </p>

          <h1>
            Data Quality
          </h1>

          <span>
            Hadoop 분석 결과와 Oracle 적재 결과를
            기반으로 원본 데이터의 품질 이슈를 확인합니다.
          </span>

        </div>

      </header>


      {/* Dataset 선택 */}
      <section className="dq-filter">

        <label htmlFor="dqDataset">
          Dataset
        </label>


        <select
          id="dqDataset"
          value={
            selectedDatasetId
          }
          onChange={
            event => {

              setData(
                null
              )

              setSelectedDatasetId(
                event.target.value
              )
            }
          }
        >

          {
            datasets.map(
              dataset => (

                <option
                  key={
                    dataset.datasetId
                  }
                  value={
                    dataset.datasetId
                  }
                >

                  #{dataset.datasetId}
                  {' '}
                  {dataset.displayName}
                  {' '}
                  / v{dataset.datasetVersion}

                </option>

              )
            )
          }

        </select>


        <span>
          실제 등록된 Dataset 기준으로
          최신 SUCCESS + Oracle 적재완료
          Data Quality Job을 조회합니다.
        </span>

      </section>


      {/* Dataset */}
      <section className="dq-dataset-card">

        <div>

          <span>
            Dataset
          </span>

          <strong>
            {
              data.dataset.displayName
            }
          </strong>

        </div>


        <div>

          <span>
            Dataset Version
          </span>

          <strong>
            {
              data.dataset.datasetVersion
            }
          </strong>

        </div>


        <div>

          <span>
            Schema Version
          </span>

          <strong>
            {
              data.dataset.schemaVersion
            }
          </strong>

        </div>


        <div>

          <span>
            원본 파일
          </span>

          <strong>

            {
              Number(
                data.dataset.fileCount
                ?? 0
              )
              .toLocaleString()
            }

            개

          </strong>

        </div>

      </section>


      {/* KPI */}
      <section className="dq-kpi-grid">

        <QualityKpi
          title="탐지 건수 합계"
          value={
            data.summary
              .totalIssueRecordCount
          }
          unit="건"
        />


        <QualityKpi
          title="영향 파일"
          value={
            data.summary
              .affectedFileCount
          }
          unit="개"
        />


        <QualityKpi
          title="탐지 품질 유형"
          value={
            data.summary
              .qualityTypeCount
          }
          unit="개"
        />


        <QualityKpi
          title="분석 Job"
          value={
            data.job?.jobId
            ?? '-'
          }
          prefix={
            data.job
              ? '#'
              : ''
          }
        />

      </section>


      {/* 품질 건수 주의 */}
      <div className="dq-note">

        품질 유형별 탐지 건수는 동일 원본 행이
        여러 규칙에 동시에 포함될 수 있습니다.
        따라서 탐지 건수 합계를 원본 전체 Record 수와
        직접 비교하지 않습니다.

      </div>


      {/* Graph */}
      <section className="dq-panel">

        <h2>
          파일별 품질 탐지 건수
        </h2>

        <p>
          DATASET_FILE 전체를 기준으로 표시합니다.
          품질 결과가 없는 파일도 0건으로 포함됩니다.
        </p>


        {
          fileChartData.length > 0
            ? (

              <ResponsiveContainer
                width="100%"
                height={340}
              >

                <BarChart
                  data={
                    fileChartData
                  }
                >

                  <CartesianGrid
                    stroke="#D9E6F2"
                    strokeDasharray="4 4"
                    vertical={false}
                  />


                  <XAxis
                    dataKey="fileName"
                    stroke="#607D98"
                  />


                  <YAxis
                    stroke="#607D98"
                    tickFormatter={
                      value =>
                        Number(
                          value
                        )
                        .toLocaleString()
                    }
                    label={{
                      value:
                        '탐지 건수',

                      angle:
                        -90,

                      position:
                        'insideLeft',

                      fill:
                        '#607D98'
                    }}
                  />


                  <Tooltip
                    formatter={
                      value => [

                        Number(
                          value
                        )
                        .toLocaleString()
                        + '건',

                        '탐지 건수 합계'
                      ]
                    }
                  />


                  <Legend />


                  <Bar
                    dataKey="recordCount"
                    name="품질 탐지 건수"
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

            )
            : (

              <div className="dq-empty">
                표시할 Dataset File이 없습니다.
              </div>

            )
        }

      </section>


      {/* Detail */}
      <section className="dq-panel">

        <h2>
          품질 이슈 상세
        </h2>

        <p>
          DATASET_FILE을 기준으로
          DATA_QUALITY_STAT을 LEFT JOIN한 결과입니다.
          품질 결과가 없는 파일은
          "탐지 없음"으로 표시합니다.
        </p>


        <div className="dq-table-wrapper">

          <table>

            <thead>

              <tr>

                <th>
                  File Type
                </th>

                <th>
                  파일명
                </th>

                <th>
                  Quality Type
                </th>

                <th>
                  Issue Count
                </th>

                <th>
                  원본 Record
                </th>

                <th>
                  Load Status
                </th>

                <th>
                  Sample
                </th>

              </tr>

            </thead>


            <tbody>

              {
                data.items.length === 0
                  ? (

                    <tr>

                      <td
                        colSpan="7"
                        className="dq-empty"
                      >

                        이 Dataset에는 표시할
                        Data Quality 결과가 없습니다.

                      </td>

                    </tr>

                  )
                  : data.items.map(
                    item => {

                      const detected =
                        hasDetection(
                          item
                        )


                      return (

                        <tr
                          key={
                            item.datasetFileId
                            + '-'
                            + (
                              item.qualityType
                              ?? 'NO_DETECTION'
                            )
                          }
                        >

                          <td>
                            {
                              item.fileType
                            }
                          </td>


                          <td>
                            {
                              item.originalFileName
                            }
                          </td>


                          <td>

                            <span
                              className={
                                detected
                                  ? 'dq-quality-badge'
                                  : 'dq-quality-badge no-detection'
                              }
                            >

                              {
                                getQualityLabel(
                                  item
                                )
                              }

                            </span>

                          </td>


                          <td>

                            {
                              Number(
                                item.recordCount
                                ?? 0
                              )
                              .toLocaleString()
                            }

                          </td>


                          <td>

                            {
                              item.fileRecordCount
                              == null
                                ? '-'
                                : Number(
                                    item.fileRecordCount
                                  )
                                  .toLocaleString()
                            }

                          </td>


                          <td>
                            {
                              item.loadStatus
                            }
                          </td>


                          <td
                            className="dq-sample"
                            title={
                              item.sampleMessage
                              ?? ''
                            }
                          >

                            {
                              item.sampleMessage
                              ?? '-'
                            }

                          </td>

                        </tr>

                      )
                    }
                  )
              }

            </tbody>

          </table>

        </div>

      </section>


      {/* Job */}
      {
        data.job
          ? (

            <details className="dq-job">

              <summary>
                분석 Job 정보
              </summary>


              <div className="dq-job-grid">

                <JobItem
                  label="Job ID"
                  value={
                    '#'
                    + data.job.jobId
                  }
                />


                <JobItem
                  label="Status"
                  value={
                    data.job.status
                  }
                />


                <JobItem
                  label="Output Record"
                  value={
                    Number(
                      data.job
                        .outputRecordCount
                      ?? 0
                    )
                    .toLocaleString()
                  }
                />


                <JobItem
                  label="Duplicate Record"
                  value={
                    Number(
                      data.job
                        .duplicateRecordCount
                      ?? 0
                    )
                    .toLocaleString()
                  }
                />


                <JobItem
                  label="Oracle 적재"
                  value={
                    data.job
                      .resultImportedYn
                  }
                />


                <JobItem
                  label="HDFS Output"
                  value={
                    data.job
                      .hdfsOutputPath
                  }
                />

              </div>

            </details>

          )
          : (

            <div className="dq-job dq-job-empty">

              선택한 Dataset에는
              SUCCESS + Oracle 적재완료 상태의
              Data Quality Job이 없습니다.

            </div>

          )
      }

    </main>
  )
}


/**
 * Data Quality KPI.
 */
function QualityKpi({
  title,
  value,
  unit,
  prefix = ''
}) {

  const displayValue =
    typeof value === 'number'
      ? value.toLocaleString()
      : value


  return (
    <article className="dq-kpi">

      <span>
        {title}
      </span>

      <strong>
        {prefix}
        {displayValue}
      </strong>

      {
        unit && (
          <small>
            {unit}
          </small>
        )
      }

    </article>
  )
}


/**
 * Job 정보.
 */
function JobItem({
  label,
  value
}) {

  return (
    <div className="dq-job-item">

      <span>
        {label}
      </span>

      <strong>
        {value ?? '-'}
      </strong>

    </div>
  )
}


export default DataQualityPage