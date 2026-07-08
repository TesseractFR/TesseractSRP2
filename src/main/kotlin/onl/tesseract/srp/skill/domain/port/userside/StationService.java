package onl.tesseract.srp.skill.domain.port.userside;

import onl.tesseract.srp.domain.commun.ChunkCoord;
import onl.tesseract.srp.domain.skill.station.StatType;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.model.station.StationKey;
import onl.tesseract.srp.skill.domain.model.station.StationStats;
import onl.tesseract.srp.skill.domain.port.serverside.StationRepository;
import onl.tesseract.srp.skill.domain.port.serverside.TerritoryRepository;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StationService {

    final private TerritoryRepository territoryRepository;
    final private StationRepository stationRepository;
    private final Map<StationKey, Station> stationCache = new HashMap<>();

    public StationService(TerritoryRepository territoryRepository, StationRepository stationRepository) {
        this.territoryRepository = territoryRepository;
        this.stationRepository = stationRepository;
    }


    public Station getStation(StationKey key) {
        if (stationCache.containsKey(key)) {
            return stationCache.get(key);
        }
        Station station = stationRepository.get(key);
        stationCache.put(key, station);
        return station;
    }

    public Station getStationByChunkCoord(ChunkCoord coord, SkillName skillName) {
        UUID territory = territoryRepository.get(coord);
        //On n'est pas dans un territoire accepté donc non améliorable donc pas défault
        if(territory == null){
            return new Station(new StationKey(skillName),new StationStats());
        }
        return getStation(new StationKey(territory, skillName));
    }

    public Station upgradeStat(@NotNull Station station, @NotNull StatType statType){
        Station upgradedStation = station.upgradeStat(statType);
        stationCache.put(station.key(), upgradedStation);
        return upgradedStation;
    }
}
