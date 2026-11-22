package onl.tesseract.srp.contract.adapter.serverside.job;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.port.serverside.JobMoneyBonusPort;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.talent.Bonus;
import onl.tesseract.srp.job.domain.port.userside.JobTalentBonusService;
import org.springframework.stereotype.Component;

/** Lit le taux de bonus d'argent que le joueur tire des talents de son métier, via {@link JobTalentBonusService}. */
@Component
public class JobServiceMoneyBonusPort implements JobMoneyBonusPort {

    private final JobTalentBonusService jobTalentBonusService;

    public JobServiceMoneyBonusPort(JobTalentBonusService jobTalentBonusService) {
        this.jobTalentBonusService = jobTalentBonusService;
    }

    @Override
    public double getMoneyBonusRate(PlayerId player, ContractClient job, MaterialName material) {
        return jobTalentBonusService.getBonusValue(new JobName(job.name()), player,
                new Material(material.value()), Bonus.MONEY);
    }
}
