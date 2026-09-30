import {
  useEffect,
  useState
} from 'react'
import {
  Link,
  useNavigate
} from 'react-router-dom'
import {
  login
} from '../api/authApi'

import {
  enterSnapshotMode,
  isSnapshotAvailable
} from '../snapshot/snapshotMode'
import '../styles/auth.css'
import '../styles/login.css'
import '../styles/cleanLogin.css'
/**
 * 초 단위를 MM:SS 형식으로 변환한다.
 *
 * 예:
 * 300초 -> 05:00
 * 59초  -> 00:59
 */
function formatRemainingTime(
  seconds
) {
  const safeSeconds =
    Math.max(
      0,
      seconds
    )
  const minutes =
    Math.floor(
      safeSeconds / 60
    )
  const remainSeconds =
    safeSeconds % 60
  return (
    String(minutes)
      .padStart(
        2,
        '0'
      )
    +
    ':'
    +
    String(remainSeconds)
      .padStart(
        2,
        '0'
      )
  )
}
/**
 * EduScope 로그인 화면.
 *
 * 유지 기능:
 * - Spring Security 로그인
 * - CSRF
 * - Session 인증
 * - 로그인 실패 처리
 * - HTTP 429 로그인 제한
 * - 5분 Countdown
 * - 회원가입 이동
 *
 * Visual:
 * - 외부 이미지를 사용하지 않는다.
 * - 실제 데이터를 사용하지 않는다.
 * - CSS + SVG 기반 추상 UI 요소만 사용한다.
 */
function LoginPage() {
  const navigate =
    useNavigate()
  const [
    loginId,
    setLoginId
  ] = useState('')
  const [
    password,
    setPassword
  ] = useState('')
  const [
    error,
    setError
  ] = useState('')
  const [
    loading,
    setLoading
  ] = useState(false)

  const [
    snapshotAvailable,
    setSnapshotAvailable
  ] = useState(false)
  /**
   * 실제 Snapshot 파일 존재 여부 확인.
   *
   * snapshot.json이 배포된 경우에만
   * 읽기 전용 진입 버튼을 표시한다.
   */
  useEffect(() => {

    let mounted = true

    async function checkSnapshot() {

      const available =
        await isSnapshotAvailable()

      if (mounted) {

        setSnapshotAvailable(
          available
        )
      }
    }

    checkSnapshot()

    return () => {
      mounted = false
    }

  }, [])


  /**
   * 반복 로그인 실패 제한 Modal 표시 여부.
   */
  const [
    securityAlertOpen,
    setSecurityAlertOpen
  ] = useState(false)
  /**
   * 로그인 제한 해제까지 남은 시간.
   */
  const [
    remainingSeconds,
    setRemainingSeconds
  ] = useState(0)
  /**
   * 로그인 제한 Countdown.
   *
   * Modal이 열려 있는 동안
   * 1초마다 남은 시간을 감소시킨다.
   */
  useEffect(() => {
    if (
      !securityAlertOpen
    ) {
      return undefined
    }
    const timer =
      window.setInterval(
        () => {
          setRemainingSeconds(
            previous => {
              if (
                previous <= 1
              ) {
                return 0
              }
              return previous - 1
            }
          )
        },
        1000
      )
    return () => {
      window.clearInterval(
        timer
      )
    }
  }, [
    securityAlertOpen
  ])
  /**
   * Spring Security 로그인 요청.
   */
  async function handleLogin() {
    if (
      !loginId.trim()
      ||
      !password
    ) {
      setError(
        '로그인 ID와 비밀번호를 입력해주세요.'
      )
      return
    }
    try {
      setLoading(
        true
      )
      setError(
        ''
      )
      /**
       * authApi.js를 통해
       * Spring Security 로그인 요청.
       */
      await login(
        loginId.trim(),
        password
      )
      /**
       * 로그인 성공 후
       * Dashboard 이동.
       */
      navigate(
        '/',
        {
          replace: true
        }
      )
    } catch (
      loginError
    ) {
      /**
       * Render Free 공개 Demo 서버 Cold Start.
       *
       * authApi.js에서 502 / 503 / 504 재시도가 모두 실패한 경우
       * 실제 계정 오류와 구분해서 안내한다.
       */
      if (
        loginError?.code
        === 'DEMO_SERVER_WAKING'
      ) {
        setError(
          '공개 데모 서버가 시작 중입니다. 잠시 후 다시 로그인해주세요.'
        )
        /**
         * 보안상 비밀번호는 제거한다.
         * 로그인 ID는 유지한다.
         */
        setPassword(
          ''
        )
        return
      }
      /**
       * HTTP 429
       *
       * 반복 로그인 실패로 인한
       * 일시적 로그인 제한.
       */
      if (
        loginError?.status === 429
      ) {
        setError(
          ''
        )
        /**
         * Backend에서 전달한 실제 남은 시간을 사용한다.
         * 값이 없는 예외 상황에서는 기존 정책인 300초를 사용한다.
         */
        setRemainingSeconds(
          loginError
            .retryAfterSeconds
          ||
          300
        )
        /**
         * 보안상 비밀번호는 제거한다.
         * 로그인 ID는 유지한다.
         */
        setPassword(
          ''
        )
        setSecurityAlertOpen(
          true
        )
        return
      }
      /**
       * 일반 로그인 실패.
       *
       * 비밀번호 오류 / LOCKED / DISABLED 등의 상세 원인은
       * 화면에서 구분해서 노출하지 않는다.
       */
      setError(
        '아이디, 비밀번호 또는 계정 상태를 확인해주세요.'
      )
    } finally {
      setLoading(
        false
      )
    }
  }
  /**
   * Oracle/Render가 없어도 사용할 수 있는
   * 읽기 전용 Snapshot 데모 진입.
   */
  function handleSnapshotDemo() {

    enterSnapshotMode()

    navigate(
      '/',
      {
        replace: true
      }
    )
  }


  /**
   * Enter Key 로그인.
   */
  function handleKeyDown(
    event
  ) {
    if (
      event.key === 'Enter'
    ) {
      event.preventDefault()
      if (
        !securityAlertOpen
      ) {
        handleLogin()
      }
    }
  }
  /**
   * 로그인 제한 시간이 끝난 후
   * Modal을 닫는다.
   */
  function closeSecurityAlert() {
    if (
      remainingSeconds > 0
    ) {
      return
    }
    setSecurityAlertOpen(
      false
    )
    setPassword(
      ''
    )
  }
  return (
    <main className="clean-login-page">
      {/* =====================================================
          Background
          ===================================================== */}
      <div className="clean-login-grid" />
      <div
        className="
          clean-login-orb
          clean-login-orb-one
        "
      />
      <div
        className="
          clean-login-orb
          clean-login-orb-two
        "
      />
      <div
        className="
          clean-login-orb
          clean-login-orb-three
        "
      />
      {/* =====================================================
          Login Shell
          ===================================================== */}
      <section className="clean-login-shell">
        {/* ===================================================
            Left Visual Area
            =================================================== */}
        <div className="clean-login-visual">
          {/* Brand */}
          <div className="clean-login-brand">
            <div className="clean-login-brand-mark">
              E
            </div>
            <div>
              <strong>
                EduScope
              </strong>
              <span>
                학습 데이터 분석 플랫폼
              </span>
            </div>
          </div>
          {/* Main Copy */}
          <div className="clean-login-copy">
            <span className="clean-login-eyebrow">
              DATA · ANALYSIS · EXPERIENCE
            </span>
            <h1>
              학습 데이터를
              <span>
                더 명확하게.
              </span>
            </h1>
            <p>
              OULAD 학습 데이터를 기반으로
              분석 결과와 학습 흐름을
              한 화면에서 깔끔하게 확인할 수 있습니다.
            </p>
          </div>
          {/* =================================================
              Abstract UI Visual
              실제 데이터가 아닌
              장식용 추상 UI 구조다.
              ================================================= */}
          <div
            className="clean-ui-stage"
            aria-hidden="true"
          >
            {/* Background Glow */}
            <div
              className="
                clean-abstract-glow
                clean-abstract-glow-one
              "
            />
            <div
              className="
                clean-abstract-glow
                clean-abstract-glow-two
              "
            />
            {/* ===============================================
                Main Dashboard Window
                =============================================== */}
            <div className="clean-abstract-window">
              {/* Browser Header */}
              <div className="clean-abstract-window-header">
                <div className="clean-window-dots">
                  <span />
                  <span />
                  <span />
                </div>
                <div className="clean-window-address" />
              </div>
              {/* Window Body */}
              <div className="clean-abstract-window-body">
                {/* Mini Sidebar */}
                <div className="clean-abstract-sidebar">
                  <div className="clean-nav-logo" />
                  <div className="clean-nav-item active">
                    <span />
                    <i />
                  </div>
                  <div className="clean-nav-item">
                    <span />
                    <i />
                  </div>
                  <div className="clean-nav-item">
                    <span />
                    <i />
                  </div>
                  <div className="clean-nav-item">
                    <span />
                    <i />
                  </div>
                </div>
                {/* ===========================================
                    Dashboard Main Content
                    =========================================== */}
                <div className="clean-abstract-dashboard">
                  {/* Summary Cards */}
                  <div className="clean-abstract-summary-row">
                    <div className="clean-summary-card">
                      <span
                        className="
                          clean-summary-icon
                          lavender
                        "
                      />
                      <div>
                        <i />
                        <strong />
                      </div>
                    </div>
                    <div className="clean-summary-card">
                      <span
                        className="
                          clean-summary-icon
                          blue
                        "
                      />
                      <div>
                        <i />
                        <strong />
                      </div>
                    </div>
                    <div className="clean-summary-card">
                      <span
                        className="
                          clean-summary-icon
                          pink
                        "
                      />
                      <div>
                        <i />
                        <strong />
                      </div>
                    </div>
                  </div>
                  {/* Main Content */}
                  <div className="clean-abstract-content-row">
                    {/* =======================================
                        Code / Data Panel
                        ======================================= */}
                    <div className="clean-code-panel">
                      <div className="clean-panel-title" />
                      <div
                        className="
                          clean-code-line
                          line-1
                        "
                      >
                        <span />
                        <i />
                      </div>
                      <div
                        className="
                          clean-code-line
                          line-2
                        "
                      >
                        <span />
                        <i />
                      </div>
                      <div
                        className="
                          clean-code-line
                          line-3
                        "
                      >
                        <span />
                        <i />
                      </div>
                      <div
                        className="
                          clean-code-line
                          line-4
                        "
                      >
                        <span />
                        <i />
                      </div>
                      <div
                        className="
                          clean-code-line
                          line-5
                        "
                      >
                        <span />
                        <i />
                      </div>
                      <div
                        className="
                          clean-code-line
                          line-6
                        "
                      >
                        <span />
                        <i />
                      </div>
                    </div>
                    {/* =======================================
                        Chart Column
                        ======================================= */}
                    <div className="clean-chart-column">
                      {/* Line Chart */}
                      <div className="clean-line-chart">
                        <div className="clean-chart-grid" />
                        <svg
                          viewBox="0 0 220 95"
                          preserveAspectRatio="none"
                        >
                          {/* Area */}
                          <path
                            className="clean-chart-area"
                            d="
                              M 5 75
                              C 35 75,
                                45 50,
                                70 57
                              S 108 29,
                                132 43
                              S 168 58,
                                190 27
                              S 210 20,
                                216 16
                              L 216 90
                              L 5 90
                              Z
                            "
                          />
                          {/* Line */}
                          <path
                            className="clean-chart-path"
                            d="
                              M 5 75
                              C 35 75,
                                45 50,
                                70 57
                              S 108 29,
                                132 43
                              S 168 58,
                                190 27
                              S 210 20,
                                216 16
                            "
                          />
                        </svg>
                      </div>
                      {/* Donut Chart */}
                      <div className="clean-donut-card">
                        <div className="clean-donut-chart" />
                        <div className="clean-donut-list">
                          <span />
                          <span />
                          <span />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            {/* ===============================================
                Floating Bar Chart
                =============================================== */}
            <div
              className="
                clean-floating-card
                clean-floating-bars
              "
            >
              <div className="clean-floating-title" />
              <div className="clean-bar-chart">
                <span />
                <span />
                <span />
                <span />
                <span />
              </div>
            </div>
            {/* ===============================================
                Floating Graph
                =============================================== */}
            <div
              className="
                clean-floating-card
                clean-floating-graph
              "
            >
              <div
                className="
                  clean-floating-title
                  short
                "
              />
              <svg
                viewBox="0 0 150 70"
                preserveAspectRatio="none"
              >
                <path
                  className="clean-floating-area"
                  d="
                    M 0 57
                    C 25 57,
                      30 39,
                      50 43
                    S 78 28,
                      93 35
                    S 120 12,
                      145 15
                    L 145 70
                    L 0 70
                    Z
                  "
                />
                <path
                  className="clean-floating-path"
                  d="
                    M 0 57
                    C 25 57,
                      30 39,
                      50 43
                    S 78 28,
                      93 35
                    S 120 12,
                      145 15
                  "
                />
              </svg>
            </div>
            {/* ===============================================
                Floating Status List
                =============================================== */}
            <div
              className="
                clean-floating-card
                clean-floating-list
              "
            >
              <div className="clean-list-row">
                <span
                  className="
                    clean-list-color
                    color-blue
                  "
                />
                <i />
              </div>
              <div className="clean-list-row">
                <span
                  className="
                    clean-list-color
                    color-purple
                  "
                />
                <i />
              </div>
              <div className="clean-list-row">
                <span
                  className="
                    clean-list-color
                    color-pink
                  "
                />
                <i />
              </div>
            </div>
            {/* ===============================================
                Decorative Orbit
                =============================================== */}
            <div
              className="
                clean-orbit-line
                orbit-one
              "
            >
              <span />
            </div>
            <div
              className="
                clean-orbit-line
                orbit-two
              "
            >
              <span />
            </div>
            {/* ===============================================
                Decorative Sphere
                =============================================== */}
            <div
              className="
                clean-visual-sphere
                sphere-one
              "
            />
            <div
              className="
                clean-visual-sphere
                sphere-two
              "
            />
            <div
              className="
                clean-visual-sphere
                sphere-three
              "
            />
          </div>
          {/* Technology / Security Footer */}
          <div className="clean-login-visual-footer">
            <span>
              Spring Security Session
            </span>
            <span>
              RBAC
            </span>
            <span>
              OULAD
            </span>
          </div>
        </div>
        {/* ===================================================
            Right Login Area
            =================================================== */}
        <div className="clean-login-panel">
          <div className="clean-login-card">
            {/* Login Title */}
            <div className="clean-login-card-heading">
              <span className="clean-login-card-label">
                EDU SCOPE ACCESS
              </span>
              <h2>
                로그인
              </h2>
              <p>
                승인된 EduScope 계정으로 접속해주세요.
              </p>
            </div>
            {/* =================================================
                Login Form
                ================================================= */}
            <div className="clean-login-form">
              {/* Login ID */}
              <label>
                <span>
                  로그인 ID
                </span>
                <div className="clean-login-input">
                  <div className="clean-login-input-symbol">
                    ID
                  </div>
                  <input
                    type="text"
                    value={loginId}
                    onChange={
                      event =>
                        setLoginId(
                          event.target.value
                        )
                    }
                    onKeyDown={
                      handleKeyDown
                    }
                    placeholder="로그인 ID를 입력하세요"
                    autoComplete="username"
                    disabled={
                      loading
                      ||
                      securityAlertOpen
                    }
                  />
                </div>
              </label>
              {/* Password */}
              <label>
                <span>
                  비밀번호
                </span>
                <div className="clean-login-input">
                  <div className="clean-login-input-symbol">
                    PW
                  </div>
                  <input
                    type="password"
                    value={password}
                    onChange={
                      event =>
                        setPassword(
                          event.target.value
                        )
                    }
                    onKeyDown={
                      handleKeyDown
                    }
                    placeholder="비밀번호를 입력하세요"
                    autoComplete="current-password"
                    disabled={
                      loading
                      ||
                      securityAlertOpen
                    }
                  />
                </div>
              </label>
              {/* Error */}
              {error && (
                <div className="clean-login-error">
                  <span>
                    !
                  </span>
                  <p>
                    {error}
                  </p>
                </div>
              )}
              {/* Login Button */}
              <button
                className="clean-login-submit"
                type="button"
                onClick={
                  handleLogin
                }
                disabled={
                  loading
                  ||
                  securityAlertOpen
                }
              >
                {
                  loading
                    ? (
                      <span className="clean-login-loading">
                        <span className="clean-login-spinner" />
                        로그인 확인 중...
                      </span>
                    )
                    : 'EduScope 로그인'
                }
              </button>
            </div>
            {/* Divider */}
            <div className="clean-login-divider">
              <span />
              <p>
                계정 안내
              </p>
              <span />
            </div>
            {/* Signup */}
            <div className="clean-login-signup">
              <span>
                아직 계정이 없나요?
              </span>
              <Link to="/signup">
                회원가입
              </Link>
            </div>

            {snapshotAvailable && (
              <div className="clean-login-signup">
                <span>
                  서버 없이 저장된 분석 결과만 확인하려면
                </span>
                <button
                  type="button"
                  className="snapshot-demo-button"
                  onClick={handleSnapshotDemo}
                >
                  저장된 분석 결과 보기
                </button>
              </div>
            )}
            {/* Security Status */}
            <div className="clean-login-security">
              <span className="clean-login-security-dot" />
              <span>
                보안 세션 기반 인증이 적용되어 있습니다.
              </span>
            </div>
          </div>
        </div>
      </section>
      {/* =====================================================
          Login Rate Limit Modal
          ===================================================== */}
      {securityAlertOpen && (
        <div className="clean-login-security-backdrop">
          <section
            className="clean-login-security-modal"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="security-alert-title"
          >
            <div className="clean-security-symbol">
              !
            </div>
            <span className="clean-security-label">
              LOGIN SECURITY
            </span>
            <h2
              id="security-alert-title"
            >
              로그인 시도 제한
            </h2>
            {
              remainingSeconds > 0
                ? (
                  <>
                    <p>
                      로그인 실패가 반복되어
                      일시적으로 로그인이 제한되었습니다.
                    </p>
                    <div className="clean-security-timer">
                      <span>
                        다시 로그인 가능
                      </span>
                      <strong>
                        {
                          formatRemainingTime(
                            remainingSeconds
                          )
                        }
                      </strong>
                    </div>
                    <p className="clean-security-help">
                      위 시간이 지나면
                      다시 로그인할 수 있습니다.
                    </p>
                  </>
                )
                : (
                  <>
                    <p>
                      로그인 제한 시간이 종료되었습니다.
                    </p>
                    <div
                      className="
                        clean-security-timer
                        ready
                      "
                    >
                      <strong>
                        00:00
                      </strong>
                    </div>
                    <p className="clean-security-help">
                      다시 로그인할 수 있습니다.
                    </p>
                  </>
                )
            }
            <button
              type="button"
              disabled={
                remainingSeconds > 0
              }
              onClick={
                closeSecurityAlert
              }
            >
              {
                remainingSeconds > 0
                  ? '잠시 기다려주세요'
                  : '다시 로그인'
              }
            </button>
          </section>
        </div>
      )}
    </main>
  )
}
export default LoginPage
