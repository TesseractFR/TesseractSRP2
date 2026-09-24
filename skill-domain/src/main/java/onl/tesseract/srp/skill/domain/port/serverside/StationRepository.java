package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.model.station.StationKey;

public interface StationRepository {
    Station get(StationKey key);
}
