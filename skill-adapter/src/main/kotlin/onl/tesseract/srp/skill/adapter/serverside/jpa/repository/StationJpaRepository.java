package onl.tesseract.srp.skill.adapter.serverside.jpa.repository;

import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.station.StationEntity;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.station.StationEntityKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationJpaRepository extends JpaRepository<StationEntity, StationEntityKey> {

}
