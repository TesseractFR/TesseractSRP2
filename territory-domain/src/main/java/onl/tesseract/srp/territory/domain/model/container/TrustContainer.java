package onl.tesseract.srp.territory.domain.model.container;

import onl.tesseract.srp.territory.domain.model.enums.result.TrustResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UntrustResult;

import java.util.Collection;
import java.util.UUID;

public interface TrustContainer {
    TrustResult addTrust(UUID player, UUID target);
    UntrustResult removeTrust(UUID player, UUID target);
    boolean isTrusted(UUID player);
    boolean canTrust(UUID player);
    Collection<UUID> getTrusted();
}

