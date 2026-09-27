package onl.tesseract.srp.common.adapter.userside.config;

import onl.tesseract.srp.common.domain.port.userside.world.WorldService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WorldServiceProvider {

    @Bean
    public WorldService worldService() {
        return new WorldService();
    }

}
