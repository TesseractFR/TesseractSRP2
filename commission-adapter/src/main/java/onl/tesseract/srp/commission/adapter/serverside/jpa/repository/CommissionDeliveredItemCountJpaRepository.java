package onl.tesseract.srp.commission.adapter.serverside.jpa.repository;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionDeliveredItemCountEntity;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface CommissionDeliveredItemCountJpaRepository extends JpaRepository<CommissionDeliveredItemCountEntity, Long> {
    Optional<CommissionDeliveredItemCountEntity> findByBoardIdAndTypeAndMinimalQuality(Long boardId, CommissionType type,
                                                                                       Quality minimalQuality);

    @Query("select coalesce(sum(d.itemCount), 0) from CommissionDeliveredItemCountEntity d"
            + " where d.boardId = :boardId and d.type = :type and d.minimalQuality in :qualities")
    int sumItemCount(@Param("boardId") Long boardId, @Param("type") CommissionType type,
                     @Param("qualities") Collection<Quality> qualities);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CommissionDeliveredItemCountEntity d where d.boardId = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
}
