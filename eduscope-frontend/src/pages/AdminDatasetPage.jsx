import {
  useEffect,
  useMemo,
  useState
} from 'react'

import {
  createAdminDataset,
  createDatasetFile,
  createDatasetVersion,
  deleteAdminDataset,
  getAdminDatasetFiles,
  getAdminDatasets,
  updateAdminDataset,
  updateDatasetFileChecksum
} from '../api/adminDatasetApi'

import '../styles/adminDataset.css'


const EMPTY_CREATE_FORM = {
  displayName: '',
  description: '',
  sourceName: '',
  sourceUrl: '',
  datasetVersion: '',
  schemaVersion: '',
  hdfsBasePath: ''
}


const DATASET_FILE_TYPES = [
  'COURSES',
  'ASSESSMENTS',
  'VLE',
  'STUDENT_INFO',
  'STUDENT_REGISTRATION',
  'STUDENT_ASSESSMENT',
  'STUDENT_VLE'
]


const DATASET_FILE_LOAD_STATUS = [
  'REGISTERED',
  'VALIDATED',
  'HDFS_STORED',
  'FAILED'
]


const EMPTY_VERSION_FORM = {
  datasetVersion: '',
  schemaVersion: '',
  hdfsBasePath: ''
}


const EMPTY_FILE_FORM = {
  fileType: '',
  originalFileName: '',
  fileSizeBytes: '',
  recordCount: '',
  contentHash: '',
  hdfsRawPath: '',
  hdfsCleanPath: '',
  loadStatus: 'REGISTERED'
}


/**
 * ADMIN Dataset 관리 화면.
 *
 * 기존 기능:
 * - Dataset 조회
 * - Dataset 등록
 * - Dataset 수정
 * - Dataset 논리삭제
 * - Dataset File 조회
 *
 * 추가 기능:
 * - 새 Dataset Version 등록
 * - Dataset File 메타정보 등록
 * - SHA-256 Checksum 갱신
 */
function AdminDatasetPage() {
  const [datasets, setDatasets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [keyword, setKeyword] = useState('')

  const [createForm, setCreateForm] =
    useState(EMPTY_CREATE_FORM)

  const [createOpen, setCreateOpen] =
    useState(false)

  const [selectedDataset, setSelectedDataset] =
    useState(null)

  const [editForm, setEditForm] =
    useState(null)

  const [saving, setSaving] =
    useState(false)

  const [message, setMessage] =
    useState('')

  // DATASET_FILE 조회 상태
  const [fileDataset, setFileDataset] =
    useState(null)

  const [datasetFiles, setDatasetFiles] =
    useState([])

  const [filesLoading, setFilesLoading] =
    useState(false)

  const [filesError, setFilesError] =
    useState('')

  // 새 Dataset Version 등록
  const [versionDataset, setVersionDataset] =
    useState(null)

  const [versionForm, setVersionForm] =
    useState(EMPTY_VERSION_FORM)

  // DATASET_FILE 등록
  const [fileCreateOpen, setFileCreateOpen] =
    useState(false)

  const [fileForm, setFileForm] =
    useState(EMPTY_FILE_FORM)

  // SHA-256 수정
  const [checksumFile, setChecksumFile] =
    useState(null)

  const [checksumValue, setChecksumValue] =
    useState('')


  /**
   * 최초 Dataset 조회.
   */
  useEffect(() => {
    let cancelled = false

    async function loadInitialDatasets() {
      try {
        const result =
          await getAdminDatasets()

        if (cancelled) {
          return
        }

        setDatasets(
          Array.isArray(result)
            ? result
            : []
        )

        setError('')
      } catch (err) {
        if (!cancelled) {
          setError(err.message)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    loadInitialDatasets()

    return () => {
      cancelled = true
    }
  }, [])


  /**
   * 최신 Dataset 목록 재조회.
   */
  async function refreshDatasets() {
    const result =
      await getAdminDatasets()

    const normalized =
      Array.isArray(result)
        ? result
        : []

    setDatasets(normalized)

    return normalized
  }


  /**
   * Dataset 검색.
   */
  const filteredDatasets =
    useMemo(() => {
      const normalized =
        keyword
          .trim()
          .toLowerCase()

      if (!normalized) {
        return datasets
      }

      return datasets.filter(
        dataset =>
          dataset.displayName
            ?.toLowerCase()
            .includes(normalized)
          ||
          dataset.sourceName
            ?.toLowerCase()
            .includes(normalized)
          ||
          dataset.datasetVersion
            ?.toLowerCase()
            .includes(normalized)
      )
    }, [
      datasets,
      keyword
    ])


  /**
   * 신규 Dataset Form.
   */
  function changeCreateField(event) {
    const {
      name,
      value
    } = event.target

    setCreateForm(
      previous => ({
        ...previous,
        [name]: value
      })
    )
  }


  /**
   * Dataset 수정 Form 열기.
   */
  function openEditor(dataset) {
    setSelectedDataset(dataset)

    setEditForm({
      displayName:
        dataset.displayName ?? '',

      description:
        dataset.description ?? '',

      sourceName:
        dataset.sourceName ?? '',

      sourceUrl:
        dataset.sourceUrl ?? '',

      hdfsBasePath:
        dataset.hdfsBasePath ?? ''
    })

    setMessage('')
  }


  /**
   * Dataset 수정 Form 값 변경.
   */
  function changeEditField(event) {
    const {
      name,
      value
    } = event.target

    setEditForm(
      previous => ({
        ...previous,
        [name]: value
      })
    )
  }


  /**
   * Dataset의 DATASET_FILE 조회.
   */
  async function openDatasetFiles(dataset) {
    try {
      setFileDataset(dataset)
      setDatasetFiles([])
      setFilesLoading(true)
      setFilesError('')

      const result =
        await getAdminDatasetFiles(
          dataset.datasetId
        )

      setDatasetFiles(
        Array.isArray(result)
          ? result
          : []
      )
    } catch (err) {
      setDatasetFiles([])
      setFilesError(err.message)
    } finally {
      setFilesLoading(false)
    }
  }


  /**
   * Dataset File Panel 닫기.
   */
  function closeDatasetFiles() {
    setFileDataset(null)
    setDatasetFiles([])
    setFilesError('')
    setFileCreateOpen(false)
    setChecksumFile(null)
    setChecksumValue('')
  }


  /**
   * 신규 Dataset 등록.
   */
  async function handleCreate() {
    if (
      !createForm.displayName.trim()
      ||
      !createForm.sourceName.trim()
      ||
      !createForm.datasetVersion.trim()
      ||
      !createForm.schemaVersion.trim()
    ) {
      setMessage(
        '필수 항목을 입력해주세요.'
      )

      return
    }

    try {
      setSaving(true)
      setMessage('')

      await createAdminDataset({
        displayName:
          createForm.displayName.trim(),

        description:
          createForm.description.trim(),

        sourceName:
          createForm.sourceName.trim(),

        sourceUrl:
          createForm.sourceUrl.trim(),

        datasetVersion:
          createForm.datasetVersion.trim(),

        schemaVersion:
          createForm.schemaVersion.trim(),

        hdfsBasePath:
          createForm.hdfsBasePath.trim()
      })

      await refreshDatasets()

      setCreateForm(
        EMPTY_CREATE_FORM
      )

      setCreateOpen(false)

      setMessage(
        'Dataset이 등록되었습니다.'
      )
    } catch (err) {
      setMessage(err.message)
    } finally {
      setSaving(false)
    }
  }


  /**
   * Dataset 메타정보 수정.
   */
  async function handleUpdate() {
    if (
      !selectedDataset
      ||
      !editForm
    ) {
      return
    }

    if (
      !editForm.displayName.trim()
      ||
      !editForm.sourceName.trim()
    ) {
      setMessage(
        'Dataset 표시 이름과 출처 이름은 필수입니다.'
      )

      return
    }

    try {
      setSaving(true)
      setMessage('')

      await updateAdminDataset(
        selectedDataset.datasetId,
        {
          displayName:
            editForm.displayName.trim(),

          description:
            editForm.description.trim(),

          sourceName:
            editForm.sourceName.trim(),

          sourceUrl:
            editForm.sourceUrl.trim(),

          hdfsBasePath:
            editForm.hdfsBasePath.trim()
        }
      )

      const latest =
        await refreshDatasets()

      const refreshed =
        latest.find(
          dataset =>
            dataset.datasetId
            === selectedDataset.datasetId
        )

      if (refreshed) {
        setSelectedDataset(refreshed)

        if (
          fileDataset?.datasetId
          === refreshed.datasetId
        ) {
          setFileDataset(refreshed)
        }
      }

      setMessage(
        'Dataset 메타정보가 수정되었습니다.'
      )
    } catch (err) {
      setMessage(err.message)
    } finally {
      setSaving(false)
    }
  }


  /**
   * Dataset 논리삭제.
   */
  async function handleDelete(dataset) {
    if (
      dataset.isDeleted
      === 'Y'
    ) {
      return
    }

    const confirmed =
      window.confirm(
        'Dataset을 논리 삭제하시겠습니까?\n'
        + '데이터는 물리적으로 삭제되지 않습니다.'
      )

    if (!confirmed) {
      return
    }

    try {
      setSaving(true)
      setMessage('')

      await deleteAdminDataset(
        dataset.datasetId
      )

      const latest =
        await refreshDatasets()

      if (
        selectedDataset?.datasetId
        === dataset.datasetId
      ) {
        setSelectedDataset(null)
        setEditForm(null)
      }

      // 파일 정보는 과거 기록 조회를 위해 유지한다.
      if (
        fileDataset?.datasetId
        === dataset.datasetId
      ) {
        const refreshed =
          latest.find(
            item =>
              item.datasetId
              === dataset.datasetId
          )

        if (refreshed) {
          setFileDataset(refreshed)
        }
      }

      setMessage(
        'Dataset이 논리 삭제되었습니다.'
      )
    } catch (err) {
      setMessage(err.message)
    } finally {
      setSaving(false)
    }
  }


  /**
   * 새 Dataset Version 등록 화면.
   */
  function openVersionCreator(dataset) {
    setVersionDataset(dataset)

    setVersionForm({
      datasetVersion: '',

      schemaVersion:
        dataset.schemaVersion ?? '',

      hdfsBasePath:
        dataset.hdfsBasePath ?? ''
    })

    setMessage('')
  }


  function changeVersionField(event) {
    const {
      name,
      value
    } = event.target

    setVersionForm(
      previous => ({
        ...previous,
        [name]: value
      })
    )
  }


  /**
   * 기존 Dataset을 기준으로
   * 새로운 DATASET Version 행 생성.
   */
  async function handleCreateVersion() {
    if (!versionDataset) {
      return
    }

    if (
      !versionForm.datasetVersion.trim()
      ||
      !versionForm.schemaVersion.trim()
      ||
      !versionForm.hdfsBasePath.trim()
    ) {
      setMessage(
        '새 Version, Schema Version, HDFS Base Path를 입력해주세요.'
      )

      return
    }

    try {
      setSaving(true)
      setMessage('')

      await createDatasetVersion(
        versionDataset.datasetId,
        {
          datasetVersion:
            versionForm.datasetVersion.trim(),

          schemaVersion:
            versionForm.schemaVersion.trim(),

          hdfsBasePath:
            versionForm.hdfsBasePath.trim()
        }
      )

      await refreshDatasets()

      setVersionDataset(null)

      setVersionForm(
        EMPTY_VERSION_FORM
      )

      setMessage(
        '새 Dataset Version이 등록되었습니다.'
      )
    } catch (err) {
      setMessage(err.message)
    } finally {
      setSaving(false)
    }
  }


  /**
   * DATASET_FILE 등록 Form 열기.
   */
  function openFileCreator() {
    if (!fileDataset) {
      return
    }

    const existingTypes =
      new Set(
        datasetFiles.map(
          file => file.fileType
        )
      )

    const firstAvailableType =
      DATASET_FILE_TYPES.find(
        type =>
          !existingTypes.has(type)
      )
      ?? ''

    setFileForm({
      ...EMPTY_FILE_FORM,
      fileType: firstAvailableType
    })

    setChecksumFile(null)
    setChecksumValue('')
    setFileCreateOpen(true)
    setMessage('')
  }


  function changeFileField(event) {
    const {
      name,
      value
    } = event.target

    setFileForm(
      previous => ({
        ...previous,
        [name]: value
      })
    )
  }


  /**
   * 실제 원본 파일 메타정보 등록.
   */
  async function handleCreateFile() {
    if (!fileDataset) {
      return
    }

    if (
      !fileForm.fileType.trim()
      ||
      !fileForm.originalFileName.trim()
    ) {
      setMessage(
        'File Type과 원본 파일명은 필수입니다.'
      )

      return
    }

    if (
      fileForm.contentHash.trim()
      &&
      !/^[0-9a-fA-F]{64}$/.test(
        fileForm.contentHash.trim()
      )
    ) {
      setMessage(
        'Checksum은 실제 SHA-256 64자리 16진수 값이어야 합니다.'
      )

      return
    }

    try {
      setSaving(true)
      setMessage('')

      await createDatasetFile(
        fileDataset.datasetId,
        {
          fileType:
            fileForm.fileType.trim(),

          originalFileName:
            fileForm.originalFileName.trim(),

          fileSizeBytes:
            toNullableNumber(
              fileForm.fileSizeBytes
            ),

          recordCount:
            toNullableNumber(
              fileForm.recordCount
            ),

          contentHash:
            toNullableText(
              fileForm.contentHash
            ),

          hdfsRawPath:
            toNullableText(
              fileForm.hdfsRawPath
            ),

          hdfsCleanPath:
            toNullableText(
              fileForm.hdfsCleanPath
            ),

          loadStatus:
            fileForm.loadStatus
        }
      )

      await openDatasetFiles(
        fileDataset
      )

      const latest =
        await refreshDatasets()

      const refreshed =
        latest.find(
          dataset =>
            dataset.datasetId
            === fileDataset.datasetId
        )

      if (refreshed) {
        setFileDataset(refreshed)
      }

      setFileCreateOpen(false)

      setFileForm(
        EMPTY_FILE_FORM
      )

      setMessage(
        'Dataset File 메타정보가 등록되었습니다.'
      )
    } catch (err) {
      setMessage(err.message)
    } finally {
      setSaving(false)
    }
  }


  /**
   * 기존 Checksum 편집.
   */
  function openChecksumEditor(file) {
    setChecksumFile(file)

    setChecksumValue(
      file.contentHash ?? ''
    )

    setFileCreateOpen(false)
    setMessage('')
  }


  /**
   * SHA-256 Checksum 갱신.
   */
  async function handleChecksumUpdate() {
    if (!checksumFile) {
      return
    }

    const normalized =
      checksumValue.trim()

    if (
      !/^[0-9a-fA-F]{64}$/.test(
        normalized
      )
    ) {
      setMessage(
        'Checksum은 실제 SHA-256 64자리 16진수 값이어야 합니다.'
      )

      return
    }

    try {
      setSaving(true)
      setMessage('')

      await updateDatasetFileChecksum(
        checksumFile.datasetFileId,
        normalized
      )

      if (fileDataset) {
        await openDatasetFiles(
          fileDataset
        )
      }

      setChecksumFile(null)
      setChecksumValue('')

      setMessage(
        'SHA-256 Checksum이 갱신되었습니다.'
      )
    } catch (err) {
      setMessage(err.message)
    } finally {
      setSaving(false)
    }
  }


  /**
   * 빈 문자열은 null로 변환.
   */
  function toNullableText(value) {
    const normalized =
      value.trim()

    return normalized
      ? normalized
      : null
  }


  /**
   * 숫자 입력 검증.
   */
  function toNullableNumber(value) {
    if (
      value === null
      ||
      value === undefined
      ||
      String(value).trim() === ''
    ) {
      return null
    }

    const numberValue =
      Number(value)

    if (
      !Number.isFinite(numberValue)
      ||
      numberValue < 0
    ) {
      throw new Error(
        '파일 크기와 Record Count는 0 이상의 숫자여야 합니다.'
      )
    }

    return numberValue
  }


  if (loading) {
    return (
      <main className="admin-dataset-page">
        Dataset 정보를 불러오는 중입니다.
      </main>
    )
  }


  return (
    <main className="admin-dataset-page">

      {/* Header */}
      <header className="admin-dataset-header">

        <div>
          <p>
            Data Management
          </p>

          <h1>
            Dataset 관리
          </h1>

          <span>
            Dataset 메타정보와 Version,
            논리 삭제 상태를 관리합니다.
          </span>
        </div>


        <button
          type="button"
          onClick={() => {
            setCreateOpen(
              previous => !previous
            )

            setMessage('')
          }}
        >
          Dataset 등록
        </button>

      </header>


      {/* Error / Message */}
      {
        error && (
          <div className="admin-dataset-error">
            {error}
          </div>
        )
      }

      {
        message && (
          <div className="admin-dataset-message">
            {message}
          </div>
        )
      }


      {/* Dataset 등록 */}
      {
        createOpen && (
          <section className="admin-dataset-form-panel">

            <h2>
              신규 Dataset 등록
            </h2>

            <p className="admin-dataset-guide">
              실제 관리할 Dataset 메타정보만 등록합니다.
              CSV 파일 업로드 기능은 아닙니다.
            </p>

            <DatasetCreateForm
              form={createForm}
              onChange={changeCreateField}
            />

            <div className="admin-dataset-actions">

              <button
                type="button"
                className="secondary"
                disabled={saving}
                onClick={() => {
                  setCreateOpen(false)
                  setMessage('')
                }}
              >
                취소
              </button>


              <button
                type="button"
                disabled={saving}
                onClick={handleCreate}
              >
                {
                  saving
                    ? '저장 중...'
                    : '등록'
                }
              </button>

            </div>

          </section>
        )
      }


      {/* 검색 */}
      <section className="admin-dataset-filter">

        <input
          type="text"
          value={keyword}
          placeholder="Dataset명, 출처, Version 검색"
          onChange={
            event =>
              setKeyword(
                event.target.value
              )
          }
        />

      </section>


      {/* Dataset 목록 */}
      <section className="admin-dataset-panel">

        <div className="admin-dataset-table-wrapper">

          <table>

            <thead>
              <tr>
                <th>ID</th>
                <th>Dataset</th>
                <th>Version</th>
                <th>Schema</th>
                <th>파일 수</th>
                <th>상태</th>
                <th>관리</th>
              </tr>
            </thead>


            <tbody>

              {
                filteredDatasets.length === 0
                  ? (
                    <tr>
                      <td
                        colSpan="7"
                        className="dataset-file-empty"
                      >
                        표시할 Dataset이 없습니다.
                      </td>
                    </tr>
                  )
                  : filteredDatasets.map(
                    dataset => (
                      <tr key={dataset.datasetId}>

                        <td>
                          {dataset.datasetId}
                        </td>


                        <td>
                          <strong>
                            {dataset.displayName}
                          </strong>

                          <div className="dataset-source">
                            {dataset.sourceName}
                          </div>
                        </td>


                        <td>
                          {dataset.datasetVersion}
                        </td>


                        <td>
                          {dataset.schemaVersion}
                        </td>


                        <td>
                          {dataset.fileCount}
                        </td>


                        <td>

                          <span
                            className={
                              dataset.isDeleted === 'Y'
                                ? 'dataset-state deleted'
                                : 'dataset-state active'
                            }
                          >
                            {
                              dataset.isDeleted === 'Y'
                                ? 'DELETED'
                                : 'ACTIVE'
                            }
                          </span>

                        </td>


                        <td>

                          <div className="dataset-row-actions">

                            <button
                              type="button"
                              onClick={() =>
                                openDatasetFiles(
                                  dataset
                                )
                              }
                            >
                              파일
                            </button>


                            <button
                              type="button"
                              disabled={
                                dataset.isDeleted
                                === 'Y'
                              }
                              onClick={() =>
                                openVersionCreator(
                                  dataset
                                )
                              }
                            >
                              새 버전
                            </button>


                            <button
                              type="button"
                              disabled={
                                dataset.isDeleted
                                === 'Y'
                              }
                              onClick={() =>
                                openEditor(
                                  dataset
                                )
                              }
                            >
                              수정
                            </button>


                            <button
                              type="button"
                              className="danger"
                              disabled={
                                dataset.isDeleted
                                === 'Y'
                                ||
                                saving
                              }
                              onClick={() =>
                                handleDelete(
                                  dataset
                                )
                              }
                            >
                              논리삭제
                            </button>

                          </div>

                        </td>

                      </tr>
                    )
                  )
              }

            </tbody>

          </table>

        </div>

      </section>


      {/* 새 Dataset Version */}
      {
        versionDataset && (
          <section className="admin-dataset-form-panel">

            <h2>
              Dataset #{versionDataset.datasetId}
              {' '}
              새 Version 등록
            </h2>


            <p className="admin-dataset-guide">
              기존 Version을 변경하지 않고
              새로운 Dataset 행을 생성합니다.
              실제 운영에 사용할 Version과
              HDFS Base Path만 입력해주세요.
            </p>


            <div className="admin-version-lock">

              <span>
                기준 Dataset
              </span>

              <strong>
                {versionDataset.displayName}
              </strong>

              <span>
                현재 Version
              </span>

              <strong>
                {versionDataset.datasetVersion}
              </strong>

            </div>


            <div className="admin-dataset-form-grid">

              <Field
                label="새 Dataset Version *"
                name="datasetVersion"
                value={
                  versionForm.datasetVersion
                }
                onChange={
                  changeVersionField
                }
              />


              <Field
                label="Schema Version *"
                name="schemaVersion"
                value={
                  versionForm.schemaVersion
                }
                onChange={
                  changeVersionField
                }
              />


              <label className="dataset-full-field">

                HDFS Base Path *

                <input
                  type="text"
                  name="hdfsBasePath"
                  value={
                    versionForm.hdfsBasePath
                  }
                  onChange={
                    changeVersionField
                  }
                />

              </label>

            </div>


            <div className="admin-dataset-actions">

              <button
                type="button"
                className="secondary"
                disabled={saving}
                onClick={() => {
                  setVersionDataset(null)

                  setVersionForm(
                    EMPTY_VERSION_FORM
                  )

                  setMessage('')
                }}
              >
                취소
              </button>


              <button
                type="button"
                disabled={saving}
                onClick={
                  handleCreateVersion
                }
              >
                {
                  saving
                    ? '등록 중...'
                    : '새 Version 등록'
                }
              </button>

            </div>

          </section>
        )
      }


      {/* DATASET_FILE 상세 */}
      {
        fileDataset && (
          <section className="admin-dataset-file-panel">

            <div className="dataset-file-header">

              <div>

                <h2>
                  Dataset File
                </h2>

                <p>
                  Dataset #{fileDataset.datasetId}
                  {' - '}
                  {fileDataset.displayName}
                </p>

              </div>


              <div className="dataset-file-header-actions">

                <button
                  type="button"
                  disabled={
                    fileDataset.isDeleted
                    === 'Y'
                  }
                  onClick={
                    openFileCreator
                  }
                >
                  파일 등록
                </button>


                <button
                  type="button"
                  onClick={
                    closeDatasetFiles
                  }
                >
                  닫기
                </button>

              </div>

            </div>


            {
              filesError && (
                <div className="admin-dataset-error">
                  {filesError}
                </div>
              )
            }


            {
              filesLoading
                ? (
                  <p className="admin-dataset-guide">
                    Dataset File 정보를 불러오는 중입니다.
                  </p>
                )
                : (
                  <div className="admin-dataset-table-wrapper">

                    <table>

                      <thead>
                        <tr>
                          <th>File Type</th>
                          <th>원본 파일명</th>
                          <th>Record Count</th>
                          <th>파일 크기(Byte)</th>
                          <th>Load Status</th>
                          <th>SHA-256</th>
                          <th>HDFS RAW</th>
                          <th>HDFS CLEAN</th>
                          <th>관리</th>
                        </tr>
                      </thead>


                      <tbody>

                        {
                          datasetFiles.length === 0
                            ? (
                              <tr>
                                <td
                                  colSpan="9"
                                  className="dataset-file-empty"
                                >
                                  등록된 Dataset File이 없습니다.
                                </td>
                              </tr>
                            )
                            : datasetFiles.map(
                              file => (
                                <tr key={file.datasetFileId}>

                                  <td>
                                    <strong>
                                      {
                                        file.fileType
                                        ?? '-'
                                      }
                                    </strong>
                                  </td>


                                  <td>
                                    {
                                      file.originalFileName
                                      ?? '-'
                                    }
                                  </td>


                                  <td>
                                    {
                                      file.recordCount != null
                                        ? Number(
                                          file.recordCount
                                        ).toLocaleString()
                                        : '-'
                                    }
                                  </td>


                                  <td>
                                    {
                                      file.fileSizeBytes != null
                                        ? Number(
                                          file.fileSizeBytes
                                        ).toLocaleString()
                                        : '-'
                                    }
                                  </td>


                                  <td>

                                    <span
                                      className={
                                        'dataset-load-status '
                                        + (
                                          file.loadStatus
                                            ?.toLowerCase()
                                          ?? ''
                                        )
                                      }
                                    >
                                      {
                                        file.loadStatus
                                        ?? '-'
                                      }
                                    </span>

                                  </td>


                                  <td className="dataset-hash">
                                    {
                                      file.contentHash
                                      ?? '-'
                                    }
                                  </td>


                                  <td className="dataset-path">
                                    {
                                      file.hdfsRawPath
                                      ?? '-'
                                    }
                                  </td>


                                  <td className="dataset-path">
                                    {
                                      file.hdfsCleanPath
                                      ?? '-'
                                    }
                                  </td>


                                  <td>

                                    <button
                                      type="button"
                                      className="dataset-inline-action"
                                      disabled={
                                        fileDataset.isDeleted
                                        === 'Y'
                                      }
                                      onClick={() =>
                                        openChecksumEditor(
                                          file
                                        )
                                      }
                                    >
                                      Checksum
                                    </button>

                                  </td>

                                </tr>
                              )
                            )
                        }

                      </tbody>

                    </table>

                  </div>
                )
            }


            {/* Dataset File 등록 */}
            {
              fileCreateOpen && (
                <div className="dataset-management-subpanel">

                  <h3>
                    Dataset File 등록
                  </h3>


                  <p className="admin-dataset-guide">
                    실제 원본 파일에서 확인한 값만 입력합니다.
                    Checksum은 SHA-256을 사용하는 경우
                    64자리 16진수여야 합니다.
                  </p>


                  <div className="admin-dataset-form-grid">

                    <label>

                      File Type *

                      <select
                        name="fileType"
                        value={
                          fileForm.fileType
                        }
                        onChange={
                          changeFileField
                        }
                      >

                        <option value="">
                          선택
                        </option>


                        {
                          DATASET_FILE_TYPES.map(
                            type => (
                              <option
                                key={type}
                                value={type}
                                disabled={
                                  datasetFiles.some(
                                    file =>
                                      file.fileType
                                      === type
                                  )
                                }
                              >
                                {type}
                              </option>
                            )
                          )
                        }

                      </select>

                    </label>


                    <Field
                      label="원본 파일명 *"
                      name="originalFileName"
                      value={
                        fileForm.originalFileName
                      }
                      onChange={
                        changeFileField
                      }
                    />


                    <label>

                      파일 크기(Byte)

                      <input
                        type="number"
                        min="0"
                        name="fileSizeBytes"
                        value={
                          fileForm.fileSizeBytes
                        }
                        onChange={
                          changeFileField
                        }
                      />

                    </label>


                    <label>

                      Record Count

                      <input
                        type="number"
                        min="0"
                        name="recordCount"
                        value={
                          fileForm.recordCount
                        }
                        onChange={
                          changeFileField
                        }
                      />

                    </label>


                    <label>

                      Load Status

                      <select
                        name="loadStatus"
                        value={
                          fileForm.loadStatus
                        }
                        onChange={
                          changeFileField
                        }
                      >

                        {
                          DATASET_FILE_LOAD_STATUS.map(
                            status => (
                              <option
                                key={status}
                                value={status}
                              >
                                {status}
                              </option>
                            )
                          )
                        }

                      </select>

                    </label>


                    <label className="dataset-full-field">

                      SHA-256 Checksum

                      <input
                        type="text"
                        name="contentHash"
                        value={
                          fileForm.contentHash
                        }
                        maxLength="64"
                        onChange={
                          changeFileField
                        }
                      />

                    </label>


                    <label className="dataset-full-field">

                      HDFS RAW Path

                      <input
                        type="text"
                        name="hdfsRawPath"
                        value={
                          fileForm.hdfsRawPath
                        }
                        onChange={
                          changeFileField
                        }
                      />

                    </label>


                    <label className="dataset-full-field">

                      HDFS CLEAN Path

                      <input
                        type="text"
                        name="hdfsCleanPath"
                        value={
                          fileForm.hdfsCleanPath
                        }
                        onChange={
                          changeFileField
                        }
                      />

                    </label>

                  </div>


                  <div className="admin-dataset-actions">

                    <button
                      type="button"
                      className="secondary"
                      disabled={saving}
                      onClick={() => {
                        setFileCreateOpen(false)

                        setFileForm(
                          EMPTY_FILE_FORM
                        )

                        setMessage('')
                      }}
                    >
                      취소
                    </button>


                    <button
                      type="button"
                      disabled={
                        saving
                        ||
                        !fileForm.fileType
                      }
                      onClick={
                        handleCreateFile
                      }
                    >
                      {
                        saving
                          ? '등록 중...'
                          : '파일 메타정보 등록'
                      }
                    </button>

                  </div>

                </div>
              )
            }


            {/* Checksum 편집 */}
            {
              checksumFile && (
                <div className="dataset-management-subpanel">

                  <h3>
                    SHA-256 Checksum 갱신
                  </h3>


                  <p className="admin-dataset-guide">
                    {checksumFile.originalFileName}
                    {' / '}
                    {checksumFile.fileType}
                  </p>


                  <label className="dataset-checksum-editor">

                    실제 SHA-256

                    <input
                      type="text"
                      value={
                        checksumValue
                      }
                      maxLength="64"
                      onChange={
                        event =>
                          setChecksumValue(
                            event.target.value
                          )
                      }
                    />

                  </label>


                  <div className="admin-dataset-actions">

                    <button
                      type="button"
                      className="secondary"
                      disabled={saving}
                      onClick={() => {
                        setChecksumFile(null)
                        setChecksumValue('')
                        setMessage('')
                      }}
                    >
                      취소
                    </button>


                    <button
                      type="button"
                      disabled={saving}
                      onClick={
                        handleChecksumUpdate
                      }
                    >
                      {
                        saving
                          ? '저장 중...'
                          : 'Checksum 저장'
                      }
                    </button>

                  </div>

                </div>
              )
            }

          </section>
        )
      }


      {/* Dataset 수정 */}
      {
        selectedDataset
        &&
        editForm
        && (
          <section className="admin-dataset-form-panel">

            <h2>
              Dataset #{selectedDataset.datasetId} 수정
            </h2>


            <div className="admin-version-lock">

              <span>
                Dataset Version
              </span>

              <strong>
                {
                  selectedDataset.datasetVersion
                }
              </strong>


              <span>
                Schema Version
              </span>

              <strong>
                {
                  selectedDataset.schemaVersion
                }
              </strong>

            </div>


            <p className="admin-dataset-guide">
              Version은 과거 분석 재현성을 위해
              기존 Dataset에서 변경하지 않습니다.
            </p>


            <DatasetEditForm
              form={editForm}
              onChange={changeEditField}
            />


            <div className="admin-dataset-actions">

              <button
                type="button"
                className="secondary"
                disabled={saving}
                onClick={() => {
                  setSelectedDataset(null)
                  setEditForm(null)
                  setMessage('')
                }}
              >
                닫기
              </button>


              <button
                type="button"
                disabled={saving}
                onClick={
                  handleUpdate
                }
              >
                {
                  saving
                    ? '저장 중...'
                    : '변경사항 저장'
                }
              </button>

            </div>

          </section>
        )
      }

    </main>
  )
}


/**
 * Dataset 등록 Form.
 */
function DatasetCreateForm({
  form,
  onChange
}) {
  return (
    <div className="admin-dataset-form-grid">

      <Field
        label="Dataset 표시 이름 *"
        name="displayName"
        value={form.displayName}
        onChange={onChange}
      />

      <Field
        label="출처 이름 *"
        name="sourceName"
        value={form.sourceName}
        onChange={onChange}
      />

      <Field
        label="Dataset Version *"
        name="datasetVersion"
        value={form.datasetVersion}
        onChange={onChange}
      />

      <Field
        label="Schema Version *"
        name="schemaVersion"
        value={form.schemaVersion}
        onChange={onChange}
      />

      <Field
        label="출처 URL"
        name="sourceUrl"
        value={form.sourceUrl}
        onChange={onChange}
      />

      <Field
        label="HDFS Base Path"
        name="hdfsBasePath"
        value={form.hdfsBasePath}
        onChange={onChange}
      />


      <label className="dataset-full-field">

        설명

        <textarea
          name="description"
          value={form.description}
          onChange={onChange}
        />

      </label>

    </div>
  )
}


/**
 * Dataset 수정 Form.
 */
function DatasetEditForm({
  form,
  onChange
}) {
  return (
    <div className="admin-dataset-form-grid">

      <Field
        label="Dataset 표시 이름 *"
        name="displayName"
        value={form.displayName}
        onChange={onChange}
      />

      <Field
        label="출처 이름 *"
        name="sourceName"
        value={form.sourceName}
        onChange={onChange}
      />

      <Field
        label="출처 URL"
        name="sourceUrl"
        value={form.sourceUrl}
        onChange={onChange}
      />

      <Field
        label="HDFS Base Path"
        name="hdfsBasePath"
        value={form.hdfsBasePath}
        onChange={onChange}
      />


      <label className="dataset-full-field">

        설명

        <textarea
          name="description"
          value={form.description}
          onChange={onChange}
        />

      </label>

    </div>
  )
}


/**
 * 공통 Text Field.
 */
function Field({
  label,
  name,
  value,
  onChange
}) {
  return (
    <label>

      {label}

      <input
        type="text"
        name={name}
        value={value}
        onChange={onChange}
      />

    </label>
  )
}


export default AdminDatasetPage