import {
  useEffect,
  useMemo,
  useState
} from 'react'

import {
  getAdminUsers,
  updateAdminUserAccess
} from '../api/adminUserApi'

import '../styles/adminUser.css'


/**
 * 날짜/시간 표시.
 */
function formatDateTime(value) {

  if (!value) {
    return '-'
  }

  return new Date(value)
    .toLocaleString()
}


/**
 * ADMIN 전용 사용자 / RBAC 관리 화면.
 *
 * 기능:
 * 1. 사용자 목록 조회
 * 2. 사용자 검색 / 상태 Filter
 * 3. 계정 상태 변경
 * 4. VIEWER / ANALYST / ADMIN / DEMO_ADMIN Role 관리
 */
function AdminUserPage() {

  const [
    users,
    setUsers
  ] = useState([])


  const [
    keyword,
    setKeyword
  ] = useState('')


  const [
    status,
    setStatus
  ] = useState('ALL')


  const [
    loading,
    setLoading
  ] = useState(true)


  const [
    error,
    setError
  ] = useState(null)


  /*
   * 현재 관리 중인 사용자.
   */
  const [
    selectedUser,
    setSelectedUser
  ] = useState(null)


  const [
    editStatus,
    setEditStatus
  ] = useState('DISABLED')


  const [
    editRoles,
    setEditRoles
  ] = useState([])


  const [
    saving,
    setSaving
  ] = useState(false)


  const [
    saveMessage,
    setSaveMessage
  ] = useState('')


  /**
   * 최초 사용자 목록 조회.
   *
   * useEffect 실행 직후 setLoading(true)를
   * 다시 호출하지 않는다.
   *
   * loading 초기값이 이미 true이므로
   * API 응답 이후에만 State를 변경한다.
   */
  useEffect(() => {

    let cancelled = false


    async function loadInitialUsers() {

      try {

        const result =
          await getAdminUsers()


        /*
         * Component가 이미 unmount된 경우
         * State를 변경하지 않는다.
         */
        if (cancelled) {
          return
        }


        setUsers(result)

        setError(null)

      } catch (err) {

        if (cancelled) {
          return
        }


        setError(
          err.message
        )

      } finally {

        if (!cancelled) {

          setLoading(false)
        }
      }
    }


    loadInitialUsers()


    /*
     * Component 종료 후 비동기 응답이
     * 도착하는 경우 State 변경 방지.
     */
    return () => {

      cancelled = true
    }

  }, [])


  /**
   * 사용자 정보 변경 이후
   * Oracle DB의 최신 목록을 다시 조회한다.
   *
   * 초기 useEffect용 조회와 분리한다.
   */
  async function refreshUsers() {

    const result =
      await getAdminUsers()


    setUsers(result)

    setError(null)


    /*
     * saveAccess()에서
     * 실제 DB 최신 사용자 정보를
     * 다시 사용할 수 있도록 반환한다.
     */
    return result
  }


  /**
   * 검색 / 상태 Filter.
   */
  const filteredUsers =
    useMemo(() => {

      const normalizedKeyword =
        keyword
          .trim()
          .toLowerCase()


      return users.filter(
        user => {

          const keywordMatch =
            normalizedKeyword.length === 0
            ||
            user.loginId
              ?.toLowerCase()
              .includes(
                normalizedKeyword
              )
            ||
            user.displayName
              ?.toLowerCase()
              .includes(
                normalizedKeyword
              )


          const statusMatch =
            status === 'ALL'
            ||
            user.accountStatus
              === status


          return (
            keywordMatch
            &&
            statusMatch
          )
        }
      )

    }, [
      users,
      keyword,
      status
    ])


  /**
   * 사용자 관리 Panel 열기.
   */
  function openUserEditor(user) {

    setSelectedUser(user)


    setEditStatus(
      user.accountStatus
    )


    setEditRoles(
      user.roles
        ? [...user.roles]
        : []
    )


    setSaveMessage('')
  }


  /**
   * 사용자 관리 Panel 닫기.
   */
  function closeUserEditor() {

    setSelectedUser(null)

    setSaveMessage('')
  }


  /**
   * Role 선택 / 해제.
   */
  function toggleRole(role) {

    setEditRoles(
      previous => {

        if (
          previous.includes(role)
        ) {

          return previous.filter(
            value =>
              value !== role
          )
        }


        return [
          ...previous,
          role
        ]
      }
    )
  }


  /**
   * 계정 상태 + Role 저장.
   */
  async function saveAccess() {

    if (!selectedUser) {
      return
    }


    /*
     * ACTIVE 상태는 실제 로그인 가능한
     * 사용자이므로 최소 하나의 Role 필요.
     */
    if (
      editStatus === 'ACTIVE'
      &&
      editRoles.length === 0
    ) {

      setSaveMessage(
        'ACTIVE 사용자는 최소 하나의 Role이 필요합니다.'
      )

      return
    }


    try {

      setSaving(true)

      setSaveMessage('')


      /*
       * Backend:
       *
       * PATCH
       * /api/admin/users/{userId}/access
       */
      const result =
        await updateAdminUserAccess(
          selectedUser.userId,
          editStatus,
          editRoles
        )


      /*
       * 화면 값을 임의로 확정하지 않고
       * 변경 후 실제 DB 데이터를 다시 조회한다.
       */
      const latestUsers =
        await refreshUsers()


      const latestSelectedUser =
        latestUsers.find(
          user =>
            user.userId
            === selectedUser.userId
        )


      /*
       * 실제 DB에서 다시 조회된 사용자로
       * 관리 Panel도 갱신한다.
       */
      if (latestSelectedUser) {

        setSelectedUser(
          latestSelectedUser
        )


        setEditStatus(
          latestSelectedUser
            .accountStatus
        )


        setEditRoles(
          latestSelectedUser.roles
            ? [
                ...latestSelectedUser.roles
              ]
            : []
        )
      }


      setSaveMessage(
        result?.message
        ?? '사용자 권한이 변경되었습니다.'
      )

    } catch (err) {

      setSaveMessage(
        err.message
      )

    } finally {

      setSaving(false)
    }
  }


  /**
   * 최초 사용자 조회 중.
   */
  if (
    loading
    &&
    users.length === 0
  ) {

    return (
      <main className="admin-user-page">

        사용자 정보를 불러오는 중입니다.

      </main>
    )
  }


  return (
    <main className="admin-user-page">


      {/* =========================
          Header
          ========================= */}

      <header className="admin-user-header">

        <div>

          <p>
            Security / RBAC
          </p>


          <h1>
            사용자 관리
          </h1>


          <span>
            EduScope 로그인 계정의 승인 상태와
            Role을 관리합니다.
          </span>

        </div>

      </header>


      {/* =========================
          오류
          ========================= */}

      {error && (

        <div className="admin-user-error">

          {error}

        </div>

      )}


      {/* =========================
          KPI
          ========================= */}

      <section className="admin-user-summary">


        <article>

          <span>
            전체 사용자
          </span>

          <strong>
            {users.length}
          </strong>

        </article>


        <article>

          <span>
            ACTIVE
          </span>

          <strong>
            {
              users.filter(
                user =>
                  user.accountStatus
                  === 'ACTIVE'
              ).length
            }
          </strong>

        </article>


        <article>

          <span>
            LOCKED
          </span>

          <strong>
            {
              users.filter(
                user =>
                  user.accountStatus
                  === 'LOCKED'
              ).length
            }
          </strong>

        </article>


        <article>

          <span>
            DISABLED
          </span>

          <strong>
            {
              users.filter(
                user =>
                  user.accountStatus
                  === 'DISABLED'
              ).length
            }
          </strong>

        </article>


      </section>


      {/* =========================
          검색 / Filter
          ========================= */}

      <section className="admin-user-filter">


        <input
          type="text"
          value={keyword}
          placeholder="로그인 ID 또는 표시 이름"
          onChange={
            event =>
              setKeyword(
                event.target.value
              )
          }
        />


        <select
          value={status}
          onChange={
            event =>
              setStatus(
                event.target.value
              )
          }
        >

          <option value="ALL">
            전체 상태
          </option>


          <option value="ACTIVE">
            ACTIVE
          </option>


          <option value="LOCKED">
            LOCKED
          </option>


          <option value="DISABLED">
            DISABLED
          </option>

        </select>


      </section>


      {/* =========================
          사용자 목록
          ========================= */}

      <section className="admin-user-panel">

        <div className="admin-user-table-wrapper">

          <table>


            <thead>

              <tr>

                <th>
                  ID
                </th>

                <th>
                  Login ID
                </th>

                <th>
                  표시 이름
                </th>

                <th>
                  Role
                </th>

                <th>
                  상태
                </th>

                <th>
                  최근 로그인
                </th>

                <th>
                  생성일
                </th>

                <th>
                  관리
                </th>

              </tr>

            </thead>


            <tbody>

              {filteredUsers.map(
                user => (

                  <tr
                    key={user.userId}
                  >


                    <td>
                      {user.userId}
                    </td>


                    <td>
                      {user.loginId}
                    </td>


                    <td>
                      {user.displayName}
                    </td>


                    <td>

                      <div className="admin-user-roles">

                        {
                          user.roles
                            ?.length > 0
                          ? user.roles.map(
                              role => (

                                <span
                                  key={role}
                                  className={
                                    'admin-role '
                                    + (
                                      role === 'DEMO_ADMIN'
                                        ? 'admin'
                                        : role.toLowerCase()
                                    )
                                  }
                                >
                                  {role}
                                </span>

                              )
                            )
                          : (

                              <span className="admin-no-role">
                                미지정
                              </span>

                            )
                        }

                      </div>

                    </td>


                    <td>

                      <span
                        className={
                          'admin-status '
                          + user
                            .accountStatus
                            .toLowerCase()
                        }
                      >
                        {
                          user.accountStatus
                        }
                      </span>

                    </td>


                    <td>

                      {
                        formatDateTime(
                          user.lastLoginAt
                        )
                      }

                    </td>


                    <td>

                      {
                        formatDateTime(
                          user.createdAt
                        )
                      }

                    </td>


                    <td>

                      <button
                        type="button"
                        className="admin-manage-button"
                        onClick={() =>
                          openUserEditor(
                            user
                          )
                        }
                      >
                        관리
                      </button>

                    </td>


                  </tr>

                )
              )}

            </tbody>


          </table>

        </div>

      </section>


      {/* =========================
          사용자 승인 / Role 관리
          ========================= */}

      {selectedUser && (

        <section className="admin-access-panel">


          <div className="admin-access-header">

            <div>

              <span>
                USER ACCESS
              </span>


              <h2>
                {
                  selectedUser.displayName
                }
              </h2>


              <p>

                {
                  selectedUser.loginId
                }

                {' · '}

                USER #

                {
                  selectedUser.userId
                }

              </p>

            </div>


            <button
              type="button"
              onClick={
                closeUserEditor
              }
            >
              닫기
            </button>

          </div>


          <div className="admin-access-grid">


            {/* =====================
                계정 상태
                ===================== */}

            <div className="admin-access-block">

              <label>
                계정 상태
              </label>


              <select
                value={editStatus}
                onChange={
                  event =>
                    setEditStatus(
                      event.target.value
                    )
                }
              >

                <option value="ACTIVE">
                  ACTIVE
                </option>


                <option value="LOCKED">
                  LOCKED
                </option>


                <option value="DISABLED">
                  DISABLED
                </option>

              </select>


              <p>
                ACTIVE 상태의 사용자만
                로그인할 수 있습니다.
              </p>

            </div>


            {/* =====================
                Role
                ===================== */}

            <div className="admin-access-block">

              <label>
                Role
              </label>


              <div className="admin-role-options">

                {[
                  'VIEWER',
                  'ANALYST',
                  'ADMIN',
                  'DEMO_ADMIN'
                ].map(
                  role => (

                    <label
                      key={role}
                      className="admin-role-option"
                    >

                      <input
                        type="checkbox"
                        checked={
                          editRoles.includes(
                            role
                          )
                        }
                        onChange={() =>
                          toggleRole(
                            role
                          )
                        }
                      />


                      <span>
                        {role}
                      </span>

                    </label>

                  )
                )}

              </div>


              <p>
                ACTIVE 사용자는 최소 하나의
                Role이 필요합니다.
              </p>

            </div>


          </div>


          {/* =========================
              저장 결과 메시지
              ========================= */}

          {saveMessage && (

            <div className="admin-save-message">

              {saveMessage}

            </div>

          )}


          {/* =========================
              저장
              ========================= */}

          <div className="admin-access-actions">

            <button
              type="button"
              disabled={saving}
              onClick={
                saveAccess
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

      )}


    </main>
  )
}


export default AdminUserPage