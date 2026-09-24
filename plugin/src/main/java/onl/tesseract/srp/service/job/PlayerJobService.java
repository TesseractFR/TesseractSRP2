package onl.tesseract.srp.service.job;

import onl.tesseract.srp.common.adapter.DomainEventPublisher;
import onl.tesseract.srp.domain.job.EnumJob;
import onl.tesseract.srp.domain.job.JobSkill;
import onl.tesseract.srp.domain.job.PlayerJobProgression;
import onl.tesseract.srp.repository.generic.job.PlayerJobProgressionRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PlayerJobService {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PlayerJobService.class);

    private final PlayerJobProgressionRepository repository;
    private final DomainEventPublisher eventService;
    private final Map<UUID, Integer> lootBatches = new HashMap<>();

    public PlayerJobService(PlayerJobProgressionRepository repository, DomainEventPublisher eventService) {
        this.repository = repository;
        this.eventService = eventService;
    }

    public PlayerJobProgression getPlayerJobProgression(UUID playerID) {
        return repository.getById(playerID) != null ? repository.getById(playerID) : new PlayerJobProgression(playerID);
    }

    /**
     * Try to unlock a skill and emit a PlayerJobSkillUnlockedEvent if successfully unlocked
     * @return True if the skill was unlocked.
     */
    public boolean unlockSkill(UUID playerID, JobSkill skill) {
        PlayerJobProgression progression = getPlayerJobProgression(playerID);
        boolean added = progression.addSkill(skill);
        if (added) {
            repository.save(progression);
            logger.info("Player " + playerID + " has unlocked skill " + skill);
            eventService.publish(new PlayerJobSkillUnlockedEvent(playerID, skill));
        }
        return added;
    }

    /**
     * Register a loot by a player that generated xpLoot points of xp. A subsequent call to processLootBatches will
     * effectively give the cumulated xp to players.
     */
    public void registerLoot(UUID playerID, int xpLoot) {
        lootBatches.put(playerID, lootBatches.getOrDefault(playerID, 0) + xpLoot);
    }

    /**
     * Give all registered xp loot to players and save them.
     */
    @Transactional
    public void processLootBatches() {
        for (Map.Entry<UUID, Integer> entry : lootBatches.entrySet()) {
            addXp(entry.getKey(), entry.getValue());
        }
        lootBatches.clear();
    }

    public void addXp(UUID playerID, int amount) {
        PlayerJobProgression progression = getPlayerJobProgression(playerID);
        int passedLevel = progression.addXp(amount);
        if (passedLevel > 0) eventService.publish(new PlayerLevelUpEvent(playerID, progression.getLevel(), passedLevel));
        savePlayerProgression(progression);
    }

    public void addLevel(UUID playerID, int amount) {
        PlayerJobProgression progression = getPlayerJobProgression(playerID);
        progression.addLevel(amount);
        if (amount > 0) eventService.publish(new PlayerLevelUpEvent(playerID, progression.getLevel(), amount));
        savePlayerProgression(progression);
    }

    /**
     * Save the player and emit events
     */
    private void savePlayerProgression(PlayerJobProgression playerJobProgression) {
        repository.save(playerJobProgression);
    }

    public void clearXp(UUID playerID) {
        PlayerJobProgression progression = getPlayerJobProgression(playerID);
        progression.addXp(-progression.getXp());
        repository.save(progression);
    }

    public void addSkillPoint(UUID playerID, int points) {
        PlayerJobProgression progression = getPlayerJobProgression(playerID);
        progression.addSkillPoints(points);
        repository.save(progression);
    }

    @Transactional
    public double increaseReputation(UUID playerId, EnumJob job, double amount) {
        PlayerJobProgression progression = getPlayerJobProgression(playerId);
        Double newValue = progression.getReputationByJob().computeIfAbsent(job, k -> 1.0);
        newValue += amount;
        progression.getReputationByJob().put(job, newValue);
        repository.save(progression);
        return newValue;
    }

    @Transactional
    public double increaseReputation(UUID playerId, EnumJob job) {
        return increaseReputation(playerId, job, 0.005);
    }
}

