package onl.tesseract.srp.common.adapter.userside.config;

import onl.tesseract.srp.common.domain.port.serverside.TeleportationPort;
import onl.tesseract.srp.common.domain.port.serverside.TeleportationSettingsPort;
import onl.tesseract.srp.common.domain.port.userside.TeleportationService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TeleportationServiceProvider {

    @Bean
    public TeleportationService teleportationService(TeleportationPort teleportationPort,
                                                     TeleportationSettingsPort teleportationSettingsPort) {
        return new TeleportationService(teleportationPort, teleportationSettingsPort);
    }
}
