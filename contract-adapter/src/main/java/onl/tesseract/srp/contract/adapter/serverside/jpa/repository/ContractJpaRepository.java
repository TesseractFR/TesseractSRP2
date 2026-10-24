package onl.tesseract.srp.contract.adapter.serverside.jpa.repository;

import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ContractJpaRepository extends JpaRepository<ContractEntity, Long> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ContractEntity c where c.id = :id")
    int deleteByIdCounting(@Param("id") Long id);

    @Query("select c.id from ContractEntity c where c.expiresAt <= :now")
    List<Long> findExpiredIds(@Param("now") Instant now);
}
