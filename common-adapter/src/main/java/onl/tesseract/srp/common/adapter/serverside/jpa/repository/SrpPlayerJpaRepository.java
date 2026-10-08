package onl.tesseract.srp.common.adapter.serverside.jpa.repository;

import onl.tesseract.srp.common.adapter.serverside.jpa.entity.SrpPlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID; /**
 * JPA repository interface for SrpPlayerEntity.
 */
@Repository
public interface SrpPlayerJpaRepository extends JpaRepository<SrpPlayerEntity, UUID> {
}
