package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "t_srp_player")
class SrpPlayerEntity(
    @Id
    var uuid: UUID
)