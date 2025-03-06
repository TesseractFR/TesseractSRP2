package onl.tesseract.srp

import org.bukkit.plugin.java.JavaPlugin
import org.springframework.boot.SpringApplication
import org.springframework.core.io.DefaultResourceLoader

class TesseractSRP : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        val resourceLoader = DefaultResourceLoader(classLoader)
        Thread.currentThread().contextClassLoader = classLoader
        val context = SpringApplication(resourceLoader, TesseractSRPSpringApp::class.java)
        context.run()
        logger.info("Tesseract SRP enabled, Spring context enabled")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
