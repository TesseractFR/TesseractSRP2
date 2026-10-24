package onl.tesseract.srp.contract.adapter.serverside.jpa.mapper;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractEntity;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public Contract toDomain(ContractEntity e) {
        return new Contract(e.getId(), new PlayerID(e.getIssuedTo()), e.getType(), e.getJob(), e.getMaterial(),
                e.getQuantity(), e.getMinimalQuality(), e.getReward(), e.getTier(),
                e.getIssuedAt(), e.getExpiresAt(), e.getVersion());
    }

    public ContractEntity toEntity(Contract c) {
        return new ContractEntity(c.id(), c.issuedTo().value(), c.type(), c.job(), c.material(),
                c.quantity(), c.minimalQuality(), c.reward(), c.tier(),
                c.issuedAt(), c.expiresAt(), c.version());
    }
}
