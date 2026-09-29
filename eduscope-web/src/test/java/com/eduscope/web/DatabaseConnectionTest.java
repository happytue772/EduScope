package com.eduscope.web;

import com.eduscope.web.audit.repository.AuditLogRepository;
import com.eduscope.web.user.repository.AppRoleRepository;
import com.eduscope.web.user.repository.AppUserRepository;
import com.eduscope.web.user.repository.AppUserRoleRepository;
import com.eduscope.web.analysis.repository.DataQualityStatRepository;
import com.eduscope.web.dataset.repository.DatasetFileRepository;
import com.eduscope.web.analysis.repository.StudentLearningSummaryStatRepository;
import com.eduscope.web.analysis.repository.CourseResultStatRepository;
import com.eduscope.web.analysis.repository.ActivityResultStatRepository;
import com.eduscope.web.analysis.repository.CourseResultStatRepository;
import com.eduscope.web.analysis.repository.RegistrationStatRepository;
import com.eduscope.web.analysis.repository.AssessmentStatRepository;
import com.eduscope.web.analysis.repository.VleActivityStatRepository;
import com.eduscope.web.analysis.repository.CourseActivityStatRepository;
import com.eduscope.web.analysis.repository.CourseWeeklyActivityStatRepository;
import com.eduscope.web.analysis.repository.StudentActivityStatRepository;
import com.eduscope.web.analysis.repository.AnalysisJobRepository;
import com.eduscope.web.vle.repository.VleMaterialRepository;
import com.eduscope.web.vle.repository.VleMaterialRepository;
import com.eduscope.web.assessment.repository.StudentAssessmentRepository;
import com.eduscope.web.assessment.repository.AssessmentRepository;
import com.eduscope.web.student.repository.StudentRegistrationRepository;
import com.eduscope.web.student.repository.StudentRegistrationRepository;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.eduscope.web.course.repository.CoursePresentationRepository;
import com.eduscope.web.dataset.repository.DatasetRepository;
import com.eduscope.web.student.repository.OuladStudentRepository;
/**
 * Spring Boot → JPA → Oracle 연결 테스트.
 */

@SpringBootTest
@Tag("oracle")
class DatabaseConnectionTest {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private CoursePresentationRepository coursePresentationRepository;

    /**
     * DATASET 테이블 연결 테스트.
     */
    @Test
    void oracleConnectionTest() {

        // Oracle DATASET 테이블에 실제 COUNT 쿼리 실행
        long count = datasetRepository.count();

        assertTrue(count >= 0);
    }
    
    @Test
    void studentRegistrationConnectionTest() {

        // Oracle STUDENT_REGISTRATION 실제 조회
        long count =
            studentRegistrationRepository.count();

        assertTrue(count > 0);
    }

    /**
     * COURSE_PRESENTATION 테이블 연결 테스트.
     */
    @Test
    void coursePresentationConnectionTest() {

        // Oracle COURSE_PRESENTATION 테이블에 실제 COUNT 쿼리 실행
        long count = coursePresentationRepository.count();

        assertTrue(count >= 0);
    }
    
    @Test
    void assessmentConnectionTest() {

        // Oracle ASSESSMENT 테이블 실제 조회
        long count =
            assessmentRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void studentAssessmentConnectionTest() {

        // Oracle STUDENT_ASSESSMENT 실제 조회
        long count =
            studentAssessmentRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void vleMaterialConnectionTest() {

        // Oracle VLE_MATERIAL 실제 데이터 조회
        long count =
            vleMaterialRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void analysisJobConnectionTest() {

        // ANALYSIS_JOB 실제 Oracle 조회
        long count =
            analysisJobRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void studentActivityStatConnectionTest() {

        // STUDENT_ACTIVITY_STAT 실제 Oracle 조회
        long count =
            studentActivityStatRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void courseActivityStatConnectionTest() {

        // COURSE_ACTIVITY_STAT 실제 Oracle 조회
        long count =
            courseActivityStatRepository.count();

        assertTrue(count > 0);
    }

    @Test
    void courseWeeklyActivityStatConnectionTest() {

        // COURSE_WEEKLY_ACTIVITY_STAT 실제 Oracle 조회
        long count =
            courseWeeklyActivityStatRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void vleActivityStatConnectionTest() {

        // Oracle VLE_ACTIVITY_STAT 실제 조회
        long count =
            vleActivityStatRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void assessmentStatConnectionTest() {

        // Oracle ASSESSMENT_STAT 실제 조회
        long count =
            assessmentStatRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void registrationStatConnectionTest() {

        // REGISTRATION_STAT 실제 Oracle 조회
        long count =
            registrationStatRepository.count();

        assertTrue(count > 0);
    }

    @Test
    void courseResultStatConnectionTest() {

        // COURSE_RESULT_STAT 실제 Oracle 조회
        long count =
            courseResultStatRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void activityResultStatConnectionTest() {

        long count =
            activityResultStatRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void studentLearningSummaryStatConnectionTest() {

        long count =
            studentLearningSummaryStatRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void datasetFileConnectionTest() {

        long count =
            datasetFileRepository.count();

        assertTrue(count > 0);
    }

    @Test
    void dataQualityStatConnectionTest() {

        long count =
            dataQualityStatRepository.count();

        assertTrue(count > 0);
    }
    
    @Test
    void securityTableConnectionTest() {

        /*
         * 아직 사용자나 Role 데이터가 0건일 수도 있으므로
         * DB 접근이 성공하는지만 확인한다.
         */
        assertTrue(appUserRepository.count() >= 0);
        assertTrue(appRoleRepository.count() >= 0);
        assertTrue(appUserRoleRepository.count() >= 0);
    }
    
    @Test
    void auditLogConnectionTest() {

        // Audit가 아직 0건이어도 DB 접근 성공이면 정상
        long count =
            auditLogRepository.count();

        assertTrue(count >= 0);
    }
    
    @Autowired
    private StudentLearningSummaryStatRepository
        studentLearningSummaryStatRepository;
    
    @Autowired
    private AssessmentStatRepository assessmentStatRepository;
    
    @Autowired
    private OuladStudentRepository ouladStudentRepository;
    
    @Autowired
    private StudentRegistrationRepository studentRegistrationRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private AssessmentRepository assessmentRepository;
    
    @Autowired
    private StudentAssessmentRepository studentAssessmentRepository;
    
    @Autowired
    private VleMaterialRepository vleMaterialRepository;
    
    @Autowired
    private AnalysisJobRepository analysisJobRepository;
    
    @Autowired
    private StudentActivityStatRepository studentActivityStatRepository;
    
    @Autowired
    private CourseActivityStatRepository courseActivityStatRepository;

    @Autowired
    private CourseWeeklyActivityStatRepository courseWeeklyActivityStatRepository;
    
    @Autowired
    private VleActivityStatRepository vleActivityStatRepository;
    
    @Autowired
    private RegistrationStatRepository registrationStatRepository;

    @Autowired
    private CourseResultStatRepository courseResultStatRepository;
    
    @Autowired
    private ActivityResultStatRepository activityResultStatRepository;
    
    @Autowired
    private DatasetFileRepository datasetFileRepository;

    @Autowired
    private DataQualityStatRepository dataQualityStatRepository;
    
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private AppUserRoleRepository appUserRoleRepository;
}
