package onl.tesseract.srp.repository.hibernate.player

import onl.tesseract.srp.repository.hibernate.SrpPlayerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SrpPlayerJpaRepository : JpaRepository<SrpPlayerEntity, UUID>