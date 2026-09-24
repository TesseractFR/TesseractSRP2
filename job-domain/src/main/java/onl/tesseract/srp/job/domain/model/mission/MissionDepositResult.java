package onl.tesseract.srp.job.domain.model.mission;

/**
 * Result of depositing items for a job mission.
 */
public record MissionDepositResult(int delivered, int remaining) {
}

