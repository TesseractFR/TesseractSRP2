package onl.tesseract.srp.commission.adapter.serverside.jpa.repository;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionBoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionBoardJpaRepository extends JpaRepository<CommissionBoardEntity, Long> {
    Optional<CommissionBoardEntity> findByGuildId(String guildId);

    Optional<CommissionBoardEntity> findByGuildIdIsNull();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CommissionBoardEntity b set b.completedCommissionCount = b.completedCommissionCount + 1 where b.id = :id")
    void incrementCompletedCommissionCount(@Param("id") Long id);
}
