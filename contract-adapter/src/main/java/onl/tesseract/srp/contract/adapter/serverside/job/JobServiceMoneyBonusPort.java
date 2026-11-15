package onl.tesseract.srp.contract.adapter.serverside.job;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.port.serverside.JobMoneyBonusPort;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.talent.Bonus;
import onl.tesseract.srp.job.domain.port.userside.JobService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Lit le taux de bonus d'argent du joueur auprès de {@link JobService}. */
@Component
public class JobServiceMoneyBonusPort implements JobMoneyBonusPort {

    private final JobService jobService;

    public JobServiceMoneyBonusPort(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public double getMoneyBonusRate(PlayerId player, ContractClient job, MaterialName material) {
        return Optional.ofNullable(jobService.getBonusValue(new JobName(job.name()), player,
                        new Material(material.value()), Bonus.MONEY))
                .orElse(0.0);
    }
}
