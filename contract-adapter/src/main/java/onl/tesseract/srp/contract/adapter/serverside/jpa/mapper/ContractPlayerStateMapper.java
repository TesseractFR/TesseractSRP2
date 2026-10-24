package onl.tesseract.srp.contract.adapter.serverside.jpa.mapper;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.ticket.ContractPlayerState;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractPlayerStateEntity;
import org.springframework.stereotype.Component;

@Component
public class ContractPlayerStateMapper {

    public ContractPlayerState toDomain(ContractPlayerStateEntity e) {
        return new ContractPlayerState(new PlayerID(e.getPlayerId()), e.getTickets(), e.getTicketsDay(),
                e.getDeliveredCount(), e.isDiscoveryGiven());
    }

    public ContractPlayerStateEntity toEntity(ContractPlayerState s) {
        return new ContractPlayerStateEntity(s.playerId().value(), s.tickets(), s.ticketsDay(),
                s.deliveredCount(), s.discoveryGiven());
    }
}
