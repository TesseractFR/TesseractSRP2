package onl.tesseract.srp.controller.command.argument;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;

import java.util.stream.Collectors;

public class CustomItemArg extends CommandArgument<CustomMaterial> {
    public CustomItemArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<CustomMaterial> builder) {
        builder.parser((input, ignored) -> {
                    CustomItemService service = ServiceContainer.get(CustomItemService.class);
                    CustomMaterial material = service.getCustomMaterial(new MaterialName(input));
                    if (material == null) {
                        throw new IllegalArgumentException("Matériau invalide");
                    }
                    return material;
                })
                .tabCompleter((ignored, context) -> {
                    CustomItemService service = ServiceContainer.get(CustomItemService.class);
                    return service.getCustomMaterials().keySet().stream()
                            .map(MaterialName::value)
                            .collect(Collectors.toList());
                })
                .errorHandler(IllegalArgumentException.class, "Matériau invalide");
    }
}