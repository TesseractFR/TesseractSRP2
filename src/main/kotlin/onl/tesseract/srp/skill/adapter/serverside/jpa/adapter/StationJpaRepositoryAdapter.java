package onl.tesseract.srp.skill.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.station.StationEntityKey;
import onl.tesseract.srp.skill.adapter.serverside.jpa.mapper.StationMapper;
import onl.tesseract.srp.skill.adapter.serverside.jpa.repository.StationJpaRepository;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.model.station.StationKey;
import onl.tesseract.srp.skill.domain.port.serverside.StationRepository;
import org.springframework.stereotype.Component;

@Component
public class StationJpaRepositoryAdapter implements StationRepository {

    private final StationJpaRepository stationJpaRepository;
    private final StationMapper stationEntityMapper;

    public StationJpaRepositoryAdapter(StationJpaRepository stationJpaRepository, StationMapper stationEntityMapper) {
        this.stationJpaRepository = stationJpaRepository;
        this.stationEntityMapper = stationEntityMapper;
    }

    @Override
    public Station get(StationKey key) {
        StationEntityKey entityKey = stationEntityMapper.toEntity(key);
        return stationJpaRepository
                .findById(entityKey)
                .map(stationEntityMapper::toDomain)
                .orElseThrow(() -> new RuntimeException("Station not found for key: " + key));
    }
}
