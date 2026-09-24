package onl.tesseract.srp.controller.command.staff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.commandBuilder.annotation.Argument;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.commandBuilder.annotation.Env;
import onl.tesseract.lib.command.argument.IntegerCommandArgument;
import onl.tesseract.lib.command.argument.PlayerArg;
import onl.tesseract.srp.service.job.PlayerJobService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


@Command(name = "job", args = {
    @Argument(value = "player", clazz = PlayerArg.class)
})
public class PlayerJobStaffCommand {
    private final PlayerJobService service;

    public PlayerJobStaffCommand(PlayerJobService service) {
        this.service = service;
    }

    @Command(name = "getLevel")
    public void getLevel(@Env(key = "player") Player player, CommandSender sender) {
        var progression = service.getPlayerJobProgression(player.getUniqueId());
        sender.sendMessage(Component.text( "Niveau : " + progression.getLevel(),NamedTextColor.GREEN));
        sender.sendMessage(Component.text( "XP : " + progression.getXp(),NamedTextColor.GREEN));
        sender.sendMessage(Component.text( "Points de compétence : " + progression.getSkillPoints(),NamedTextColor.GREEN));
    }

    @Command(name = "xp")
    public static class XpCommand {
        private final PlayerJobService service;

        public XpCommand(PlayerJobService service) {
            this.service = service;
        }

        @Command
        public void give(@Env(key = "player") Player player, @Argument("amount") IntegerCommandArgument amount, CommandSender sender) {
            service.addXp(player.getUniqueId(), amount.get());
            sender.sendMessage(Component.text("XP ajouté !",NamedTextColor.GREEN));
        }

        @Command
        public void remove(@Env(key = "player") Player player, @Argument("amount") IntegerCommandArgument amount, CommandSender sender) {
            service.addXp(player.getUniqueId(), -amount.get());
            sender.sendMessage(Component.text("XP retiré !",NamedTextColor.GREEN));
        }

        @Command
        public void clear(@Env(key = "player") Player player, CommandSender sender) {
            service.clearXp(player.getUniqueId());
            sender.sendMessage(Component.text("XP remis à 0 !",NamedTextColor.GREEN));
        }
    }

    @Command(name = "level")
    public static class LevelCommand {
        private final PlayerJobService service;

        public LevelCommand(PlayerJobService service) {
            this.service = service;
        }

        @Command
        public void give(@Env(key = "player") Player player, @Argument("amount") IntegerCommandArgument amount, CommandSender sender) {
            service.addLevel(player.getUniqueId(), amount.get());
            sender.sendMessage(Component.text("Niveau ajouté !",NamedTextColor.GREEN));
        }

        @Command
        public void remove(@Env(key = "player") Player player, @Argument("amount") IntegerCommandArgument amount, CommandSender sender) {
            service.addLevel(player.getUniqueId(), -amount.get());
            sender.sendMessage(Component.text("Niveau retiré !",NamedTextColor.GREEN));
        }
    }

    @Command(name = "skillpoints")
    public static class SkillPointCommand {
        private final PlayerJobService service;

        public SkillPointCommand(PlayerJobService service) {
            this.service = service;
        }

        @Command
        public void give(@Env(key = "player") Player player, @Argument("amount") IntegerCommandArgument amount, CommandSender sender) {
            service.addSkillPoint(player.getUniqueId(), amount.get());
            sender.sendMessage(Component.text("Points de compétence ajoutés !",NamedTextColor.GREEN));
        }
    }

}

