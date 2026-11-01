package onl.tesseract.srp.contract.adapter.serverside;

import onl.tesseract.srp.common.domain.port.userside.PlayerService;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractTicketEntity;
import onl.tesseract.srp.contract.adapter.serverside.jpa.repository.ContractTicketJpaRepository;
import onl.tesseract.srp.contract.adapter.serverside.yaml.YamlContractConfigRepository;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInfoRepository;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.talent.Bonus;
import onl.tesseract.srp.job.domain.port.userside.JobService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.OptionalInt;

@Component
public class ContractPlayerInfoRepository implements PlayerInfoRepository {

    private final ContractTicketJpaRepository contractTicketJpaRepository;
    private final PlayerService playerService;
    private final JobService jobService;
    private final YamlContractConfigRepository yamlContractConfigRepository;

    public ContractPlayerInfoRepository(ContractTicketJpaRepository contractTicketJpaRepository, PlayerService playerService, JobService jobService,
                                        YamlContractConfigRepository yamlContractConfigRepository) {
        this.contractTicketJpaRepository = contractTicketJpaRepository;
        this.playerService = playerService;
        this.jobService = jobService;
        this.yamlContractConfigRepository = yamlContractConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OptionalInt findTickets(PlayerId player) {
        return contractTicketJpaRepository.findById(player.value().toString())
                .map(entity -> OptionalInt.of(entity.getTickets()))
                .orElse(OptionalInt.empty());
    }

    @Override
    @Transactional
    public void saveTickets(PlayerId player, int tickets) {
        contractTicketJpaRepository.save(new ContractTicketEntity(player.value().toString(), tickets));
    }

    @Override
    public int getDailyQuota(PlayerId player) {
        var rank = playerService.getPlayer(player.value()).getRank();
        return rank == null ? 0 : yamlContractConfigRepository.getDailyTickets(rank.name());
    }

    @Override
    public double getMoneyBonus(PlayerId player, String job, String material) {
        return jobService.getBonusValue(
                new JobName(job),
                new PlayerId(player.value()),
                new Material(material),
                Bonus.MONEY);
    }
}