import {
  useState
} from 'react'

import {
  Link,
  useNavigate
} from 'react-router-dom'

import {
  signup
} from '../api/authApi'

import '../styles/auth.css'


function SignupPage() {

  const navigate =
    useNavigate()


  const [
    form,
    setForm
  ] = useState({
    loginId: '',
    password: '',
    passwordConfirm: '',
    displayName: ''
  })


  const [
    error,
    setError
  ] = useState('')


  const [
    message,
    setMessage
  ] = useState('')


  const [
    loading,
    setLoading
  ] = useState(false)


  function changeField(
    event
  ) {

    const {
      name,
      value
    } = event.target


    setForm(
      previous => ({
        ...previous,
        [name]: value
      })
    )
  }


  async function submit(
    event
  ) {

    event.preventDefault()

    try {

      setLoading(true)
      setError('')


      const result =
        await signup(form)


      setMessage(
        result.message
      )


      /*
       * 가입 성공 후 잠시 뒤
       * 로그인 화면으로 이동.
       */
      setTimeout(
        () => {
          navigate('/login')
        },
        1200
      )

    } catch (err) {

      setError(
        err.message
      )

    } finally {

      setLoading(false)
    }
  }


  return (
    <main className="auth-page">

      <section className="auth-card">

        <div className="auth-brand">
          EduScope
        </div>

        <h1>
          회원가입
        </h1>

        <p className="auth-description">
          가입 후 관리자의 승인과
          역할 부여가 필요합니다.
        </p>


        <form
          onSubmit={submit}
          className="auth-form"
        >

          <label>
            로그인 ID

            <input
              name="loginId"
              value={form.loginId}
              onChange={changeField}
              minLength={4}
              maxLength={100}
              required
            />
          </label>


          <label>
            표시 이름

            <input
              name="displayName"
              value={form.displayName}
              onChange={changeField}
              maxLength={100}
              required
            />
          </label>


          <label>
            비밀번호

            <input
              type="password"
              name="password"
              value={form.password}
              onChange={changeField}
              minLength={8}
              required
            />
          </label>


          <label>
            비밀번호 확인

            <input
              type="password"
              name="passwordConfirm"
              value={
                form.passwordConfirm
              }
              onChange={changeField}
              minLength={8}
              required
            />
          </label>


          {error && (
            <div className="auth-error">
              {error}
            </div>
          )}


          {message && (
            <div className="auth-success">
              {message}
            </div>
          )}


          <button
            type="submit"
            disabled={loading}
          >
            {
              loading
                ? '가입 중...'
                : '회원가입'
            }
          </button>

        </form>


        <div className="auth-link">

          이미 계정이 있나요?

          {' '}

          <Link to="/login">
            로그인
          </Link>

        </div>

      </section>

    </main>
  )
}


export default SignupPage