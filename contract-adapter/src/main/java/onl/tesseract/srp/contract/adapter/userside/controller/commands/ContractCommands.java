package onl.tesseract.srp.contract.adapter.userside.controller.commands;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.common.adapter.SrpCommandInstanceProvider;
import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.contract.adapter.userside.controller.commands.args.ContractJobArg;
import onl.tesseract.srp.contract.adapter.userside.controller.menu.ContractDeliveryMenu;
import onl.tesseract.srp.contract.adapter.userside.controller.menu.ContractMerchantMenu;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

import java.util.OptionalLong;

@Component
@Command(name = "contract", playerOnly = true)
public class ContractCommands extends CommandContext {

    private final ContractService contractService;

    public ContractCommands(SrpCommandInstanceProvider provider, ContractService contractService) {
        super(provider);
        this.contractService = contractService;
    }

    @Command(name = "merchant", description = "Ouvrir le menu du marchand de contrats.")
    public void merchant(Player sender) {
        new ContractMerchantMenu(new PlayerID(sender.getUniqueId()), contractService).open(sender);
    }

    @Command(name = "deliver", description = "Livrer le parchemin tenu en main au PNJ d'un métier.")
    public void deliver(Player sender, @Argument("metier") ContractJobArg jobArg) {
        PlayerID player = new PlayerID(sender.getUniqueId());
        String job = jobArg.get();
        OptionalLong contractId = ContractScrollItem.readContractId(sender.getInventory().getItemInMainHand());
        if (contractId.isEmpty()) {
            contractService.noScrollInHand(player, job);
            return;
        }
        contractService.prepareDelivery(player, contractId.getAsLong(), job).ifPresent(contract -> new ContractDeliveryMenu(player, job, contract, contractService).open(sender));
    }

    @Command(name = "tickets", description = "Voir ses tickets de contrat.")
    public void tickets(Player sender) {
        contractService.showTickets(new PlayerID(sender.getUniqueId()));
    }
}