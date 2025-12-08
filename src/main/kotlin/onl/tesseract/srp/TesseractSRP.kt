package onl.tesseract.srp

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.core.title.TitleService
import onl.tesseract.lib.TesseractLib
import onl.tesseract.lib.persistence.yaml.equipment.EquipmentYamlRepository
import onl.tesseract.lib.persistence.yaml.equipment.InvocableGenericSerializer
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.controller.command.staff.SrpStaffCommand
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer
import onl.tesseract.srp.service.world.WorldService
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.DefaultResourceLoader
import java.util.*

lateinit var PLUGIN_INSTANCE: TesseractSRP

class TesseractSRP : JavaPlugin() {

    lateinit var springContext: ApplicationContext

    override fun onEnable() {
        // Plugin startup logic
        PLUGIN_INSTANCE = this
        val classLoader = CompoundClassLoader(listOf(classLoader, classLoader.parent, TesseractLib.javaClass.classLoader), classLoader.parent)
        val resourceLoader = DefaultResourceLoader(classLoader)
        Thread.currentThread().contextClassLoader = classLoader
        val app = SpringApplication(resourceLoader, TesseractSRPSpringApp::class.java)
        app.setDefaultProperties(mapOf("spring.config.location" to "classpath:/application.properties"))
        app.addInitializers({ applicationContext ->
            val env = applicationContext.environment as ConfigurableEnvironment
            val resource = resourceLoader.getResource("application.properties")
            val props = Properties().apply {
                resource.inputStream.use { load(it) }
            }
            env.propertySources.addFirst(PropertiesPropertySource("customProperties", props))
        })
        this.springContext = app.run()
        registerCommands()
        registerListeners()
        registerSerializers()
        registerTitles()
        checkWorldsExist()
        logger.info("Tesseract SRP enabled, Spring context enabled")
    }

    private fun registerListeners() {
        springContext.getBeansOfType(Listener::class.java)
            .forEach { (_, bean) -> this.server.pluginManager.registerEvents(bean, this) }
    }

    fun registerCommands() {
        val provider = springContext.getBean(SrpCommandInstanceProvider::class.java)
        SrpStaffCommand(provider).register(this, "staffSrp")
        springContext.getBeansOfType(CommandContext::class.java)
            .forEach { (_, bean) -> bean.register(this, bean.commandDefinition.name) }
    }

    private fun registerSerializers() {
        val serializers = springContext.getBeansOfType(SrpInvocableSerializer::class.java)
        serializers.values.forEach { serializer ->
            EquipmentYamlRepository.registerTypeSerializer(
                serializer.typeKey,
                serializer as InvocableGenericSerializer<*>
            )
        }
    }

    private fun registerTitles() {
        val titleService = ServiceContainer[TitleService::class.java]
        PlayerRank.entries.map { it.title }
            .forEach { titleService.save(it) }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun checkWorldsExist() {
        val worldService = springContext.getBean(WorldService::class.java)
        SrpWorld.entries.forEach { worldService.getBukkitWorld(it) }
    }
}
