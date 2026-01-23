package onl.tesseract.srp.repository.generic.job

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.job.PlayerJobProgression
import java.util.UUID

interface PlayerJobProgressionRepository : Repository<PlayerJobProgression, UUID>
