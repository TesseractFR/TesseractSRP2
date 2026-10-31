package onl.tesseract.srp.contract.adapter.serverside.jpa.repository;

import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractTicketJpaRepository extends JpaRepository<ContractTicketEntity, String> {
}