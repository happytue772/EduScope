package com.eduscope.loader.reference.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.reference.entity.CoursePresentation;

public interface CoursePresentationRepository 
   extends JpaRepository<CoursePresentation, Long>{
	
	Optional<CoursePresentation>
	  findByDatasetAndCodeModuleAndCodePresentation(
			  Dataset dataset,
			  String codeModule,
			  String codePresentation);

}
