package onl.tesseract.srp.commission.adapter.serverside.jpa.repository;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionDeliveredItemCountEntity;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionDeliveredItemCountJpaRepository extends JpaRepository<CommissionDeliveredItemCountEntity, Long> {
    Optional<CommissionDeliveredItemCountEntity> findByBoardIdAndTypeAndMinimalQuality(Long boardId, CommissionType type,
                                                                                       Quality minimalQuality);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CommissionDeliveredItemCountEntity d where d.boardId = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
