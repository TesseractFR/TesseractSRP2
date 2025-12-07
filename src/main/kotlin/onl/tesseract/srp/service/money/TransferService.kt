package onl.tesseract.srp.service.money

import jakarta.transaction.Transactional
import onl.tesseract.srp.domain.money.ledger.Ledger
import onl.tesseract.srp.domain.money.ledger.TransactionSubType
import onl.tesseract.srp.domain.money.ledger.TransactionType
import onl.tesseract.srp.service.territory.guild.GuildService
import onl.tesseract.srp.service.player.SrpPlayerService
import org.springframework.stereotype.Service
import java.util.*

/**
 * Used to transfer money between to accounts atomically, while recording only one transaction.
 */
@Service
open class TransferService(private val moneyLedgerService: MoneyLedgerService) {

    /**
     * Transfer money between to accounts atomically in one transaction.
     * @param build Builder method to set the origin and destination of the transfer. The builder defines extension
     * functions in [SrpPlayerService] (fromPlayer, toPlayer) and [GuildService] (fromGuild, toGuild).
     */
    @Transactional
    open fun transferMoney(
        amount: Int,
        type: TransactionType,
        subType: TransactionSubType,
        details: String? = null,
        build: TransferTransactionBuilder.() -> Unit
    ) {
        val builder = TransferTransactionBuilder(amount, type, subType, details)
        builder.build()

        moneyLedgerService.recordTransaction(
            checkNotNull(builder.from) { "Missing from side of transaction" },
            checkNotNull(builder.to) { "Missing to side of transaction" },
            builder.amount,
            builder.type,
            builder.subType,
            builder.details,
        )
    }

    inner class TransferTransactionBuilder(
        val amount: Int,
        val type: TransactionType,
        val subType: TransactionSubType,
        val details: String? = null
    ) {
        var from: Ledger? = null
            set(value) {
                check(field == null) { "from side of transaction already set" }
                field = value
            }
        var to: Ledger? = null
            set(value) {
                check(field == null) { "to side of transaction already set" }
                field = value
            }

        init {
            require(amount > 0)
        }

        fun SrpPlayerService.fromPlayer(playerID: UUID) {
            moneyTransaction(playerID, -amount, this@TransferTransactionBuilder)
        }

        fun SrpPlayerService.toPlayer(playerID: UUID) {
            moneyTransaction(playerID, amount, this@TransferTransactionBuilder)
        }

        fun GuildService.fromGuild(guildID: UUID) {
            moneyTransaction(guildID, -amount, this@TransferTransactionBuilder)
        }

        fun GuildService.toGuild(guildID: UUID) {
            moneyTransaction(guildID, amount, this@TransferTransactionBuilder)
        }

    }
}
