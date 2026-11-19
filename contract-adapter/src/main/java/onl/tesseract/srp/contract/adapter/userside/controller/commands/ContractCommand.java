package onl.tesseract.srp.contract.adapter.userside.controller.commands;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.common.adapter.SrpCommandInstanceProvider;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.adapter.scroll.ContractScrollReader;
import onl.tesseract.srp.contract.adapter.userside.controller.commands.args.ContractJobArgument;
import onl.tesseract.srp.contract.adapter.userside.controller.menu.ContractDeliveryMenu;
import onl.tesseract.srp.contract.adapter.userside.controller.menu.ContractMerchantMenu;
import onl.tesseract.srp.contract.adapter.userside.controller.message.ContractMessageFormatter;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.port.userside.ContractDeliveryService;
import onl.tesseract.srp.contract.domain.port.userside.ContractPurchaseService;
import onl.tesseract.srp.contract.domain.port.userside.ContractTicketService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

/** Commandes joueur pour ouvrir le marchand, livrer un parchemin et consulter ses tickets. */
@Component
@Command(name = "contract", playerOnly = true)
public class ContractCommand extends CommandContext {

    private final ContractPurchaseService contractPurchaseService;
    private final ContractDeliveryService contractDeliveryService;
    private final ContractTicketService contractTicketService;
    private final ContractMessageFormatter contractMessageFormatter;

    public ContractCommand(SrpCommandInstanceProvider provider, ContractPurchaseService contractPurchaseService,
                           ContractDeliveryService contractDeliveryService, ContractTicketService contractTicketService,
                           ContractMessageFormatter contractMessageFormatter) {
        super(provider);
        this.contractPurchaseService = contractPurchaseService;
        this.contractDeliveryService = contractDeliveryService;
        this.contractTicketService = contractTicketService;
        this.contractMessageFormatter = contractMessageFormatter;
    }

    @Command(name = "merchant", description = "Ouvrir le menu du marchand de contrats.")
    public void openMerchantMenu(Player sender) {
        new ContractMerchantMenu(new PlayerId(sender.getUniqueId()), contractPurchaseService, contractTicketService,
                contractMessageFormatter).open(sender);
    }

    @Command(name = "deliver", description = "Livrer le parchemin tenu en main au PNJ d'un métier.")
    public void deliverHeldScroll(Player sender, @Argument("metier") ContractJobArgument jobArgument) {
        PlayerId player = new PlayerId(sender.getUniqueId());
        ContractClient npc = jobArgument.get();
        ContractScrollReader.readContractId(sender.getInventory().getItemInMainHand())
                .ifPresentOrElse(
                        contractId -> contractDeliveryService.findContract(contractId).ifPresentOrElse(
                                contract -> new ContractDeliveryMenu(player, npc, contract, contractDeliveryService,
                                        contractMessageFormatter).open(sender),
                                () -> sender.sendMessage(contractMessageFormatter.formatNpcInvalidScroll(npc))),
                        () -> sender.sendMessage(contractMessageFormatter.formatNpcNoScrollInHand(npc)));
    }

    @Command(name = "tickets", description = "Voir ses tickets de contrat.")
    public void showTickets(Player sender) {
        PlayerId player = new PlayerId(sender.getUniqueId());
        sender.sendMessage(contractMessageFormatter.formatTickets(contractTicketService.getTickets(player),
                contractTicketService.getDailyQuota(player)));
    }
}
