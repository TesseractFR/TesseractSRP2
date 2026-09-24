package onl.tesseract.srp.territory.domain.model.container;

import onl.tesseract.srp.territory.domain.model.enums.result.TrustResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UntrustResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DefaultTrustContainer implements TrustContainer {
    protected final Set<UUID> trusted;

    public DefaultTrustContainer() {
        this.trusted = new HashSet<>();
    }

    public DefaultTrustContainer(Set<UUID> trusted) {
        this.trusted = trusted;
    }

    @Override
    public TrustResult addTrust(UUID player, UUID target) {
        if (trusted.add(target)) return TrustResult.SUCCESS;
        return TrustResult.ALREADY_TRUST;
    }

    @Override
    public boolean canTrust(UUID player) {
        return true;
    }

    @Override
    public Collection<UUID> getTrusted() {
        return trusted;
    }

    @Override
    public UntrustResult removeTrust(UUID player, UUID target) {
        if (!isTrusted(target)) return UntrustResult.NOT_TRUST;
        if (trusted.remove(target)) return UntrustResult.SUCCESS;
        return UntrustResult.NOT_TRUST;
    }

    @Override
    public boolean isTrusted(UUID player) {
        return trusted.contains(player);
    }
}

