package com.eduscope.loader.analysis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.analysis.entity.ActivityResultStat;
import com.eduscope.loader.analysis.entity.ActivityResultStatId;

public interface ActivityResultStatRepository
        extends JpaRepository<ActivityResultStat, ActivityResultStatId> {

    long countByJobId(Long jobId);

    List<ActivityResultStat> findAllByJobId(Long jobId);
}