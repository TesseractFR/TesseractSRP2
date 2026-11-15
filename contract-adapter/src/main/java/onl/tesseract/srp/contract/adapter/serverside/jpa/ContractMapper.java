package onl.tesseract.srp.contract.adapter.serverside.jpa;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractEntity;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import org.springframework.stereotype.Component;

/** Convertit les contrats du domaine en entités JPA et inversement. */
@Component
public class ContractMapper {

    /** @return une entité sans identifiant, que la base complète à l'insertion */
    public ContractEntity toEntity(ContractDraft draft) {
        ContractEntity entity = new ContractEntity();
        entity.setType(draft.type());
        entity.setJob(draft.client().name());
        entity.setMaterial(draft.material().value());
        entity.setQuantity(draft.quantity());
        entity.setMinimalQuality(draft.minimalQuality());
        entity.setReward(draft.reward().value().intValueExact());
        entity.setExpiresAt(draft.expiresAt());
        return entity;
    }

    public Contract toDomain(ContractEntity entity) {
        return new Contract(new ContractId(entity.getId()), entity.getType(), new ContractClient(entity.getJob()),
                new MaterialName(entity.getMaterial()), entity.getQuantity(), entity.getMinimalQuality(),
                MoneyValue.of(entity.getReward()), entity.getExpiresAt());
    }
}
