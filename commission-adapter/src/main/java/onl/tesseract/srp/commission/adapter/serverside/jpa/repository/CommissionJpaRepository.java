package onl.tesseract.srp.commission.adapter.serverside.jpa.repository;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionJpaRepository extends JpaRepository<CommissionEntity, Long> {
    List<CommissionEntity> findByBoardIdOrderById(Long boardId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CommissionEntity c where c.boardId = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CommissionEntity c set c.deliveredQuantity = c.deliveredQuantity + :itemCount where c.id = :id")
    void addDeliveredQuantity(@Param("id") Long id, @Param("itemCount") int itemCount);
}
