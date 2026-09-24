package onl.tesseract.srp.repository.yaml.equipment.annexionStick;

import onl.tesseract.lib.persistence.yaml.equipment.InvocableGenericSerializer;
import onl.tesseract.srp.util.equipment.annexionStick.AnnexionStickInvocable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.UUID;

public abstract class AnnexionStickSerializer<T extends AnnexionStickInvocable> extends InvocableGenericSerializer<T> {

    protected abstract T factory(UUID uuid, boolean invoked, int handSlot);

    @Override
    public YamlConfiguration serialize(T value) {
        YamlConfiguration yaml = new YamlConfiguration();
        writeGenericProps(yaml, value);
        return yaml;
    }

    @Override
    public T deserialize(ConfigurationSection yaml) {
        UUID uuid = parsePlayerID(yaml);
        boolean invoked = parseInvoked(yaml);
        int handSlot = parseHandSlot(yaml);

        return factory(uuid, invoked, handSlot);
    }

}

