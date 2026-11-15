package onl.tesseract.srp.contract.adapter.serverside.ticket;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractTicketEntity;
import onl.tesseract.srp.contract.adapter.serverside.jpa.repository.ContractTicketJpaRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractTicketRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.OptionalInt;

/** Persiste le solde de tickets des joueurs dans la table {@code t_contract_player_state}. */
@Component
public class JpaContractTicketRepository implements ContractTicketRepository {

    private final ContractTicketJpaRepository contractTicketJpaRepository;

    public JpaContractTicketRepository(ContractTicketJpaRepository contractTicketJpaRepository) {
        this.contractTicketJpaRepository = contractTicketJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OptionalInt findTickets(PlayerId player) {
        return contractTicketJpaRepository.findById(player.value().toString())
                .map(entity -> OptionalInt.of(entity.getTickets()))
                .orElse(OptionalInt.empty());
    }

    @Override
    @Transactional
    public void saveTickets(PlayerId player, int tickets) {
        contractTicketJpaRepository.save(new ContractTicketEntity(player.value().toString(), tickets));
    }
}
