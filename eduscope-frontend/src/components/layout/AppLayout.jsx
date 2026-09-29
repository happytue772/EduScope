import {
  Outlet,
  useLocation
} from 'react-router-dom'

import Sidebar from './Sidebar'
import LogoutButton from '../auth/LogoutButton'

import '../../styles/layout.css'


/**
 * URL별 Header 제목.
 * 기존 Route는 변경하지 않고 표시 이름만 관리한다.
 */
const PAGE_TITLES = {
  '/': '대시보드',
  '/courses': '강의 분석',
  '/courses/compare': '강의 비교',
  '/assessments': '평가 분석',
  '/activities': '학습활동 분석',
  '/registrations': '수강 분석',
  '/results': '성과 비교',
  '/students': '학생 분석',
  '/datasets': '데이터셋',
  '/data-quality': '데이터 품질',
  '/analysis-jobs': '분석 작업',
  '/admin/users': '사용자 관리',
  '/admin/audit-logs': '감사 로그',
  '/admin/datasets': '데이터셋 관리',
  '/admin/audit': '감사 로그',

  '/admin/system-health': '시스템 상태'
}


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


function normalizeRoles(user) {

  const roles =
    user?.roles ?? []


  return roles.length > 0
    ? roles
    : ['VIEWER']
}


/**
 * EduScope 공통 Layout.
 *
 * ProtectedLayout에서 이미 검증한 user를 그대로 재사용한다.
 * /api/auth/me 중복 호출은 발생하지 않는다.
 */
function AppLayout({
  user
}) {

  const location =
    useLocation()


  const pageTitle =
    PAGE_TITLES[location.pathname]
    ?? 'EduScope'


  const roles =
    normalizeRoles(user)


  return (
    <div className="app-layout">

      <Sidebar user={user} />

      <div className="app-main">

        <header className="top-header">

          <div className="header-heading">

            <span className="header-eyebrow">
              OULAD 학습 데이터 분석 플랫폼
            </span>

            <div className="header-title">
              {pageTitle}
            </div>

          </div>


          <div className="header-user">

            <div className="header-user-avatar">

              {
                user?.displayName
                  ?.trim()
                  ?.charAt(0)
                  ?.toUpperCase()
                || 'U'
              }

            </div>


            <div className="header-user-info">

              <strong className="header-user-name">
                {user?.displayName ?? '사용자'}
              </strong>

              <div className="header-role-badges">

                {
                  roles.map(
                    role => {

                      const meta =
                        ROLE_META[role]
                        ?? ROLE_META.VIEWER

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


            <LogoutButton />

          </div>

        </header>


        <div className="app-content-shell">
          <Outlet context={{ user }} />
        </div>

      </div>

    </div>
  )
}


export default AppLayout
