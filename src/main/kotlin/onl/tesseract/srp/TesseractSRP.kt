package onl.tesseract.srp

import onl.tesseract.srp.controller.command.staff.SrpStaffCommand
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.core.io.DefaultResourceLoader

class TesseractSRP : JavaPlugin() {

    private lateinit var springContext: ApplicationContext

    override fun onEnable() {
        // Plugin startup logic
        val resourceLoader = DefaultResourceLoader(classLoader)
        Thread.currentThread().contextClassLoader = classLoader
        val app = SpringApplication(resourceLoader, TesseractSRPSpringApp::class.java)
        this.springContext = app.run()
        registerCommands()
        logger.info("Tesseract SRP enabled, Spring context enabled")
    }

    fun registerCommands() {
        val provider = springContext.getBean(SrpCommandInstanceProvider::class.java)
        SrpStaffCommand(provider).register(this, "staffSrp")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
