package onl.tesseract.srp.territory.domain.port.serverside;

import onl.tesseract.srp.territory.domain.model.Territory;
import onl.tesseract.lib.repository.Repository;

import java.util.UUID;



public interface TerritoryRepository<T extends Territory<?>, ID> extends Repository<T, ID> {
    T findByPlayer(UUID player);
}

