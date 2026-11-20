package onl.tesseract.srp.commission.adapter.serverside.jpa;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionBoardEntity;
import onl.tesseract.srp.commission.adapter.serverside.jpa.repository.CommissionBoardJpaRepository;
import onl.tesseract.srp.commission.adapter.serverside.jpa.repository.CommissionJpaRepository;
import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Persiste les tableaux dans la table {@code t_commission_boards}. */
@Component
public class JpaCommissionBoardRepository implements CommissionBoardRepository {

    private final CommissionBoardJpaRepository commissionBoardJpaRepository;
    private final CommissionJpaRepository commissionJpaRepository;
    private final CommissionBoardMapper commissionBoardMapper;

    public JpaCommissionBoardRepository(CommissionBoardJpaRepository commissionBoardJpaRepository,
                                        CommissionJpaRepository commissionJpaRepository, CommissionBoardMapper commissionBoardMapper) {
        this.commissionBoardJpaRepository = commissionBoardJpaRepository;
        this.commissionJpaRepository = commissionJpaRepository;
        this.commissionBoardMapper = commissionBoardMapper;
    }

    @Override
    @Transactional
    public CommissionBoard createSpawnBoard(BoardLevels levels) {
        return commissionBoardMapper.toDomain(commissionBoardJpaRepository.save(commissionBoardMapper.toNewSpawnBoardEntity(levels)));
    }

    @Override
    @Transactional
    public CommissionBoard createGuildBoard(GuildId guildId, double usageTaxRate) {
        return commissionBoardMapper.toDomain(commissionBoardJpaRepository.save(
                commissionBoardMapper.toNewGuildBoardEntity(guildId, usageTaxRate)));
    }

    /** Le compteur de commissions terminées n'est pas recopié : seule la livraison l'incrémente, en base. */
    @Override
    @Transactional
    public void save(CommissionBoard commissionBoard) {
        BoardId boardId = commissionBoard.getId();
        CommissionBoardEntity entity = commissionBoardJpaRepository.findById(boardId.value())
                .orElseThrow(() -> new CommissionBoardNotFoundException(boardId));
        commissionBoardMapper.copyChanges(commissionBoard, entity);
        commissionBoardJpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommissionBoard> findById(BoardId boardId) {
        return commissionBoardJpaRepository.findById(boardId.value()).map(commissionBoardMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommissionBoard> findSpawnBoard() {
        return commissionBoardJpaRepository.findByGuildIdIsNull().map(commissionBoardMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommissionBoard> findByGuild(GuildId guildId) {
        return commissionBoardJpaRepository.findByGuildId(guildId.value().toString()).map(commissionBoardMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByGuild(GuildId guildId) {
        commissionBoardJpaRepository.findByGuildId(guildId.value().toString()).ifPresent(entity -> {
            commissionJpaRepository.deleteByBoardId(entity.getId());
            commissionBoardJpaRepository.deleteById(entity.getId());
        });
    }
}
