package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.territory.domain.port.serverside.SrpPlayerRepository;
import org.springframework.stereotype.Component;

@Component
public class SrpPlayerRepositoryImpl implements SrpPlayerRepository {

    @Override
    public boolean isBaron() {
        // TODO: Implement based on player rank system
        return false;
    }

    @Override
    public int getCampLevel() {
        // TODO: Implement based on player camp level
        return 1;
    }
}

