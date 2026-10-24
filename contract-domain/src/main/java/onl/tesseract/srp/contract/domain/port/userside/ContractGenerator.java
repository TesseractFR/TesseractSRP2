package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.profile.JobProfile;
import onl.tesseract.srp.contract.domain.model.profile.PlayerContractProfile;
import onl.tesseract.srp.contract.domain.model.profile.UnlockedMaterial;
import onl.tesseract.srp.contract.domain.model.settings.ContractSettings;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import onl.tesseract.srp.contract.domain.port.serverside.RandomSource;
import onl.tesseract.srp.contract.domain.port.serverside.RewardCalculator;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ContractGenerator {

    private final ContractConfigRepository config;
    private final RewardCalculator rewards;
    private final RandomSource random;
    private final Clock clock;

    public ContractGenerator(ContractConfigRepository config, RewardCalculator rewards, RandomSource random,
                             Clock clock) {
        this.config = config;
        this.rewards = rewards;
        this.random = random;
        this.clock = clock;
    }

    public Optional<Contract> generate(PlayerID buyer, PlayerContractProfile profile) {
        List<JobProfile> jobs = profile.jobs().stream().filter(j -> !usable(j).isEmpty()).toList();
        if (jobs.isEmpty()) {
            return Optional.empty();
        }
        ContractSettings settings = config.current();
        JobProfile job = pickJob(jobs);
        List<UnlockedMaterial> materials = usable(job);
        UnlockedMaterial material = materials.get(random.nextInt(materials.size()));

        int quantity = quantityFor(settings, material);
        Quality minimal = Quality.values()[random.nextInt(job.maxQuality().ordinal() + 1)];
        return Optional.of(build(buyer, job, material, quantity, minimal, settings));
    }

    public Optional<Contract> generateDiscovery(PlayerID buyer, PlayerContractProfile profile) {
        ContractSettings settings = config.current();
        Optional<JobProfile> job = profile.jobs().stream().filter(j -> !usable(j).isEmpty())
                .min(Comparator.comparingInt(j -> usable(j).stream().mapToInt(UnlockedMaterial::tier).min().orElse(0)));
        if (job.isEmpty()) {
            return Optional.empty();
        }
        UnlockedMaterial material = usable(job.get()).stream()
                .min(Comparator.comparingInt(UnlockedMaterial::tier)).orElseThrow();
        int quantity = Math.clamp(settings.discoveryQuantity(), 1, maxQuantity(settings));
        return Optional.of(build(buyer, job.get(), material, quantity, Quality.POOR, settings));
    }

    private Contract build(PlayerID buyer, JobProfile job, UnlockedMaterial material, int quantity,
                           Quality minimal, ContractSettings settings) {
        Instant now = clock.instant();
        Contract draft = new Contract(null, buyer, ContractType.HARVEST, job.job(), material.material(),
                quantity, minimal, 0, material.tier(), now, now.plus(settings.scrollTtl()), 0L);
        return draft.withReward(rewards.reward(draft));
    }

    private List<UnlockedMaterial> usable(JobProfile job) {
        List<UnlockedMaterial> result = new ArrayList<>();
        for (UnlockedMaterial m : job.materials()) {
            if (m.lootChance() > 0 && m.harvestsPerHour() > 0) {
                result.add(m);
            }
        }
        return result;
    }

    private JobProfile pickJob(List<JobProfile> jobs) {
        double total = 0;
        for (JobProfile j : jobs) {
            total += Math.max(1, j.level());
        }
        double r = random.nextDouble() * total;
        for (JobProfile j : jobs) {
            r -= Math.max(1, j.level());
            if (r < 0) {
                return j;
            }
        }
        return jobs.getLast();
    }

    int quantityFor(ContractSettings settings, UnlockedMaterial material) {
        double raw = settings.effortHours() * material.harvestsPerHour() * material.lootChance();
        long rounded = Math.round(raw);
        return Math.clamp(rounded, 1, maxQuantity(settings));
    }

    private int maxQuantity(ContractSettings settings) {
        return settings.maxStacks() * settings.stackSize();
    }
}
