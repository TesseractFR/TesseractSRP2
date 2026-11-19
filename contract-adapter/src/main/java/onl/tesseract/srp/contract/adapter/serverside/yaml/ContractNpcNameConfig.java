package onl.tesseract.srp.contract.adapter.serverside.yaml;

import onl.tesseract.srp.contract.domain.model.contract.ContractClient;

import java.util.Optional;

/** Configuration des noms affichés des PNJ destinataires des contrats. */
public interface ContractNpcNameConfig {
    /** @return le nom du PNJ du destinataire, ou {@link Optional#empty()} s'il n'est pas configuré */
    Optional<String> findNpcName(ContractClient client);
}
