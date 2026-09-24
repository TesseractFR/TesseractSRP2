package onl.tesseract.srp.controller.menu.guild;

import kotlin.Unit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.chat.ChatEntryService;
import onl.tesseract.lib.menu.ItemBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.lib.util.ItemLoreBuilder;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.UUID;

import static onl.tesseract.lib.chat.ChatFormats.CHAT_ERROR;

public class GuildMenu extends Menu {
    private static final int BANK_BUTTON_INDEX = 4;

    private final UUID playerID;
    private final GuildService guildService;
    private final ChatEntryService chatService;

    public GuildMenu(UUID playerID, GuildService guildService, ChatEntryService chatService) {
        this(playerID, guildService, chatService, null);
    }

    public GuildMenu(UUID playerID, GuildService guildService, ChatEntryService chatService, Menu previous) {
        super(MenuSize.Five, Component.text("Guilde"), previous);
        this.playerID = playerID;
        this.guildService = guildService;
        this.chatService = chatService;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    @Override
    public void placeButtons(Player viewer) {
        Guild guild = guildService.getGuildByMember(viewer.getUniqueId());
        if (guild == null) {
            close();
            return;
        }

        addBankButton(guild, viewer);

        addBackButton();
        addCloseButton();
    }

    private void addBankButton(Guild guild, Player viewer) {
        GuildRole role = guild.getMemberRole(playerID);
        ItemLoreBuilder lore = new ItemLoreBuilder()
                .newline()
                .addField("Compte", Component.text(guild.getMoney() + " Lys", NamedTextColor.GREEN))
                .newline()
                .append("Clic gauche : ", NamedTextColor.GOLD)
                .append("Déposer", NamedTextColor.GRAY);
        if (role.canWithdrawMoney()) {
            lore.newline()
                    .append("Clic droit : ", NamedTextColor.GOLD)
                    .append("Retirer", NamedTextColor.GRAY);
        }

        addButton(
                BANK_BUTTON_INDEX,
                new ItemBuilder(Material.GOLD_INGOT)
                        .name("Banque de guilde", NamedTextColor.GOLD)
                        .lore(lore.get())
                        .build(),
                event -> {
                    if (event.getClick() == ClickType.LEFT) {
                        promptPlayerForBankOperation(guild, viewer, BankOperation.Deposit);
                    } else if (event.getClick() == ClickType.RIGHT) {
                        if (role.canWithdrawMoney()) {
                            promptPlayerForBankOperation(guild, viewer, BankOperation.Withdraw);
                        }
                    }
                }
        );
    }

    private void promptPlayerForBankOperation(Guild guild, Player viewer, BankOperation operation) {
        close();
        chatService.getChatEntry(viewer, Component.text("Montant à " + operation, NamedTextColor.GREEN), input -> {
            int amount;
            try {
                amount = Integer.parseInt(input);
                if (amount < 0) {
                    viewer.sendMessage(CHAT_ERROR.append(Component.text("Nombre invalide")));
                    return Unit.INSTANCE;
                }
            } catch (NumberFormatException e) {
                viewer.sendMessage(CHAT_ERROR.append(Component.text("Nombre invalide")));
                return Unit.INSTANCE;
            }

            try {
                boolean success;
                if (operation == BankOperation.Deposit) {
                    success = guildService.depositMoney(guild.getId(), playerID, amount);
                } else {
                    success = guildService.withdrawMoney(guild.getId(), playerID, amount);
                }

                if (success) {
                    open(viewer);
                } else {
                    viewer.sendMessage(CHAT_ERROR.append(Component.text("Argent insuffisant")));
                }
            } catch (Exception e) {
                viewer.sendMessage(CHAT_ERROR.append(Component.text("Une erreur est survenue")));
            }
            return Unit.INSTANCE;
        });
    }

    public enum BankOperation {
        Deposit("déposer"),
        Withdraw("retirer");

        private final String label;

        BankOperation(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
