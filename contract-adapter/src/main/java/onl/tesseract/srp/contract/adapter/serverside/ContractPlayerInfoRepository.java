package onl.tesseract.srp.contract.adapter.serverside;

import onl.tesseract.srp.common.domain.port.userside.PlayerService;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractTicketEntity;
import onl.tesseract.srp.contract.adapter.serverside.jpa.repository.ContractTicketJpaRepository;
import onl.tesseract.srp.contract.adapter.serverside.yaml.YamlContractConfigRepository;
import onl.tesseract.srp.contract.domain.model.PlayerID;
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

    private final ContractTicketJpaRepository ticketJpaRepo;
    private final PlayerService playerService;
    private final JobService jobService;
    private final YamlContractConfigRepository config;

    public ContractPlayerInfoRepository(ContractTicketJpaRepository ticketJpaRepo, PlayerService playerService, JobService jobService,
                                        YamlContractConfigRepository config) {
        this.ticketJpaRepo = ticketJpaRepo;
        this.playerService = playerService;
        this.jobService = jobService;
        this.config = config;
    }

    @Override
    @Transactional(readOnly = true)
    public OptionalInt findTickets(PlayerID player) {
        return ticketJpaRepo.findById(player.value())
                .map(entity -> OptionalInt.of(entity.getTickets()))
                .orElse(OptionalInt.empty());
    }

    @Override
    @Transactional
    public void saveTickets(PlayerID player, int tickets) {
        ticketJpaRepo.save(new ContractTicketEntity(player.value(), tickets));
    }

    @Override
    public int getDailyTickets(PlayerID player) {
        var rank = playerService.getPlayer(player.value()).getRank();
        return rank == null ? 0 : config.getDailyTickets(rank.name());
    }

    @Override
    public double getMoneyBonus(PlayerID player, String job, String material) {
        return jobService.getBonusValue(
                new JobName(job),
                new onl.tesseract.srp.job.domain.model.PlayerID(player.value()),
                new Material(material),
                Bonus.MONEY);
    }
}