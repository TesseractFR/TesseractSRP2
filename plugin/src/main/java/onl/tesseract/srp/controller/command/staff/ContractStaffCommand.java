package onl.tesseract.srp.controller.command.staff;

import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.adapter.userside.controller.message.ContractMessageFormatter;
import onl.tesseract.srp.contract.domain.model.exception.ContractException;
import onl.tesseract.srp.contract.domain.port.userside.ContractTicketService;
import org.bukkit.entity.Player;

@org.springframework.stereotype.Component
@Command(name = "contract")
public class ContractStaffCommand {
    private ContractStaffCommand() {
    }

    @org.springframework.stereotype.Component
    @Command(name = "tickets", description = "Gérer les tickets de contrat d'un joueur")
    public static class TicketsCommand {
        private final ContractTicketService contractTicketService;
        private final ContractMessageFormatter contractMessageFormatter;

        public TicketsCommand(ContractTicketService contractTicketService, ContractMessageFormatter contractMessageFormatter) {
            this.contractTicketService = contractTicketService;
            this.contractMessageFormatter = contractMessageFormatter;
        }

        @Command(name = "give", description = "Ajouter des tickets à un joueur")
        public void give(@Argument("joueur") PlayerArg playerArg, @Argument("amount") IntegerCommandArgument amountArg,
                         Player sender) {
            Player target = playerArg.get();
            int amount = amountArg.get();
            try {
                contractTicketService.giveTickets(new PlayerId(target.getUniqueId()), amount);
                sender.sendMessage(contractMessageFormatter.formatStaffTicketsGiven(target.getName(), amount));
            } catch (ContractException refusal) {
                sender.sendMessage(contractMessageFormatter.formatRefusal(refusal));
            }
        }

        @Command(name = "get", description = "Voir les tickets d'un joueur")
        public void get(@Argument("joueur") PlayerArg playerArg, Player sender) {
            Player target = playerArg.get();
            PlayerId targetId = new PlayerId(target.getUniqueId());
            sender.sendMessage(contractMessageFormatter.formatStaffTickets(target.getName(),
                    contractTicketService.getTickets(targetId), contractTicketService.getDailyQuota(targetId)));
        }
    }
}
