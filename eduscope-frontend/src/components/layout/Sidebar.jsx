import {
  NavLink
} from 'react-router-dom'


const ROLE_META = {

  ADMIN: {
    label: '관리자',
    className: 'app-role-badge app-role-admin'
  },

  DEMO_ADMIN: {
    label: '데모 관리자',
    className: 'app-role-badge app-role-admin'
  },

  ANALYST: {
    label: '분석가',
    className: 'app-role-badge app-role-analyst'
  },

  VIEWER: {
    label: '조회',
    className: 'app-role-badge app-role-viewer'
  }

}


/**
 * 사용자 Role 정규화.
 *
 * Role 정보가 없는 경우
 * VIEWER를 기본값으로 사용한다.
 */
function normalizeRoles(user) {

  const roles =
    user?.roles ?? []


  return roles.length > 0
    ? roles
    : ['VIEWER']
}


/**
 * EduScope RBAC Sidebar.
 *
 * 권한 없는 메뉴도 숨기지 않고
 * 잠금 상태로 표시한다.
 *
 * 실제 API 인가는
 * Spring Security가 담당한다.
 */
function Sidebar({
  user
}) {

  const roles =
    normalizeRoles(user)


  const isAdmin =
    roles.includes(
      'ADMIN'
    )


  const isDemoAdmin =
    roles.includes(
      'DEMO_ADMIN'
    )


  /**
   * 분석 화면 조회 권한.
   * ADMIN / DEMO_ADMIN은 ANALYST 조회 화면도 확인할 수 있다.
   */
  const canViewAnalysis =
    roles.includes(
      'ANALYST'
    )
    ||
    isAdmin
    ||
    isDemoAdmin


  /**
   * 관리자 화면 조회 권한.
   * 실제 변경 권한은 Backend Spring Security가 최종 검증한다.
   */
  const canViewAdmin =
    isAdmin
    ||
    isDemoAdmin


  return (

    <aside className="sidebar">

      {/* ================================
          Brand
          ================================ */}

      <div className="sidebar-brand">

        <div className="sidebar-brand-mark">
          E
        </div>


        <div>

          <div className="sidebar-logo">
            EduScope
          </div>


          <div className="sidebar-brand-subtitle">
            학습 데이터 분석
          </div>

        </div>

      </div>


      {/* ================================
          Session Status
          ================================ */}

      <div className="sidebar-status">

        <span className="sidebar-status-dot" />


        <span>
          보안 세션 연결됨
        </span>

      </div>


      {/* ================================
          Navigation
          ================================ */}

      <nav>

        {/* ==============================
            개요
            ============================== */}

        <SidebarSection
          label="개요"
          tone="overview"
        />


        <NavItem
          to="/"
          symbol="01"
          label="대시보드"
        />


        {/* ==============================
            분석
            ============================== */}

        <SidebarSection
          label="분석"
          tone="analysis"
        />


        <NavItem
          to="/courses"
          symbol="02"
          label="강의 분석"
        />


        <NavItem
          to="/assessments"
          symbol="03"
          label="평가 분석"
        />


        <NavItem
          to="/activities"
          symbol="04"
          label="학습활동"
        />


        <NavItem
          to="/registrations"
          symbol="05"
          label="수강 분석"
        />


        <NavItem
          to="/results"
          symbol="06"
          label="성과 비교"
        />


        {/* ==============================
            분석가
            ============================== */}

        <SidebarSection
          label="분석가"
          tone="analyst"
        />


        <LockedNavLink
          enabled={canViewAnalysis}
          to="/students"
          symbol="07"
          label="학생 분석"
        />


        <LockedNavLink
          enabled={canViewAnalysis}
          to="/datasets"
          symbol="08"
          label="데이터셋"
        />


        <LockedNavLink
          enabled={canViewAnalysis}
          to="/data-quality"
          symbol="09"
          label="데이터 품질"
        />


        <LockedNavLink
          enabled={canViewAnalysis}
          to="/analysis-jobs"
          symbol="10"
          label="분석 작업"
        />


        {/* ==============================
            관리자
            ============================== */}

        <SidebarSection
          label="관리자"
          tone="admin"
        />


        <LockedNavLink
          enabled={canViewAdmin}
          to="/admin/users"
          symbol="11"
          label="사용자 관리"
        />


        <LockedNavLink
          enabled={canViewAdmin}
          to="/admin/audit-logs"
          symbol="12"
          label="감사 로그"
        />


        <LockedNavLink
          enabled={canViewAdmin}
          to="/admin/datasets"
          symbol="13"
          label="데이터셋 관리"
        />


        {/*
         * 신규:
         * 실제 Backend System Health API를 사용하는
         * ADMIN / DEMO_ADMIN 시스템 상태 조회 화면.
         */}
        <LockedNavLink
          enabled={canViewAdmin}
          to="/admin/system-health"
          symbol="14"
          label="시스템 상태"
        />

      </nav>


      {/* ================================
          Current Role
          ================================ */}

      <div className="sidebar-footer">

        <div className="sidebar-footer-label">
          현재 권한
        </div>


        <div className="sidebar-role-list">

          {
            roles.map(
              role => {

                const meta =
                  ROLE_META[role]
                  ??
                  ROLE_META.VIEWER


                return (

                  <span
                    key={role}
                    className={meta.className}
                  >

                    <span className="app-role-code">
                      {role}
                    </span>


                    <span className="app-role-label">
                      {meta.label}
                    </span>

                  </span>
                )
              }
            )
          }

        </div>

      </div>

    </aside>
  )
}


/**
 * Sidebar 영역 제목.
 */
function SidebarSection({
  label,
  tone
}) {

  return (

    <div
      className={
        `sidebar-section-title sidebar-section-${tone}`
      }
    >

      <span className="sidebar-section-line" />


      <span>
        {label}
      </span>

    </div>
  )
}


/**
 * 일반 Navigation Item.
 */
function NavItem({
  to,
  symbol,
  label
}) {

  return (

    <NavLink
      to={to}
      className={({
        isActive
      }) =>
        isActive
          ? 'sidebar-nav-item active'
          : 'sidebar-nav-item'
      }
    >

      <span className="sidebar-nav-symbol">
        {symbol}
      </span>


      <span className="sidebar-nav-label">
        {label}
      </span>

    </NavLink>
  )
}


/**
 * 권한이 없는 메뉴도 숨기지 않는다.
 *
 * enabled = true
 * → 실제 Navigation
 *
 * enabled = false
 * → 잠금 상태 표시
 */
function LockedNavLink({
  enabled,
  to,
  symbol,
  label
}) {

  if (!enabled) {

    return (

      <div
        className="sidebar-link locked sidebar-nav-item"
        title="접근 권한이 없습니다."
      >

        <span className="sidebar-nav-symbol">
          {symbol}
        </span>


        <span className="sidebar-nav-label">
          {label}
        </span>


        <span className="lock-icon">
          잠금
        </span>

      </div>
    )
  }


  return (

    <NavItem
      to={to}
      symbol={symbol}
      label={label}
    />
  )
}


export default Sidebar