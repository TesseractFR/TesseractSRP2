package onl.tesseract.srp.contract.adapter.userside.controller.commands.args;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.commandBuilder.CommandArgumentException;
import onl.tesseract.lib.service.ServiceContainer;

public class ContractJobArg extends CommandArgument<String> {

    public ContractJobArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<String> builder) {
        builder.parser((input, context) -> catalog().find(input)
                .orElseThrow(() -> new CommandArgumentException("Métier " + input + " introuvable")))
                .tabCompleter((sender, partial) -> catalog().names());
    }

    private ContractJobCatalog catalog() {
        return ServiceContainer.get(ContractJobCatalog.class);
    }
}