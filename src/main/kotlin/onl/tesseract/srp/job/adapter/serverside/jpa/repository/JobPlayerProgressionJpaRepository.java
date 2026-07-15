package onl.tesseract.srp.job.adapter.serverside.jpa.repository;

import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobPlayerProgressionEntity;
import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobPlayerProgressionEntityKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPlayerProgressionJpaRepository extends JpaRepository<JobPlayerProgressionEntity, JobPlayerProgressionEntityKey> {
}
