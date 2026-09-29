package com.eduscope.loader.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eduscope.loader.reference.entity.StudentRegistration;

public interface StudentRegistrationRepository 
     extends JpaRepository<StudentRegistration, Long> {

}
