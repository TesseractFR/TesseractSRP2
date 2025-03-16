package onl.tesseract.srp

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.lib.TesseractLib
import onl.tesseract.srp.controller.command.staff.SrpStaffCommand
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

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
