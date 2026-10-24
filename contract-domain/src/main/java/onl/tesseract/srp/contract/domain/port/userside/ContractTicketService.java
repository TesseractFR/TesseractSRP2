package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.settings.ContractSettings;
import onl.tesseract.srp.contract.domain.model.ticket.ContractPlayerState;
import onl.tesseract.srp.contract.domain.model.ticket.TicketSource;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractPlayerStateRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerProfileRepository;

import java.time.Clock;
import java.time.LocalDate;

/** Stock quotidien de tickets de contrat. */
public class ContractTicketService {

    private final ContractPlayerStateRepository states;
    private final PlayerProfileRepository profiles;
    private final ContractConfigRepository config;
    private final Clock clock;

    public ContractTicketService(ContractPlayerStateRepository states, PlayerProfileRepository profiles,
                                 ContractConfigRepository config, Clock clock) {
        this.states = states;
        this.profiles = profiles;
        this.config = config;
        this.clock = clock;
    }

    public int ticketsOf(PlayerID player) {
        return currentState(player).tickets();
    }

    /**
     * État du joueur avec le renouvellement quotidien appliqué et enregistré. Le stock de la veille n'est pas
     * reporté : le renouvellement remet le stock au quota quotidien du grade.
     */
    public ContractPlayerState currentState(PlayerID player) {
        ContractPlayerState stored = states.find(player);
        ContractPlayerState renewed = stored.renewedFor(today(), profiles.dailyTickets(player));
        if (!renewed.equals(stored)) {
            states.save(renewed);
        }
        return renewed;
    }

    /**
     * Ajoute un ticket gagné en jeu. Refusé quand le stock a déjà atteint le quota plus le plafond bonus,
     * sauf pour le staff ({@link TicketSource#ADMIN}).
     */
    public boolean grantTicket(PlayerID player, TicketSource source) {
        ContractPlayerState state = currentState(player);
        ContractSettings settings = config.current();
        int limit = profiles.dailyTickets(player) + settings.bonusCap();
        if (source != TicketSource.ADMIN && state.tickets() >= limit) {
            return false;
        }
        states.save(state.withTickets(state.tickets() + 1));
        return true;
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
