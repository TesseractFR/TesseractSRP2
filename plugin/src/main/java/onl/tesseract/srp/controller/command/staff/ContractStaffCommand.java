package onl.tesseract.srp.controller.command.staff;

import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.entity.Player;

@org.springframework.stereotype.Component
@Command(name = "contract")
public class ContractStaffCommand {
    private ContractStaffCommand() {
    }

    @org.springframework.stereotype.Component
    @Command(name = "tickets", description = "Gérer les tickets de contrat d'un joueur")
    public static class TicketsCommand {
        private final ContractService contractService;

        public TicketsCommand(ContractService contractService) {
            this.contractService = contractService;
        }

        @Command(name = "give", description = "Ajouter des tickets à un joueur")
        public void give(@Argument("joueur") PlayerArg playerArg, @Argument("amount") IntegerCommandArgument amountArg,
                         Player sender) {
            contractService.giveTicketsAsStaff(new PlayerID(sender.getUniqueId()),
                    new PlayerID(playerArg.get().getUniqueId()), amountArg.get());
        }

        @Command(name = "get", description = "Voir les tickets d'un joueur")
        public void get(@Argument("joueur") PlayerArg playerArg, Player sender) {
            contractService.showTicketsAsStaff(new PlayerID(sender.getUniqueId()),
                    new PlayerID(playerArg.get().getUniqueId()));
        }
    }
}