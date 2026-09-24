package onl.tesseract.srp;

import lombok.SneakyThrows;
import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.core.title.TitleService;
import onl.tesseract.lib.TesseractLib;
import onl.tesseract.lib.persistence.yaml.equipment.EquipmentYamlRepository;
import onl.tesseract.lib.persistence.yaml.equipment.InvocableGenericSerializer;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.controller.command.staff.SrpStaffCommand;
import onl.tesseract.srp.domain.player.PlayerRank;
import onl.tesseract.srp.common.domain.model.world.SrpWorld;
import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer;
import onl.tesseract.srp.service.world.WorldService;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class TesseractSRP extends JavaPlugin {

    public static TesseractSRP PLUGIN_INSTANCE;
    public static final Path TREEPATH = Path.of("plugins");

    public ApplicationContext springContext;

    @SneakyThrows
    @Override
    public void onEnable() {
        PLUGIN_INSTANCE = this;
        CompoundClassLoader classLoader = new CompoundClassLoader(
            java.util.List.of(getClassLoader(), getClassLoader().getParent(), TesseractLib.class.getClassLoader()),
            getClassLoader().getParent());
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader(classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        SpringApplication app = new SpringApplication(resourceLoader, TesseractSRPSpringApp.class);
        app.setDefaultProperties(Map.of("spring.config.location", "classpath:/application.properties"));
        app.addInitializers(applicationContext -> {
            ConfigurableEnvironment env = (ConfigurableEnvironment) applicationContext.getEnvironment();
            var resource = resourceLoader.getResource("application.properties");
            Properties props = new Properties();
            try (var is = resource.getInputStream()) {
                props.load(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            env.getPropertySources().addFirst(new PropertiesPropertySource("customProperties", props));
        });
        this.springContext = app.run();
        Config config = this.springContext.getBean(Config.class);
        copyConfigsIfAbsent(config.forceUpdateConfig());
        registerCommands();
        registerListeners();
        registerSerializers();
        registerTitles();
        checkWorldsExist();
        getLogger().info("Tesseract SRP enabled, Spring context enabled");
    }

    private void registerListeners() {
        Map<String, Listener> beans = this.springContext.getBeansOfType(Listener.class);
        for (Map.Entry<String, Listener> entry : beans.entrySet()) {
            Listener bean = entry.getValue();
            this.getServer().getPluginManager().registerEvents(bean, this);
        }
    }

    public void registerCommands() {
        SrpCommandInstanceProvider provider = this.springContext.getBean(SrpCommandInstanceProvider.class);
        new SrpStaffCommand(provider).register(this, "staffSrp");
        Map<String, CommandContext> beans = this.springContext.getBeansOfType(CommandContext.class);
        for (Map.Entry<String, CommandContext> entry : beans.entrySet()) {
            CommandContext bean = entry.getValue();
            bean.register(this, bean.getCommandDefinition().getName());
        }
    }

    private void registerSerializers() {
        Map<String, SrpInvocableSerializer> serializers = this.springContext.getBeansOfType(SrpInvocableSerializer.class);
        for (SrpInvocableSerializer serializer : serializers.values()) {
            EquipmentYamlRepository.INSTANCE.registerTypeSerializer(
                serializer.getTypeKey(),
                (InvocableGenericSerializer<?>) serializer);
        }
    }

    private void registerTitles() {
        TitleService titleService = ServiceContainer.getInstance().get(TitleService.class);
        for (PlayerRank rank : PlayerRank.values()) {
            titleService.save(rank.getTitle());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void checkWorldsExist() {
        WorldService worldService = this.springContext.getBean(WorldService.class);
        for (SrpWorld world : SrpWorld.values()) {
            worldService.getBukkitWorld(world);
        }
    }

    private void copyConfigsIfAbsent(boolean forceUpdateConfig) throws URISyntaxException {
        if (!Files.exists(TREEPATH)) {
            try {
                Files.createDirectories(TREEPATH);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
        var uri = getClass().getResource("/Tesseract").toURI();
        if (uri == null) return;
        try (var fs = FileSystems.newFileSystem(uri, Map.of())) {
            var path = fs.getPath("Tesseract");
            Files.walk(path).forEach(x -> {
                try {
                    Path outPath = TREEPATH.resolve(x.toString());
                    if (forceUpdateConfig && Files.exists(outPath) && Files.isRegularFile(outPath)) {
                        Files.deleteIfExists(outPath);
                    }
                    if (!Files.exists(outPath)) {
                        Files.copy(x, outPath);
                    }
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}

