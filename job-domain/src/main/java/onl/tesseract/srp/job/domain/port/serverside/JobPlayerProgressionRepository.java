package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;

public interface JobPlayerProgressionRepository {
    PlayerJobProgression getPlayerJobProgression(PlayerID player, JobName jobName);

}
