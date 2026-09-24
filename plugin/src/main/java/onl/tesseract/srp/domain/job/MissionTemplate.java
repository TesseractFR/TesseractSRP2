package onl.tesseract.srp.domain.job;

import java.util.List;

public class MissionTemplate {
    private final List<MissionItem> items;

    public MissionTemplate(List<MissionItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items cannot be empty");
        }
        this.items = items;
    }

    public List<MissionItem> getItems() { return items; }
}

