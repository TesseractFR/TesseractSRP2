package onl.tesseract.srp.contract.adapter.serverside.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_contract_player_state")
public class ContractTicketEntity {
    @Id
    @Column(name = "player_id", columnDefinition = "BINARY(16)")
    private UUID playerId;

    @Column(nullable = false)
    private int tickets;
}