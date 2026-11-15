package onl.tesseract.srp.contract.adapter.serverside.yaml;

/** Configuration du nombre de tickets de contrat attribués chaque jour selon le grade. */
public interface DailyTicketConfig {
    /** @return nombre de tickets quotidiens du grade, zéro s'il n'est pas configuré */
    int getDailyTickets(String rankName);
}
