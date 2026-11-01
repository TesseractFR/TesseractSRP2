package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;

public interface JobPlayerProgressionRepository {
    PlayerJobProgression getPlayerJobProgression(PlayerId player, JobName jobName);

}
