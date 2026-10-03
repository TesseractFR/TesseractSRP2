package onl.tesseract.srp.controller.menu.elytra;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.core.cosmetics.menu.ElytraTrailSelectionMenu;
import onl.tesseract.lib.equipment.EquipmentService;
import onl.tesseract.lib.event.equipment.invocable.Elytra;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.srp.domain.equipment.elytra.ElytraInvocationResult;
import onl.tesseract.srp.service.equipment.elytra.ElytraService;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static onl.tesseract.lib.chat.ChatFormats.ELYTRA_ERROR;
import static onl.tesseract.lib.chat.ChatFormats.ELYTRA_SUCCESS;
import static onl.tesseract.srp.common.adapter.utils.PlayerUtils.tryFreeChestplateSlot;

public class ElytraMenu extends ElytraBaseMenu {
    private static final int SLOT_FLANC_ETHERE = 22;
    private static final int SLOT_AUTO_GLIDE = 40;
    private static final int SLOT_PROPULSION = 4;
    private static final int SLOT_AMELIORATIONS = 24;
    private static final int SLOT_SILLAGE = 20;

    private final ElytraService elytraService;
    private final PlayerProfileService playerProfileService;
    private final EquipmentService equipmentService;
    private final SrpPlayerService srpPlayerService;

    public ElytraMenu(Player player,
                      ElytraService elytraService,
                      PlayerProfileService playerProfileService,
                      EquipmentService equipmentService,
                      SrpPlayerService srpPlayerService) {
        this(player, elytraService, playerProfileService, equipmentService, srpPlayerService, null);
    }

    public ElytraMenu(Player player,
                      ElytraService elytraService,
                      PlayerProfileService playerProfileService,
                      EquipmentService equipmentService,
                      SrpPlayerService srpPlayerService,
                      Menu previous) {
        super(MenuSize.Five, "Ailes Célestes", previous, player);
        this.elytraService = elytraService;
        this.playerProfileService = playerProfileService;
        this.equipmentService = equipmentService;
        this.srpPlayerService = srpPlayerService;
    }

    @Override
    public void placeButtons(Player viewer) {
        Elytra elytra = getElytra(player);
        boolean autoGlide = elytra != null && elytra.getAutoGlide();

        placeElytraInvokeButton(viewer);
        placeAutoGlideButton(autoGlide, viewer);
        placePropulsionButton(viewer);
        placeUpgradeButton(viewer);
        placeSillageButton(viewer);
        addBackButton();
        addCloseButton();
    }

    private void placeElytraInvokeButton(Player viewer) {
        addButton(SLOT_FLANC_ETHERE, new ItemBuilder(Material.ELYTRA)
                .name("Flanc éthéré", NamedTextColor.LIGHT_PURPLE)
                .lore()
                .append("Invoque ou désinvoque vos ailes divines.", NamedTextColor.GRAY)
                .buildLore()
                .build(), event -> {
            ElytraInvocationResult result = elytraService.getInvocationResult(viewer.getUniqueId());
            switch (result) {
                case NO_ELYTRA:
                    if (!canEquipElytra(viewer)) {
                        return;
                    }
                    elytraService.createElytra(viewer.getUniqueId());
                    invokeElytra(viewer);
                    break;
                case ALREADY_INVOKED:
                    Elytra invokedElytra = getElytra(viewer, true);
                    if (invokedElytra == null) {
                        return;
                    }
                    equipmentService.uninvoke(viewer, invokedElytra);
                    break;
                case READY_TO_INVOKE:
                    if (!canEquipElytra(viewer)) {
                        return;
                    }
                    invokeElytra(viewer);
                    break;
                default:
                    throw new IllegalStateException("Unexpected invocation result: " + result);
            }
            close();
        });
    }

    private boolean canEquipElytra(Player viewer) {
        if (!tryFreeChestplateSlot(viewer)) {
            viewer.closeInventory();
            viewer.sendMessage(ELYTRA_ERROR.append(Component.text(
                    "Ton inventaire est plein, impossible d'invoquer tes Ailes Célestes."
            )));
            return false;
        }
        return true;
    }

    private void invokeElytra(Player viewer) {
        equipmentService.invoke(viewer, Elytra.class, null, true);
        viewer.sendMessage(ELYTRA_SUCCESS.append(Component.text(
                "Tu as invoqué tes Ailes Célestes !"
        )));
    }

    private void placeAutoGlideButton(boolean autoGlide, Player viewer) {
        NamedTextColor autoGlideColor = autoGlide ? NamedTextColor.GREEN : NamedTextColor.RED;
        Material autoGlideMaterial = autoGlide ? Material.LIME_DYE : Material.GRAY_DYE;
        addButton(SLOT_AUTO_GLIDE, new ItemBuilder(autoGlideMaterial)
                .name("Vol automatique", NamedTextColor.AQUA)
                .lore()
                .append("Tombez dans le vide pour voler automatiquement", NamedTextColor.GRAY)
                .newline()
                .newline()
                .append("Statut: ", NamedTextColor.GRAY)
                .append(autoGlide ? "Activé" : "Désactivé", autoGlideColor)
                .buildLore()
                .build(), event -> {
            elytraService.toggleAutoGlide(viewer.getUniqueId());
            open(viewer);
        });
    }

    private void placePropulsionButton(Player viewer) {
        addButton(SLOT_PROPULSION, new ItemBuilder(Material.FIREWORK_ROCKET)
                .name("Propulsion synergique", NamedTextColor.GOLD)
                .lore()
                .append("Vous concentrez l'énergie pour vous propulser.", NamedTextColor.BLUE)
                .buildLore()
                .build(), event -> {
            if (!elytraService.requestPropulsion(viewer.getUniqueId())) {
                viewer.sendMessage(ELYTRA_ERROR.append(Component.text(
                        "Vous devez invoquer vos ailes pour utiliser cette fonction."
                )));
            } else {
                Elytra elytra = getElytra(viewer, true);
                if (elytra == null) {
                    return;
                }
                elytra.synergicPropulsion(viewer);
            }
            close();
        });
    }

    private void placeUpgradeButton(Player viewer) {
        addButton(SLOT_AMELIORATIONS, new ItemBuilder(Material.PRISMARINE_CRYSTALS)
                .name("Améliorations", NamedTextColor.GOLD)
                .lore()
                .append("Affiche les améliorations disponibles.", NamedTextColor.GRAY)
                .buildLore()
                .build(), event -> new ElytraUpgradeMenu(
                player.getUniqueId(),
                playerProfileService,
                elytraService,
                srpPlayerService,
                this
        ).open(viewer));
    }

    private void placeSillageButton(Player viewer) {
        addButton(SLOT_SILLAGE, new ItemBuilder(Material.NETHER_STAR)
                .name("Sillage des ailes", NamedTextColor.DARK_AQUA)
                .lore()
                .append("Affiche des particules pendant le vol.", NamedTextColor.GRAY)
                .buildLore()
                .build(), event -> new ElytraTrailSelectionMenu(
                viewer.getUniqueId(),
                this
        ).open(viewer));
    }

    private Elytra getElytra(Player viewer) {
        return getElytra(viewer, false);
    }

    private Elytra getElytra(Player viewer, boolean invokedOnly) {
        var equipment = equipmentService.getEquipment(viewer.getUniqueId());
        Elytra elytra = equipment.get(Elytra.class);
        if (elytra == null || invokedOnly && !elytra.isInvoked()) {
            return null;
        }
        return elytra;
    }
}
