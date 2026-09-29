package com.eduscope.loader.dataset.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eduscope.loader.dataset.entity.Dataset;

public interface DatasetRepository
  extends JpaRepository<Dataset, Long> {
	
	Optional<Dataset> findBySourceNameAndDatasetVersion(
			String sourceName,
			String datasetVersion
			);
}
