package onl.tesseract.srp.domain.job;

import onl.tesseract.srp.domain.item.CustomMaterial;
import java.util.Collection;
import java.util.Map;

public class Job {
    private final EnumJob enumJob;
    private final Map<CustomMaterial, BaseStat> baseStats;
    private final Collection<MissionTemplate> missionTemplates;

    public Job(EnumJob enumJob, Map<CustomMaterial, BaseStat> baseStats, Collection<MissionTemplate> missionTemplates) {
        this.enumJob = enumJob;
        this.baseStats = baseStats;
        this.missionTemplates = missionTemplates;
    }

    public EnumJob getEnumJob() { return enumJob; }
    public Map<CustomMaterial, BaseStat> getBaseStats() { return baseStats; }
    public Collection<MissionTemplate> getMissionTemplates() { return missionTemplates; }

    public Collection<CustomMaterial> getMaterials() {
        return baseStats.keySet();
    }
}

