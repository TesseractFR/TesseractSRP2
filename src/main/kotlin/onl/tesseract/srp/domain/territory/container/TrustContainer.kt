package onl.tesseract.srp.domain.territory.container

import onl.tesseract.srp.domain.territory.enum.result.TrustResult
import onl.tesseract.srp.domain.territory.enum.result.UntrustResult
import java.util.*

interface TrustContainer{
    fun addTrust(player: UUID, target: UUID): TrustResult
    fun removeTrust(player: UUID, target: UUID): UntrustResult
    fun isTrusted(player: UUID): Boolean
    fun canTrust(player: UUID): Boolean
    fun getTrusted() : Collection<UUID>
}

open class DefaultTrustContainer(protected val trusted: MutableSet<UUID> = mutableSetOf()) : TrustContainer{

    override fun addTrust(
        player: UUID,
        target: UUID,
    ): TrustResult {
        if(trusted.add(target))return TrustResult.SUCCESS
        return TrustResult.ALREADY_TRUST
    }

    override fun canTrust(player: UUID): Boolean {
        return true
    }

    override fun getTrusted(): Collection<UUID> {
        return trusted
    }


    override fun removeTrust(
        player: UUID,
        target: UUID,
    ): UntrustResult {
        if(!isTrusted(target)) return UntrustResult.NOT_TRUST
        if(trusted.remove(target))return UntrustResult.SUCCESS
        return UntrustResult.NOT_TRUST
    }

    override fun isTrusted(player: UUID): Boolean {
        return trusted.contains(player)
    }

}

