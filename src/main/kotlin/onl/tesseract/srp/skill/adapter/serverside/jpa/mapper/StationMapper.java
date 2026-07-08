package onl.tesseract.srp.skill.adapter.serverside.jpa.mapper;

import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.station.StationEntity;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.station.StationEntityKey;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.model.station.StationKey;
import onl.tesseract.srp.skill.domain.model.station.StationStats;
import onl.tesseract.srp.skill.domain.model.station.UpgradeLevel;
import org.springframework.stereotype.Component;

@Component
public class StationMapper {

    public Station toDomain(StationEntity entity) {
        return new Station(
            toDomain(entity.getKey()),
            new StationStats(
                new UpgradeLevel(entity.getTierLevel()),
                new UpgradeLevel(entity.getQualityBonusLevel()),
                new UpgradeLevel(entity.getSuccessBonusLevel()),
                new UpgradeLevel(entity.getTimeReducLevel())
            )
        );
    }

    public StationEntity toEntity(Station domain) {
        return new StationEntity(
            toEntity(domain.key()),
            domain.stats().tier().value(),
            domain.stats().quality().value(),
            domain.stats().success().value(),
            domain.stats().timeReduction().value()
        );
    }

    public StationEntityKey toEntity(StationKey domainKey) {
        return new StationEntityKey(domainKey.skillName().value(),domainKey.territoryId());
    }

    public StationKey toDomain(StationEntityKey entityKey) {
        return new StationKey(entityKey.getTerritory(), new SkillName(entityKey.getSkillName()));
    }
}
