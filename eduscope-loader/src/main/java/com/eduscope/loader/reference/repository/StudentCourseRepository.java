package com.eduscope.loader.reference.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.reference.entity.CoursePresentation;
import com.eduscope.loader.reference.entity.OuladStudent;
import com.eduscope.loader.reference.entity.StudentCourse;

public interface StudentCourseRepository 
    extends JpaRepository<StudentCourse, Long> {
	
	Optional<StudentCourse>
	  findByCoursePresentationAndStudent(
			  CoursePresentation coursePresentation,
			  OuladStudent student);
	  
}
