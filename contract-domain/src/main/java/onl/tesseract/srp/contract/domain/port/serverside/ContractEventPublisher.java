package onl.tesseract.srp.contract.domain.port.serverside;

public interface ContractEventPublisher {

    void publish(Object event);
}
