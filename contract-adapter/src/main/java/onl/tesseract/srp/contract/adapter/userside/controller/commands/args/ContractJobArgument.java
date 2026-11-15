package onl.tesseract.srp.contract.adapter.userside.controller.commands.args;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.commandBuilder.CommandArgumentException;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;

/** Résout et complète l'argument de commande désignant un métier ou une compétence. */
public class ContractJobArgument extends CommandArgument<ContractClient> {

    public ContractJobArgument(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<ContractClient> builder) {
        builder.parser((input, context) -> getCatalog().findByName(input)
                        .orElseThrow(() -> new CommandArgumentException("Métier " + input + " introuvable")))
                .tabCompleter((sender, partial) -> getCatalog().findAllNames());
    }

    private ContractJobCatalog getCatalog() {
        return ServiceContainer.get(ContractJobCatalog.class);
    }
}
