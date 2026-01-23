package onl.tesseract.srp.repository.generic.player

import onl.tesseract.srp.domain.player.SrpPlayer
import java.util.UUID

interface SrpPlayerRepository : onl.tesseract.lib.repository.Repository<SrpPlayer, UUID>
