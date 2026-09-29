import {
  useEffect,
  useState
} from 'react'

import {
  createPortal
} from 'react-dom'

import {
  getDatasetDetail,
  getDatasets
} from '../api/datasetApi'

import '../styles/dataset.css'


/**
 * Byte 단위 파일 크기 표시.
 */
function formatBytes(bytes) {

  if (
    bytes === null
    ||
    bytes === undefined
  ) {
    return '-'
  }

  const value =
    Number(bytes)

  if (value < 1024) {
    return value + ' B'
  }

  if (value < 1024 * 1024) {

    return (
      (
        value / 1024
      ).toFixed(2)
      + ' KB'
    )
  }

  if (
    value
    <
    1024 * 1024 * 1024
  ) {

    return (
      (
        value
        /
        (
          1024 * 1024
        )
      ).toFixed(2)
      + ' MB'
    )
  }

  return (
    (
      value
      /
      (
        1024
        *
        1024
        *
        1024
      )
    ).toFixed(2)
    + ' GB'
  )
}


/**
 * Dataset 조회 화면.
 */
function DatasetPage() {

  const [
    datasets,
    setDatasets
  ] = useState([])


  const [
    selectedDatasetId,
    setSelectedDatasetId
  ] = useState('')


  const [
    detail,
    setDetail
  ] = useState(null)


  const [
    selectedFile,
    setSelectedFile
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
   * Dataset 목록 초기 조회.
   */
  useEffect(() => {

    async function loadDatasets() {

      try {

        const data =
          await getDatasets()

        setDatasets(data)


        /*
         * DB에 실제 존재하는 첫 Dataset 사용.
         * 특정 ID를 임의 생성하지 않는다.
         */
        if (data.length > 0) {

          setSelectedDatasetId(
            String(
              data[0].datasetId
            )
          )
        }

      } catch (err) {

        setError(err.message)

      } finally {

        setLoading(false)
      }
    }


    loadDatasets()

  }, [])


  /**
   * Dataset 선택 변경 시 상세조회.
   */
  useEffect(() => {

    if (!selectedDatasetId) {
      return
    }


    async function loadDetail() {

      try {

        setLoading(true)

        const data =
          await getDatasetDetail(
            selectedDatasetId
          )

        setDetail(data)

        setSelectedFile(null)

        setError(null)

      } catch (err) {

        setError(err.message)

      } finally {

        setLoading(false)
      }
    }


    loadDetail()

  }, [
    selectedDatasetId
  ])


  /**
   * 파일 상세 Popup이 열리면
   * 배경 Scroll을 막는다.
   */
  useEffect(() => {

    if (!selectedFile) {
      return
    }

    const previous =
      document.body.style.overflow


    function handleEscape(event) {

      if (event.key === 'Escape') {

        setSelectedFile(null)
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
        previous

      window.removeEventListener(
        'keydown',
        handleEscape
      )
    }

  }, [
    selectedFile
  ])


  return (
    <main className="dataset-page">


      <header className="dataset-header">

        <div>

          <p>
            OULAD 원본 데이터 및 적재 메타데이터
          </p>

          <h1>
            Dataset
          </h1>

          <span>
            EduScope 분석에 사용된 원본 Dataset과
            파일별 적재·HDFS·검증 정보를 확인합니다.
          </span>

        </div>


        <div className="dataset-selector">

          <label>
            Dataset
          </label>

          <select
            value={
              selectedDatasetId
            }
            onChange={(event) =>
              setSelectedDatasetId(
                event.target.value
              )
            }
          >

            {datasets.map(
              (dataset) => (

                <option
                  key={
                    dataset.datasetId
                  }
                  value={
                    dataset.datasetId
                  }
                >

                  {
                    dataset.displayName
                  }

                </option>

              )
            )}

          </select>

        </div>

      </header>


      {error && (

        <div className="page-error">
          {error}
        </div>

      )}


      {loading && (

        <div className="dataset-loading">
          Dataset 정보를 불러오는 중입니다.
        </div>

      )}


      {!loading && detail && (
        <>


          {/* Dataset 기본정보 */}
          <section className="dataset-info-card">

            <div>

              <span>
                Dataset
              </span>

              <strong>
                {
                  detail
                    .dataset
                    .displayName
                }
              </strong>

            </div>


            <div>

              <span>
                Source
              </span>

              <strong>
                {
                  detail
                    .dataset
                    .sourceName
                }
              </strong>

            </div>


            <div>

              <span>
                Dataset Version
              </span>

              <strong>
                {
                  detail
                    .dataset
                    .datasetVersion
                }
              </strong>

            </div>


            <div>

              <span>
                Schema Version
              </span>

              <strong>
                {
                  detail
                    .dataset
                    .schemaVersion
                }
              </strong>

            </div>

          </section>


          {/* KPI */}
          <section className="dataset-kpi-grid">

            <DatasetKpi
              title="실제 파일"
              value={
                detail
                  .summary
                  .actualFileCount
              }
              unit="개"
            />


            <DatasetKpi
              title="전체 Record"
              value={
                detail
                  .summary
                  .totalRecordCount
              }
              unit="건"
            />


            <DatasetKpi
              title="전체 파일 크기"
              value={
                formatBytes(
                  detail
                    .summary
                    .totalFileSizeBytes
                )
              }
            />


            <DatasetKpi
              title="Hash 등록"
              value={
                detail
                  .summary
                  .hashedFileCount
              }
              unit="개"
            />

          </section>


          {/* Dataset Metadata */}
          <section className="dataset-panel">

            <h2>
              Dataset 정보
            </h2>


            <div className="dataset-meta-grid">

              <MetaItem
                label="Source Type"
                value={
                  detail
                    .dataset
                    .sourceType
                }
              />

              <MetaItem
                label="Source Name"
                value={
                  detail
                    .dataset
                    .sourceName
                }
              />

              <MetaItem
                label="HDFS Base Path"
                value={
                  detail
                    .dataset
                    .hdfsBasePath
                }
              />

              <MetaItem
                label="등록 파일 수"
                value={
                  detail
                    .dataset
                    .fileCount
                }
              />

            </div>


            {detail.dataset.description && (

              <div className="dataset-description">

                {
                  detail
                    .dataset
                    .description
                }

              </div>

            )}

          </section>


          {/* Files */}
          <section className="dataset-panel">

            <div className="dataset-panel-header">

              <div>

                <h2>
                  Dataset Files
                </h2>

                <p>
                  파일을 클릭하면 전체 경로와
                  SHA-256 Hash를 확인할 수 있습니다.
                </p>

              </div>

            </div>


            <div className="dataset-table-wrapper">

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
                      크기
                    </th>

                    <th>
                      Records
                    </th>

                    <th>
                      Load Status
                    </th>

                    <th>
                      Hash
                    </th>

                  </tr>

                </thead>


                <tbody>

                  {detail.files.map(
                    (file) => (

                      <tr
                        key={
                          file.datasetFileId
                        }
                        onClick={() =>
                          setSelectedFile(
                            file
                          )
                        }
                      >

                        <td>
                          {file.fileType}
                        </td>

                        <td>
                          {
                            file
                              .originalFileName
                          }
                        </td>

                        <td>
                          {
                            formatBytes(
                              file
                                .fileSizeBytes
                            )
                          }
                        </td>

                        <td>
                          {
                            file.recordCount
                            == null
                              ? '-'
                              : Number(
                                  file
                                    .recordCount
                                )
                                .toLocaleString()
                          }
                        </td>

                        <td>

                          <span className="dataset-status">

                            {
                              file
                                .loadStatus
                              ?? '-'
                            }

                          </span>

                        </td>

                        <td className="dataset-hash-short">

                          {
                            file.contentHash
                              ? file
                                  .contentHash
                                  .slice(
                                    0,
                                    12
                                  )
                                  + '...'
                              : '-'
                          }

                        </td>

                      </tr>

                    )
                  )}

                </tbody>

              </table>

            </div>

          </section>


          {/* HDFS */}
          <details className="dataset-hdfs-info">

            <summary>
              HDFS / Source 정보
            </summary>

            <p>
              HDFS Base Path:
              {' '}
              {
                detail
                  .dataset
                  .hdfsBasePath
                ?? '-'
              }
            </p>

            <p>
              Source URL:
              {' '}
              {
                detail
                  .dataset
                  .sourceUrl
                ?? '-'
              }
            </p>

          </details>

        </>
      )}


      {/* ================================================
          Dataset File 상세 Portal
          ================================================ */}

      {
        selectedFile
        &&
        createPortal(

          <div
            className="dataset-file-overlay"
            onClick={() =>
              setSelectedFile(null)
            }
          >

            <section
              className="dataset-file-modal"

              role="dialog"

              aria-modal="true"

              aria-label="Dataset File 상세정보"

              onClick={(event) =>
                event.stopPropagation()
              }
            >


              <header>

                <div>

                  <span>
                    DATASET FILE
                  </span>

                  <h2>
                    {
                      selectedFile
                        .originalFileName
                    }
                  </h2>

                  <p>
                    {
                      selectedFile
                        .fileType
                    }
                  </p>

                </div>


                <button
                  type="button"
                  onClick={() =>
                    setSelectedFile(
                      null
                    )
                  }
                >
                  닫기 ×
                </button>

              </header>


              <div className="dataset-file-detail-grid">

                <FileDetailItem
                  label="Dataset File ID"
                  value={
                    selectedFile
                      .datasetFileId
                  }
                />

                <FileDetailItem
                  label="Load Status"
                  value={
                    selectedFile
                      .loadStatus
                  }
                />

                <FileDetailItem
                  label="File Size"
                  value={
                    formatBytes(
                      selectedFile
                        .fileSizeBytes
                    )
                  }
                />

                <FileDetailItem
                  label="Record Count"
                  value={
                    selectedFile.recordCount
                    == null
                      ? '-'
                      : Number(
                          selectedFile
                            .recordCount
                        )
                        .toLocaleString()
                  }
                />

              </div>


              <FileLongItem
                label="SHA-256 Content Hash"
                value={
                  selectedFile
                    .contentHash
                }
              />

              <FileLongItem
                label="HDFS Raw Path"
                value={
                  selectedFile
                    .hdfsRawPath
                }
              />

              <FileLongItem
                label="HDFS Clean Path"
                value={
                  selectedFile
                    .hdfsCleanPath
                }
              />

            </section>

          </div>,

          document.body
        )
      }

    </main>
  )
}


function DatasetKpi({
  title,
  value,
  unit
}) {

  const displayValue =
    typeof value === 'number'
      ? value.toLocaleString()
      : value


  return (
    <article className="dataset-kpi">

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


function MetaItem({
  label,
  value
}) {

  return (
    <div className="dataset-meta-item">

      <span>
        {label}
      </span>

      <strong>
        {value ?? '-'}
      </strong>

    </div>
  )
}


function FileDetailItem({
  label,
  value
}) {

  return (
    <div className="dataset-file-detail-item">

      <span>
        {label}
      </span>

      <strong>
        {value ?? '-'}
      </strong>

    </div>
  )
}


function FileLongItem({
  label,
  value
}) {

  return (
    <div className="dataset-file-long-item">

      <span>
        {label}
      </span>

      <code>
        {value ?? '-'}
      </code>

    </div>
  )
}


export default DatasetPage