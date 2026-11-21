package onl.tesseract.srp.commission.adapter.serverside.jpa;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionEntity;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDraft;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.springframework.stereotype.Component;

/** Convertit les commissions du domaine en entités JPA et inversement. */
@Component
public class CommissionMapper {

    /** @return une entité sans identifiant ni livraison, que la base complète à l'insertion */
    public CommissionEntity toEntity(CommissionDraft draft) {
        CommissionEntity entity = new CommissionEntity();
        entity.setBoardId(draft.boardId().value());
        entity.setPart(draft.part());
        entity.setType(draft.type());
        entity.setMaterial(draft.material().value());
        entity.setQuantity(draft.quantity());
        entity.setDeliveredQuantity(0);
        entity.setMinimalQuality(draft.minimalQuality());
        entity.setReward(draft.reward().value().intValueExact());
        return entity;
    }

    public Commission toDomain(CommissionEntity entity) {
        return new Commission(new CommissionId(entity.getId()), new CommissionBoardId(entity.getBoardId()), entity.getPart(),
                entity.getType(), new MaterialName(entity.getMaterial()), entity.getQuantity(), entity.getDeliveredQuantity(),
                entity.getMinimalQuality(), MoneyValue.of(entity.getReward()));
    }
}
