import {
  useEffect,
  useState
} from 'react'

import AdminAuditPage
  from './pages/AdminAuditPage'
  
  import AdminDatasetPage
    from './pages/AdminDatasetPage'
	
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
  useLocation
} from 'react-router-dom'


/* =========================
 * 공통 Layout
 * ========================= */

import AppLayout
  from './components/layout/AppLayout'


/* =========================
 * 인증 API
 * ========================= */

import {
  getCurrentUser
} from './api/authApi'

import {
  isSnapshotMode,
  SNAPSHOT_USER
} from './snapshot/snapshotMode'


/* =========================
 * 인증 화면
 * ========================= */

import LoginPage
  from './pages/LoginPage'

import SignupPage
  from './pages/SignupPage'


/* =========================
 * Overview
 * ========================= */

import DashboardPage
  from './pages/DashboardPage'


/* =========================
 * 분석 화면
 * ========================= */

import CourseAnalysisPage
  from './pages/CourseAnalysisPage'

import CourseComparePage
  from './pages/CourseComparePage'

import AssessmentAnalysisPage
  from './pages/AssessmentAnalysisPage'

import ActivityAnalysisPage
  from './pages/ActivityAnalysisPage'

import RegistrationAnalysisPage
  from './pages/RegistrationAnalysisPage'

import ResultAnalysisPage
  from './pages/ResultAnalysisPage'

import StudentAnalysisPage
  from './pages/StudentAnalysisPage'


/* =========================
 * 데이터 / 운영
 * ========================= */

import DatasetPage
  from './pages/DatasetPage'

import DataQualityPage
  from './pages/DataQualityPage'

import AnalysisJobPage
  from './pages/AnalysisJobPage'
  
  import SystemHealthPage
    from './pages/SystemHealthPage'


/* =========================
 * ADMIN
 * ========================= */

import AdminUserPage
  from './pages/AdminUserPage'


/* =========================
 * 오류 페이지
 * ========================= */

import NotFoundPage
  from './pages/NotFoundPage'


/* 전체 Premium 디자인 시스템 */
import './styles/premium.css'
import './styles/cleanTheme.css'

/**
 * 로그인 여부를 확인한 뒤
 * EduScope 내부 Layout으로 진입시킨다.
 *
 * 로그인 X
 * → /login
 *
 * 로그인 O
 * → AppLayout
 */
function ProtectedLayout() {

  const location =
    useLocation()


  const [
    user,
    setUser
  ] = useState(null)


  const [
    loading,
    setLoading
  ] = useState(true)


  useEffect(() => {

    let mounted = true


    async function loadCurrentUser() {

      /*
       * Oracle DB가 없어도 동작하는
       * 읽기 전용 Snapshot 모드.
       */
      if (
        isSnapshotMode()
      ) {

        if (mounted) {

          setUser(
            SNAPSHOT_USER
          )
        }

        setLoading(false)

        return
      }


      try {

        /*
         * Spring Security Session 기반
         * 현재 로그인 사용자 조회.
         */
        const currentUser =
          await getCurrentUser()


        if (mounted) {

          setUser(
            currentUser
          )
        }

      } catch {

        /*
         * 인증되지 않은 경우.
         *
         * 임의 사용자 데이터는 생성하지 않는다.
         */
        if (mounted) {

          setUser(null)
        }

      } finally {

        if (mounted) {

          setLoading(false)
        }
      }
    }


    loadCurrentUser()


    return () => {

      mounted = false
    }

  }, [])


  /*
   * 사용자 확인 전에는
   * 로그인 화면으로 즉시 이동하지 않는다.
   */
  if (loading) {

    return (
      <div className="app-loading">
        EduScope 사용자 정보를 확인하는 중입니다.
      </div>
    )
  }


  /*
   * 로그인 Session이 없으면
   * 로그인 화면으로 이동.
   */
  if (!user) {

    return (
      <Navigate
        to="/login"
        replace
        state={{
          from: location.pathname
        }}
      />
    )
  }


  /*
   * 로그인 사용자 정보를
   * AppLayout → Sidebar로 전달.
   */
  return (
    <AppLayout
      user={user}
    />
  )
}


/**
 * EduScope Frontend Route.
 */
function App() {

  return (
    <BrowserRouter>

      <Routes>


        {/* ==================================
            로그인 없이 접근 가능
            ================================== */}

        <Route
          path="/login"
          element={
            <LoginPage />
          }
        />


        <Route
          path="/signup"
          element={
            <SignupPage />
          }
        />
		
        {/* ==================================
            로그인 후 접근
            ================================== */}

        <Route
          element={
            <ProtectedLayout />
          }
        >

          <Route
            path="/admin/system-health"
            element={
              <SystemHealthPage />
            }
          />
		
		<Route
		  path="/admin/datasets"
		  element={
		    <AdminDatasetPage />
		  }
		/>
		

          {/* ===============================
              Overview
              =============================== */}

          <Route
            path="/"
            element={
              <DashboardPage />
            }
          />


          {/* ===============================
              일반 분석
              VIEWER / ANALYST / ADMIN / DEMO_ADMIN
              =============================== */}

          <Route
            path="/courses"
            element={
              <CourseAnalysisPage />
            }
          />


          <Route
            path="/courses/compare"
            element={
              <CourseComparePage />
            }
          />


          <Route
            path="/assessments"
            element={
              <AssessmentAnalysisPage />
            }
          />


          <Route
            path="/activities"
            element={
              <ActivityAnalysisPage />
            }
          />


          <Route
            path="/registrations"
            element={
              <RegistrationAnalysisPage />
            }
          />


          <Route
            path="/results"
            element={
              <ResultAnalysisPage />
            }
          />


          {/* ===============================
              ANALYST / ADMIN / DEMO_ADMIN 조회 영역
              
              Sidebar에서는 VIEWER에게
              잠금 상태로 표시한다.
              
              실제 API 권한은
              Spring Security가 검증한다.
              =============================== */}

          <Route
            path="/students"
            element={
              <StudentAnalysisPage />
            }
          />
		  <Route
		  		  path="/admin/audit-logs"
		  		  element={
		  		    <AdminAuditPage />
		  		  }
		  		/>


          <Route
            path="/datasets"
            element={
              <DatasetPage />
            }
          />


          <Route
            path="/data-quality"
            element={
              <DataQualityPage />
            }
          />


          <Route
            path="/analysis-jobs"
            element={
              <AnalysisJobPage />
            }
          />


          {/* ===============================
              ADMIN / DEMO_ADMIN 조회 영역
              =============================== */}

          <Route
            path="/admin/users"
            element={
              <AdminUserPage />
            }
          />


        </Route>


        {/* ==================================
            존재하지 않는 주소
            ================================== */}

        <Route
          path="*"
          element={
            <NotFoundPage />
          }
        />


      </Routes>

    </BrowserRouter>
  )
}


export default App