package onl.tesseract.srp

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.lib.TesseractLib
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.persistence.yaml.equipment.EquipmentYamlRepository
import onl.tesseract.srp.controller.command.staff.SrpStaffCommand
import onl.tesseract.srp.domain.campement.AnnexionStickInvocable
import onl.tesseract.srp.repository.yaml.equipment.AnnexionStickSerializer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.io.DefaultResourceLoader

lateinit var PLUGIN_INSTANCE: TesseractSRP

class TesseractSRP : JavaPlugin() {

    private lateinit var springContext: ApplicationContext

    override fun onEnable() {
        // Plugin startup logic
        PLUGIN_INSTANCE = this
        val classLoader = CompoundClassLoader(listOf(classLoader, classLoader.parent, TesseractLib.javaClass.classLoader), classLoader.parent)
        val resourceLoader = DefaultResourceLoader(classLoader)
        Thread.currentThread().contextClassLoader = classLoader
        val app = SpringApplication(resourceLoader, TesseractSRPSpringApp::class.java)
        this.springContext = app.run()
        registerCommands()
        registerListeners()
        registerSerializers()
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
        val campementService = springContext.getBean(CampementService::class.java)
        EquipmentYamlRepository.registerTypeSerializer(
            AnnexionStickInvocable::class.java.simpleName,
            AnnexionStickSerializer(campementService)
        )
    }


    override fun onDisable() {
        // Plugin shutdown logic
    }
}
