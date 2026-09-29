import {
  Link,
  useSearchParams
} from 'react-router-dom'

/**
 * 아직 실제 구현 전인 분석 화면의
 * 임시 Route.
 *
 * 기능을 구현한 것으로 처리하지 않고,
 * 선택한 강의 ID만 유지한다.
 */
function PlannedAnalysisPage({
  title,
  description
}) {

  const [searchParams] =
    useSearchParams()

  const courseId =
    searchParams.get('courseId')

  const backUrl =
    courseId
      ? '/courses?courseId='
        + courseId
      : '/courses'

  return (
    <main className="page-state">

      <h1>
        {title}
      </h1>

      <p>
        {description}
      </p>

      {courseId && (
        <p>
          선택된 강의 ID:
          {' '}
          {courseId}
        </p>
      )}

      <p>
        이 화면은 다음 구현 단계에서
        기존 EduScope DB/API를 사용하여
        실제 분석 기능으로 연결합니다.
      </p>

      <Link to={backUrl}>
        강의 분석으로 돌아가기
      </Link>

    </main>
  )
}

export default PlannedAnalysisPage