import {
  useState
} from 'react'

import {
  useNavigate
} from 'react-router-dom'

import {
  logout
} from '../../api/authApi'

import {
  isSnapshotMode,
  leaveSnapshotMode
} from '../../snapshot/snapshotMode'


/**
 * EduScope 로그아웃 버튼.
 */
function LogoutButton() {

  const navigate =
    useNavigate()


  const [
    loading,
    setLoading
  ] = useState(false)


  async function handleLogout() {

    try {

      setLoading(true)

      /*
       * Snapshot 모드는 Backend Session을 사용하지 않는다.
       */
      if (
        isSnapshotMode()
      ) {

        leaveSnapshotMode()

        navigate(
          '/login',
          {
            replace: true
          }
        )

        return
      }

      await logout()


      /*
       * Session 제거 후
       * React 로그인 화면으로 이동.
       */
      navigate(
        '/login',
        {
          replace: true
        }
      )

    } finally {

      setLoading(false)
    }
  }


  return (
    <button
      type="button"
      className="logout-button"
      disabled={loading}
      onClick={handleLogout}
    >
      {
        loading
          ? '로그아웃 중...'
          : '로그아웃'
      }
    </button>
  )
}


export default LogoutButton