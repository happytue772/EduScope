import {
  useEffect,
  useMemo,
  useState
} from 'react'

import {
  useOutletContext
} from 'react-router-dom'

import {
  createAnalysisJob,
  executeAnalysisJob,
  getAnalysisJobOverview,
  retryAnalysisJob
} from '../api/analysisJobApi'

import '../styles/analysisJob.css'


/**
 * EduScope에서 실제 지원하는 분석 유형.
 */
const ANALYSIS_TYPES = [
  'STUDENT_ACTIVITY',
  'COURSE_ACTIVITY',
  'COURSE_WEEKLY_ACTIVITY',
  'VLE_ACTIVITY',
  'ASSESSMENT',
  'REGISTRATION',
  'COURSE_RESULT',
  'ACTIVITY_RESULT',
  'STUDENT_LEARNING_SUMMARY',
  'DATA_QUALITY'
]


/**
 * Analysis Job 입력 규칙.
 *
 * 사용자가 변경하면 안 되는 경로는 코드에서 고정하고,
 * 실행마다 달라지는 suffix만 입력받는다.
 */
const JOB_FORM_CONFIG = {

  STUDENT_ACTIVITY: {
    versionPrefix: 'student-activity-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-vle'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/student-activity-'
    ]
  },


  COURSE_ACTIVITY: {
    versionPrefix: 'course-activity-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-vle'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/course-activity-'
    ]
  },


  COURSE_WEEKLY_ACTIVITY: {
    versionPrefix:
      'course-weekly-activity-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-vle'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/course-weekly-activity-'
    ]
  },


  VLE_ACTIVITY: {
    versionPrefix:
      'vle-activity-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-vle'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/vle-activity-'
    ]
  },


  ASSESSMENT: {
    versionPrefix:
      'assessment-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-assessment'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/assessment-'
    ]
  },


  REGISTRATION: {
    versionPrefix:
      'registration-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-registration'
      },

      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-info'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/registration-'
    ]
  },


  COURSE_RESULT: {
    versionPrefix:
      'course-result-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-info'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/course-result-'
    ]
  },


  ACTIVITY_RESULT: {
    versionPrefix:
      'activity-result-',

    /*
     * 이전 Student Activity 결과가
     * Input으로 다시 필요하므로
     * 마지막 이름만 사용자가 입력한다.
     */
    inputParts: [
      {
        type: 'dynamic',
        label:
          'Student Activity 결과',

        prefix:
          '/user/user/eduscope/output/student-activity-'
      },

      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-info'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/activity-result-'
    ]
  },


  STUDENT_LEARNING_SUMMARY: {
    versionPrefix:
      'student-learning-summary-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/student-assessment'
      },

      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw/assessments'
      },

      {
        type: 'dynamic',
        label:
          'Student Activity 결과',

        prefix:
          '/user/user/eduscope/output/student-activity-'
      }
    ],

    outputPrefixes: [
      '/user/user/eduscope/output/student-learning-summary-'
    ]
  },


  DATA_QUALITY: {
    versionPrefix:
      'data-quality-',

    inputParts: [
      {
        type: 'fixed',
        value:
          '/user/user/eduscope/raw'
      }
    ],

    /*
     * DATA_QUALITY는 Output이 2개 필요하다.
     * 사용자는 공통 suffix만 입력하고
     * ';' 연결은 코드에서 자동 처리한다.
     */
    outputPrefixes: [
      '/user/user/eduscope/quality/data-quality-row-',
      '/user/user/eduscope/quality/data-quality-duplicate-'
    ]
  }
}


/**
 * 신규 Job 요청 Form.
 *
 * 전체 경로를 직접 입력하지 않고
 * 변경 가능한 부분만 저장한다.
 */
const EMPTY_CREATE_FORM = {
  analysisType:
    'STUDENT_ACTIVITY',

  versionSuffix: '',

  outputSuffix: '',

  upstreamOutputSuffix: ''
}


/**
 * 자유 입력 suffix 검증.
 *
 * 경로 구분자인 /, ; 등을 사용자가
 * 직접 입력하지 못하게 한다.
 */
function isValidPathSuffix(value) {

  return /^[A-Za-z0-9][A-Za-z0-9._-]*$/
    .test(value)
}


/**
 * 선택된 Analysis Type의
 * 고정 설정을 반환한다.
 */
function getJobFormConfig(
  analysisType
) {

  return JOB_FORM_CONFIG[
    analysisType
  ]
}


/**
 * 실제 Backend에 전달할
 * HDFS Input Path를 생성한다.
 */
function buildHdfsInputPath(
  config,
  form
) {

  return config.inputParts
    .map(
      part => {

        if (
          part.type === 'fixed'
        ) {

          return part.value
        }


        return (
          part.prefix
          +
          form
            .upstreamOutputSuffix
            .trim()
        )
      }
    )
    .join(';')
}


/**
 * 실제 Backend에 전달할
 * HDFS Output Path를 생성한다.
 *
 * DATA_QUALITY는 자동으로:
 *
 * row경로;duplicate경로
 *
 * 형태가 된다.
 */
function buildHdfsOutputPath(
  config,
  form
) {

  const suffix =
    form.outputSuffix.trim()


  return config.outputPrefixes
    .map(
      prefix =>
        prefix + suffix
    )
    .join(';')
}


/**
 * 숫자 표시.
 */
function formatNumber(value) {

  if (
    value === null
    ||
    value === undefined
  ) {

    return '-'
  }


  return Number(value)
    .toLocaleString()
}


/**
 * 날짜 / 시간 표시.
 */
function formatDateTime(value) {

  if (!value) {

    return '-'
  }


  return new Date(value)
    .toLocaleString()
}


/**
 * Analysis Job 운영 화면.
 *
 * 기능:
 *
 * - Dataset별 Job 조회
 * - 분석 유형 / 상태 Filter
 * - Job 상세 조회
 * - 신규 PENDING Job 생성
 * - PENDING Job 실제 Hadoop 실행
 * - RUNNING Job 상태 자동 갱신
 * - FAILED Job 재실행 요청
 */
function AnalysisJobPage() {

  const outletContext =
    useOutletContext()


  const roles =
    outletContext?.user?.roles ?? []


  /**
   * Analysis Job 생성 / 실행 / 재실행은
   * 실제 운영 권한인 ADMIN만 사용할 수 있다.
   * DEMO_ADMIN은 Job 이력과 상세 결과만 조회한다.
   */
  const canManageAnalysisJobs =
    roles.includes(
      'ADMIN'
    )


  const isDemoAdmin =
    roles.includes(
      'DEMO_ADMIN'
    )


  const analysisCommandRestrictionMessage =
    isDemoAdmin
      ? '공개 데모에서는 분석 작업 요청·실행·재실행을 사용할 수 없습니다.'
      : '현재 권한에서는 분석 작업 요청·실행·재실행을 사용할 수 없습니다.'


  /*
   * 입력 중 Dataset ID와
   * 실제 조회 중 Dataset ID를 분리한다.
   */
  const [
    datasetIdInput,
    setDatasetIdInput
  ] = useState('1')


  const [
    datasetId,
    setDatasetId
  ] = useState(1)


  const [
    data,
    setData
  ] = useState(null)


  const [
    selectedType,
    setSelectedType
  ] = useState('ALL')


  const [
    selectedStatus,
    setSelectedStatus
  ] = useState('ALL')


  const [
    selectedJob,
    setSelectedJob
  ] = useState(null)


  const [
    loading,
    setLoading
  ] = useState(true)


  const [
    error,
    setError
  ] = useState(null)


  /*
   * 신규 Job 요청 Form.
   */
  const [
    createOpen,
    setCreateOpen
  ] = useState(false)


  const [
    createForm,
    setCreateForm
  ] = useState(
    EMPTY_CREATE_FORM
  )


  /*
   * FAILED Job 재실행용
   * 새로운 HDFS Output 경로.
   */
  const [
    retryOutputPath,
    setRetryOutputPath
  ] = useState('')


  /*
   * 생성 / 재실행 / 실제 실행 요청의
   * 중복 클릭 방지.
   */
  const [
    commandLoading,
    setCommandLoading
  ] = useState(false)


  const [
    commandMessage,
    setCommandMessage
  ] = useState('')


  /*
   * 같은 Dataset에서도
   * Job 변경 후 다시 조회하기 위한 Key.
   */
  const [
    refreshKey,
    setRefreshKey
  ] = useState(0)


  /**
   * Analysis Job 운영 현황 조회.
   */
  useEffect(() => {

    let cancelled = false


    async function loadJobs() {

      try {

        setLoading(true)


        const result =
          await getAnalysisJobOverview(
            datasetId
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

        if (cancelled) {

          return
        }


        setData(
          null
        )

        setError(
          err.message
        )

      } finally {

        if (!cancelled) {

          setLoading(
            false
          )
        }
      }
    }


    loadJobs()


    return () => {

      cancelled = true
    }

  }, [
    datasetId,
    refreshKey
  ])


  /**
   * RUNNING Job이 존재할 때만
   * 3초마다 실제 DB 상태를 조회한다.
   */
  useEffect(() => {

    const hasRunningJob =
      data?.jobs?.some(
        job =>
          job.status === 'RUNNING'
      )


    if (!hasRunningJob) {

      return undefined
    }


    const timer =
      window.setInterval(
        () => {

          setRefreshKey(
            previous =>
              previous + 1
          )

        },
        3000
      )


    return () => {

      window.clearInterval(
        timer
      )
    }

  }, [
    data
  ])


  /**
   * DB에 실제 존재하는
   * Analysis Type 목록.
   */
  const analysisTypes =
    useMemo(
      () => {

        if (!data) {

          return []
        }


        return [
          ...new Set(
            data.jobs
              .map(
                job =>
                  job.analysisType
              )
              .filter(Boolean)
          )
        ].sort()

      },
      [
        data
      ]
    )


  /**
   * 화면 Filter.
   */
  const filteredJobs =
    useMemo(
      () => {

        if (!data) {

          return []
        }


        return data.jobs.filter(
          job => {

            const typeMatch =
              selectedType === 'ALL'
              ||
              job.analysisType
              === selectedType


            const statusMatch =
              selectedStatus === 'ALL'
              ||
              job.status
              === selectedStatus


            return (
              typeMatch
              &&
              statusMatch
            )
          }
        )

      },
      [
        data,
        selectedType,
        selectedStatus
      ]
    )


  /**
   * Dataset 변경 조회.
   */
  function handleDatasetSearch() {

    const value =
      Number(
        datasetIdInput
      )


    if (
      !Number.isInteger(value)
      ||
      value <= 0
    ) {

      setCommandMessage(
        'Dataset ID는 1 이상의 정수여야 합니다.'
      )

      return
    }


    setSelectedJob(
      null
    )

    setSelectedType(
      'ALL'
    )

    setSelectedStatus(
      'ALL'
    )

    setCommandMessage(
      ''
    )


    setDatasetId(
      value
    )
  }


  /**
   * 신규 Job Form 변경.
   */
  function changeCreateField(
    event
  ) {

    const {
      name,
      value
    } = event.target


    /*
     * 분석 유형이 바뀌면
     * 이전 분석용 suffix는 초기화한다.
     */
    if (
      name === 'analysisType'
    ) {

      setCreateForm({
        analysisType:
          value,

        versionSuffix:
          '',

        outputSuffix:
          '',

        upstreamOutputSuffix:
          ''
      })


      setCommandMessage(
        ''
      )


      return
    }


    setCreateForm(
      previous => ({
        ...previous,
        [name]: value
      })
    )
  }


  /**
   * 신규 Analysis Job 요청.
   *
   * 사용자는 자유 입력 부분만 입력하고,
   * 실제 Version/Input/Output은
   * 고정 규칙과 조합한다.
   *
   * 이 단계에서는 Hadoop은 실행하지 않고
   * PENDING Job만 생성한다.
   */
  async function handleCreateJob() {

    if (!canManageAnalysisJobs) {

      setCommandMessage(
        analysisCommandRestrictionMessage
      )

      return
    }


    const config =
      getJobFormConfig(
        createForm.analysisType
      )


    if (!config) {

      setCommandMessage(
        '지원하지 않는 분석 유형입니다.'
      )

      return
    }


    const versionSuffix =
      createForm
        .versionSuffix
        .trim()


    const outputSuffix =
      createForm
        .outputSuffix
        .trim()


    const requiresUpstreamOutput =
      config.inputParts.some(
        part =>
          part.type === 'dynamic'
      )


    if (!versionSuffix) {

      setCommandMessage(
        'Analysis Version의 변경 가능한 부분을 입력해주세요.'
      )

      return
    }


    if (
      !isValidPathSuffix(
        versionSuffix
      )
    ) {

      setCommandMessage(
        'Version에는 영문, 숫자, -, _, . 만 사용할 수 있습니다.'
      )

      return
    }


    if (!outputSuffix) {

      setCommandMessage(
        'Output 이름의 변경 가능한 부분을 입력해주세요.'
      )

      return
    }


    if (
      !isValidPathSuffix(
        outputSuffix
      )
    ) {

      setCommandMessage(
        'Output 이름에는 영문, 숫자, -, _, . 만 사용할 수 있습니다.'
      )

      return
    }


    /*
     * ACTIVITY_RESULT,
     * STUDENT_LEARNING_SUMMARY는
     * 이전 Student Activity 결과가 필요하다.
     */
    if (
      requiresUpstreamOutput
    ) {

      const upstreamSuffix =
        createForm
          .upstreamOutputSuffix
          .trim()


      if (!upstreamSuffix) {

        setCommandMessage(
          '사용할 Student Activity 결과 이름을 입력해주세요.'
        )

        return
      }


      if (
        !isValidPathSuffix(
          upstreamSuffix
        )
      ) {

        setCommandMessage(
          '이전 분석 결과 이름에는 영문, 숫자, -, _, . 만 사용할 수 있습니다.'
        )

        return
      }
    }


    const analysisVersion =
      config.versionPrefix
      +
      versionSuffix


    /*
     * Oracle ANALYSIS_VERSION은
     * VARCHAR2(30).
     */
    if (
      analysisVersion.length > 30
    ) {

      setCommandMessage(
        '완성된 Analysis Version은 30자를 초과할 수 없습니다.'
      )

      return
    }


    const hdfsInputPath =
      buildHdfsInputPath(
        config,
        createForm
      )


    const hdfsOutputPath =
      buildHdfsOutputPath(
        config,
        createForm
      )


    try {

      setCommandLoading(
        true
      )

      setCommandMessage(
        ''
      )


      const result =
        await createAnalysisJob({

          datasetId,

          analysisType:
            createForm.analysisType,

          analysisVersion,

          hdfsInputPath,

          hdfsOutputPath
        })


      setCommandMessage(
        `Job #${result.jobId}이 PENDING 상태로 생성되었습니다.`
      )


      /*
       * 현재 분석 유형은 유지하고
       * 실행마다 달라지는 값만 비운다.
       */
      setCreateForm({

        analysisType:
          createForm.analysisType,

        versionSuffix:
          '',

        outputSuffix:
          '',

        upstreamOutputSuffix:
          ''
      })


      setCreateOpen(
        false
      )


      setRefreshKey(
        previous =>
          previous + 1
      )

    } catch (err) {

      setCommandMessage(
        err.message
      )

    } finally {

      setCommandLoading(
        false
      )
    }
  }


  /**
   * FAILED Job 재실행 요청.
   *
   * 기존 FAILED Job은 수정하지 않고
   * 새로운 PENDING Job을 생성한다.
   */
  async function handleRetryJob() {

    if (!canManageAnalysisJobs) {

      setCommandMessage(
        analysisCommandRestrictionMessage
      )

      return
    }


    if (
      !selectedJob
      ||
      selectedJob.status !== 'FAILED'
    ) {

      return
    }


    if (
      !retryOutputPath.trim()
    ) {

      setCommandMessage(
        '새 HDFS Output 경로를 입력해주세요.'
      )

      return
    }


    const confirmed =
      window.confirm(
        `FAILED Job #${selectedJob.jobId}을 재실행 요청하시겠습니까?\n`
        +
        '기존 실패 이력은 유지되고 새로운 PENDING Job이 생성됩니다.'
      )


    if (!confirmed) {

      return
    }


    try {

      setCommandLoading(
        true
      )

      setCommandMessage(
        ''
      )


      const result =
        await retryAnalysisJob(
          selectedJob.jobId,
          retryOutputPath.trim()
        )


      setCommandMessage(
        `기존 Job #${selectedJob.jobId}을 기준으로 `
        +
        `새 Job #${result.jobId}이 생성되었습니다.`
      )


      setRetryOutputPath(
        ''
      )

      setSelectedJob(
        null
      )


      setRefreshKey(
        previous =>
          previous + 1
      )

    } catch (err) {

      setCommandMessage(
        err.message
      )

    } finally {

      setCommandLoading(
        false
      )
    }
  }


  /**
   * PENDING Job 실제 Hadoop 실행.
   *
   * PENDING
   * → RUNNING
   * → SSH
   * → Ubuntu
   * → Hadoop MapReduce
   * → SUCCESS / FAILED
   */
  async function handleExecuteJob() {

    if (!canManageAnalysisJobs) {

      setCommandMessage(
        analysisCommandRestrictionMessage
      )

      return
    }


    if (
      !selectedJob
      ||
      selectedJob.status !== 'PENDING'
    ) {

      return
    }


    const confirmed =
      window.confirm(
        `Job #${selectedJob.jobId}을 실제 실행하시겠습니까?\n`
        +
        'VMware Ubuntu의 Hadoop MapReduce가 실행됩니다.'
      )


    if (!confirmed) {

      return
    }


    try {

      setCommandLoading(
        true
      )

      setCommandMessage(
        ''
      )


      const result =
        await executeAnalysisJob(
          selectedJob.jobId
        )


      setCommandMessage(
        `Job #${selectedJob.jobId} 실제 실행을 시작했습니다. `
        +
        `현재 상태: ${result?.status ?? 'RUNNING'}`
      )


      /*
       * 선택 객체에는 이전 PENDING 상태가 있으므로
       * 닫고 DB 최신 상태를 다시 조회한다.
       */
      setSelectedJob(
        null
      )


      setRefreshKey(
        previous =>
          previous + 1
      )

    } catch (err) {

      setCommandMessage(
        err.message
      )

    } finally {

      setCommandLoading(
        false
      )
    }
  }


  if (loading) {

    return (
      <main className="job-page">

        <div className="job-loading">
          Analysis Job을 불러오는 중입니다.
        </div>

      </main>
    )
  }


  return (
    <main className="job-page">

      {/* =========================
          Header
          ========================= */}
      <header className="job-header">

        <div>

          <p>
            Hadoop / Spring Batch 분석 실행 이력
          </p>

          <h1>
            Analysis Job
          </h1>

          <span>
            분석 실행 이력과 처리 결과를 조회하고,
            신규 분석 요청과 실제 Hadoop 실행 및
            실패 Job 재실행 요청을 관리합니다.
          </span>

        </div>

      </header>


      {/* =========================
          Error / Command Message
          ========================= */}
      {error && (

        <div className="page-error">
          {error}
        </div>

      )}


      {commandMessage && (

        <div className="job-command-message">
          {commandMessage}
        </div>

      )}


      {!canManageAnalysisJobs && (

        <div
          role="alert"
          aria-live="polite"
          style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '12px',
            padding: '14px 16px',
            marginBottom: '16px',
            border: '1px solid #f3c26b',
            borderLeft: '5px solid #f59e0b',
            borderRadius: '12px',
            background: '#fff8e6',
            boxShadow:
              '0 4px 10px rgba(245, 158, 11, 0.08)'
          }}
        >

          <div
            aria-hidden="true"
            style={{
              flexShrink: 0,
              width: '28px',
              height: '28px',
              borderRadius: '999px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: '#fef3c7',
              color: '#d97706',
              fontSize: '16px',
              fontWeight: 800
            }}
          >
            !
          </div>


          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '4px'
            }}
          >

            <strong
              style={{
                fontSize: '14px',
                lineHeight: 1.4,
                color: '#92400e'
              }}
            >
              제한된 기능 안내
            </strong>


            <span
              style={{
                fontSize: '13px',
                lineHeight: 1.55,
                color: '#78350f'
              }}
            >
              {analysisCommandRestrictionMessage}
            </span>

          </div>

        </div>

      )}


      {/* =========================
          Dataset 선택
          ========================= */}
      <section className="job-dataset-toolbar">

        <div>

          <label>
            Dataset ID
          </label>

          <input
            type="number"
            min="1"
            value={
              datasetIdInput
            }
            onChange={
              event =>
                setDatasetIdInput(
                  event.target.value
                )
            }
          />

        </div>


        <button
          type="button"
          onClick={
            handleDatasetSearch
          }
        >
          Dataset 조회
        </button>


        <div className="job-current-dataset">

          현재 조회 Dataset

          <strong>
            #{datasetId}
          </strong>

        </div>


        <button
          type="button"
          className="job-create-open-button"
          disabled={!canManageAnalysisJobs}
          title={
            !canManageAnalysisJobs
              ? analysisCommandRestrictionMessage
              : undefined
          }
          onClick={() => {

            setCreateOpen(
              previous =>
                !previous
            )

            setCommandMessage(
              ''
            )
          }}
        >

          {
            createOpen
              ? '요청 Form 닫기'
              : '신규 Job 요청'
          }

        </button>

      </section>


      {/* =========================
          신규 Job 요청
          ========================= */}
      {createOpen && canManageAnalysisJobs && (

        <section className="job-create-panel">

          <div className="job-panel-header">

            <h2>
              신규 Analysis Job 요청
            </h2>

            <p>
              고정 경로는 자동 적용됩니다.
              변경 가능한 부분만 입력하면
              PENDING Job이 생성됩니다.
            </p>

          </div>


          <div className="job-create-grid">

            {/* 분석 유형 */}
            <label>

              분석 유형

              <select
                name="analysisType"
                value={
                  createForm.analysisType
                }
                onChange={
                  changeCreateField
                }
              >

                {ANALYSIS_TYPES.map(
                  type => (

                    <option
                      key={type}
                      value={type}
                    >
                      {type}
                    </option>

                  )
                )}

              </select>

            </label>


            {/* Analysis Version */}
            <label>

              Analysis Version

              <div className="job-fixed-input">

                <span>
                  {
                    getJobFormConfig(
                      createForm.analysisType
                    ).versionPrefix
                  }
                </span>


                <input
                  type="text"
                  name="versionSuffix"
                  value={
                    createForm.versionSuffix
                  }
                  placeholder="예: v2"
                  onChange={
                    changeCreateField
                  }
                />

              </div>

            </label>


            {/* HDFS Input */}
            <div className="job-create-full">

              <div className="job-field-title">
                HDFS Input Path
              </div>


              <div className="job-path-list">

                {
                  getJobFormConfig(
                    createForm.analysisType
                  )
                    .inputParts
                    .map(
                      (
                        part,
                        index
                      ) => (

                        <div
                          key={
                            `${part.type}-${index}`
                          }
                          className="job-path-row"
                        >

                          {
                            part.type === 'fixed'
                              ? (
                                <>

                                  <span className="job-lock">
                                    🔒
                                  </span>

                                  <code>
                                    {part.value}
                                  </code>

                                </>
                              )
                              : (
                                <>

                                  <span className="job-path-label">
                                    {part.label}
                                  </span>


                                  <div className="job-fixed-input job-fixed-input-full">

                                    <span>
                                      {part.prefix}
                                    </span>

                                    <input
                                      type="text"
                                      name="upstreamOutputSuffix"
                                      value={
                                        createForm
                                          .upstreamOutputSuffix
                                      }
                                      placeholder="예: v1"
                                      onChange={
                                        changeCreateField
                                      }
                                    />

                                  </div>

                                </>
                              )
                          }

                        </div>

                      )
                    )
                }

              </div>


              <small className="job-field-help">
                고정된 Input 경로는 분석 유형에 따라 자동 설정됩니다.
              </small>

            </div>


            {/* HDFS Output */}
            <div className="job-create-full">

              <div className="job-field-title">
                HDFS Output Path
              </div>


              <div className="job-output-suffix">

                <label>

                  실행별 Output 이름

                  <input
                    type="text"
                    name="outputSuffix"
                    value={
                      createForm.outputSuffix
                    }
                    placeholder={
                      createForm.analysisType
                      === 'DATA_QUALITY'
                        ? '예: rules-v3'
                        : '예: job-16'
                    }
                    onChange={
                      changeCreateField
                    }
                  />

                </label>

              </div>


              <div className="job-path-list">

                {
                  getJobFormConfig(
                    createForm.analysisType
                  )
                    .outputPrefixes
                    .map(
                      (
                        prefix,
                        index
                      ) => (

                        <div
                          key={prefix}
                          className="job-path-preview"
                        >

                          <span>
                            {
                              createForm.analysisType
                              === 'DATA_QUALITY'
                                ? (
                                  index === 0
                                    ? 'Row'
                                    : 'Duplicate'
                                )
                                : 'Output'
                            }
                          </span>


                          <code>

                            {prefix}

                            {
                              createForm.outputSuffix
                              ||
                              '...'
                            }

                          </code>

                        </div>

                      )
                    )
                }

              </div>


              <small className="job-field-help">

                {
                  createForm.analysisType
                  === 'DATA_QUALITY'
                    ? (
                      <>

                        두 Output 경로는 Backend 전달 시

                        <strong>
                          {' ; '}
                        </strong>

                        로 자동 연결됩니다.

                      </>
                    )
                    : (
                      <>
                        고정 경로 뒤의 이름만 입력하면 됩니다.
                      </>
                    )
                }

              </small>

            </div>

          </div>


          <div className="job-command-actions">

            <button
              type="button"
              className="secondary"
              disabled={
                commandLoading
              }
              onClick={() => {

                setCreateOpen(
                  false
                )

                setCommandMessage(
                  ''
                )
              }}
            >
              취소
            </button>


            <button
              type="button"
              disabled={
                commandLoading
                ||
                !canManageAnalysisJobs
              }
              onClick={
                handleCreateJob
              }
            >

              {
                commandLoading
                  ? '요청 중...'
                  : 'PENDING Job 생성'
              }

            </button>

          </div>

        </section>

      )}


      {/* =========================
          KPI
          ========================= */}
      {data && (

        <section className="job-kpi-grid">

          <JobKpi
            title="전체 Job"
            value={
              data.summary.totalJobCount
            }
          />


          <JobKpi
            title="SUCCESS"
            value={
              data.summary.successCount
            }
          />


          <JobKpi
            title="FAILED"
            value={
              data.summary.failedCount
            }
          />


          <JobKpi
            title="결과 적재"
            value={
              data.summary.importedCount
            }
          />

        </section>

      )}


      {/* =========================
          Filter
          ========================= */}
      {data && (

        <section className="job-filter">

          <div>

            <label>
              분석 유형
            </label>

            <select
              value={
                selectedType
              }
              onChange={
                event =>
                  setSelectedType(
                    event.target.value
                  )
              }
            >

              <option value="ALL">
                전체
              </option>


              {analysisTypes.map(
                type => (

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
              상태
            </label>

            <select
              value={
                selectedStatus
              }
              onChange={
                event =>
                  setSelectedStatus(
                    event.target.value
                  )
              }
            >

              <option value="ALL">
                전체
              </option>

              <option value="PENDING">
                PENDING
              </option>

              <option value="RUNNING">
                RUNNING
              </option>

              <option value="SUCCESS">
                SUCCESS
              </option>

              <option value="FAILED">
                FAILED
              </option>

            </select>

          </div>

        </section>

      )}


      {/* =========================
          Job 목록
          ========================= */}
      {data && (

        <section className="job-panel">

          <div className="job-panel-header">

            <h2>
              Job 실행 이력
            </h2>

            <p>
              행을 클릭하면 Job 상세정보를 확인할 수 있습니다.
            </p>

          </div>


          <div className="job-table-wrapper">

            <table>

              <thead>

                <tr>

                  <th>
                    Job
                  </th>

                  <th>
                    분석 유형
                  </th>

                  <th>
                    상태
                  </th>

                  <th>
                    Version
                  </th>

                  <th>
                    Output
                  </th>

                  <th>
                    Duplicate
                  </th>

                  <th>
                    Oracle
                  </th>

                </tr>

              </thead>


              <tbody>

                {
                  filteredJobs.length === 0
                    ? (

                      <tr>

                        <td
                          colSpan="7"
                          className="job-empty"
                        >
                          조회된 Analysis Job이 없습니다.
                        </td>

                      </tr>

                    )
                    : filteredJobs.map(
                      job => (

                        <tr
                          key={
                            job.jobId
                          }
                          onClick={() => {

                            setSelectedJob(
                              job
                            )

                            setRetryOutputPath(
                              ''
                            )

                            setCommandMessage(
                              ''
                            )
                          }}
                        >

                          <td>
                            #{job.jobId}
                          </td>


                          <td>
                            {job.analysisType}
                          </td>


                          <td>

                            <span
                              className={
                                'job-status '
                                +
                                job.status
                                  .toLowerCase()
                              }
                            >
                              {job.status}
                            </span>

                          </td>


                          <td>
                            {job.analysisVersion}
                          </td>


                          <td>

                            {
                              formatNumber(
                                job.outputRecordCount
                              )
                            }

                          </td>


                          <td>

                            {
                              formatNumber(
                                job.duplicateRecordCount
                              )
                            }

                          </td>


                          <td>
                            {job.resultImportedYn}
                          </td>

                        </tr>

                      )
                    )
                }

              </tbody>

            </table>

          </div>

        </section>

      )}


      {/* =========================
          선택 Job 상세
          ========================= */}
      {selectedJob && (

        <section className="job-detail-panel">

          <div className="job-detail-header">

            <div>

              <span>
                ANALYSIS JOB
              </span>

              <h2>

                #{selectedJob.jobId}

                {' '}

                {selectedJob.analysisType}

              </h2>

            </div>


            <button
              type="button"
              onClick={() => {

                setSelectedJob(
                  null
                )

                setRetryOutputPath(
                  ''
                )
              }}
            >
              닫기
            </button>

          </div>


          <div className="job-detail-grid">

            <JobDetail
              label="Status"
              value={
                selectedJob.status
              }
            />


            <JobDetail
              label="Analysis Version"
              value={
                selectedJob.analysisVersion
              }
            />


            <JobDetail
              label="Input Record"
              value={
                formatNumber(
                  selectedJob.inputRecordCount
                )
              }
            />


            <JobDetail
              label="Output Record"
              value={
                formatNumber(
                  selectedJob.outputRecordCount
                )
              }
            />


            <JobDetail
              label="Valid"
              value={
                formatNumber(
                  selectedJob.validRecordCount
                )
              }
            />


            <JobDetail
              label="Invalid"
              value={
                formatNumber(
                  selectedJob.invalidRecordCount
                )
              }
            />


            <JobDetail
              label="Duplicate"
              value={
                formatNumber(
                  selectedJob.duplicateRecordCount
                )
              }
            />


            <JobDetail
              label="Processing Time"
              value={
                selectedJob.processingTimeMs == null
                  ? '-'
                  : selectedJob.processingTimeMs
                    + ' ms'
              }
            />


            <JobDetail
              label="Requested"
              value={
                formatDateTime(
                  selectedJob.requestedAt
                )
              }
            />


            <JobDetail
              label="Started"
              value={
                formatDateTime(
                  selectedJob.startedAt
                )
              }
            />


            <JobDetail
              label="Finished"
              value={
                formatDateTime(
                  selectedJob.finishedAt
                )
              }
            />


            <JobDetail
              label="Imported"
              value={
                selectedJob.resultImportedYn
              }
            />

          </div>


          <JobLongDetail
            label="HDFS Input"
            value={
              selectedJob.hdfsInputPath
            }
          />


          <JobLongDetail
            label="HDFS Output"
            value={
              selectedJob.hdfsOutputPath
            }
          />


          <JobLongDetail
            label="Result File"
            value={
              selectedJob.resultFilePath
            }
          />


          {/* 실제 실패 정보 */}
          {
            (
              selectedJob.errorStep
              ||
              selectedJob.errorMessage
            )
            && (

              <div className="job-error-box">

                <strong>
                  오류 정보
                </strong>

                <p>

                  Step:

                  {' '}

                  {
                    selectedJob.errorStep
                    ?? '-'
                  }

                </p>

                <p>

                  {
                    selectedJob.errorMessage
                    ?? '-'
                  }

                </p>

              </div>

            )
          }


          {/* =========================
              PENDING 실제 실행
              ========================= */}
          {
            selectedJob.status === 'PENDING'
            && (

              <div className="job-execute-panel">

                <h3>
                  Hadoop 실제 실행
                </h3>

                <p>
                  현재 Job의 분석 유형과 HDFS 경로를 사용하여
                  VMware Ubuntu에서 실제 MapReduce를 실행합니다.
                </p>

                <button
                  type="button"
                  disabled={
                    commandLoading
                    ||
                    !canManageAnalysisJobs
                  }
                  title={
                    !canManageAnalysisJobs
                      ? analysisCommandRestrictionMessage
                      : undefined
                  }
                  onClick={
                    handleExecuteJob
                  }
                >

                  {
                    commandLoading
                      ? '실행 요청 중...'
                      : '실제 실행'
                  }

                </button>

              </div>

            )
          }


          {/* =========================
              FAILED 재실행 요청
              ========================= */}
          {
            selectedJob.status === 'FAILED'
            && (

              <div className="job-retry-panel">

                <h3>
                  FAILED Job 재실행 요청
                </h3>

                <p>
                  기존 실패 이력은 수정하지 않고
                  새로운 PENDING Job을 생성합니다.
                </p>


                <input
                  type="text"
                  disabled={!canManageAnalysisJobs}
                  value={
                    retryOutputPath
                  }
                  placeholder="새 실제 HDFS Output 경로"
                  onChange={
                    event =>
                      setRetryOutputPath(
                        event.target.value
                      )
                  }
                />


                <button
                  type="button"
                  disabled={
                    commandLoading
                    ||
                    !canManageAnalysisJobs
                    ||
                    !retryOutputPath.trim()
                  }
                  title={
                    !canManageAnalysisJobs
                      ? analysisCommandRestrictionMessage
                      : undefined
                  }
                  onClick={
                    handleRetryJob
                  }
                >

                  {
                    commandLoading
                      ? '요청 중...'
                      : '재실행 요청'
                  }

                </button>

              </div>

            )
          }

        </section>

      )}

    </main>
  )
}


/**
 * KPI Card.
 */
function JobKpi({
  title,
  value
}) {

  return (
    <article className="job-kpi">

      <span>
        {title}
      </span>

      <strong>
        {formatNumber(value)}
      </strong>

    </article>
  )
}


/**
 * 상세 단일 값.
 */
function JobDetail({
  label,
  value
}) {

  return (
    <div className="job-detail-item">

      <span>
        {label}
      </span>

      <strong>
        {value ?? '-'}
      </strong>

    </div>
  )
}


/**
 * HDFS Path 등 긴 값.
 */
function JobLongDetail({
  label,
  value
}) {

  return (
    <div className="job-long-detail">

      <span>
        {label}
      </span>

      <code>
        {value ?? '-'}
      </code>

    </div>
  )
}


export default AnalysisJobPage