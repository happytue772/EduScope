package com.eduscope.loader.dataset.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduscope.loader.dataset.entity.Dataset;
import com.eduscope.loader.dataset.entity.DatasetFile;

public interface DatasetFileRepository
        extends JpaRepository<DatasetFile, Long> {

    Optional<DatasetFile> findByDatasetAndFileType(
            Dataset dataset,
            String fileType
    );
}