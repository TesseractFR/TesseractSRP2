package onl.tesseract.srp.config

import onl.tesseract.core.Config
import onl.tesseract.srp.repository.hibernate.SrpPlayerEntity
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings
import org.hibernate.cfg.Configuration
import org.hibernate.service.ServiceRegistry


object HibernateConfig {
    val sessionFactory: SessionFactory = buildSessionFactory()

    fun buildSessionFactory(): SessionFactory {
        try {
            val configuration = setConfiguration(Config())

            // Enregistrer les classes d'entité
            configuration.addAnnotatedClass(SrpPlayerEntity::class.java)
            val serviceRegistry: ServiceRegistry? =
                StandardServiceRegistryBuilder().applySettings(configuration.properties).build()
            return configuration.buildSessionFactory(serviceRegistry)
        } catch (ex: Exception) {
            System.err.println("Initial SessionFactory creation failed.$ex")
            throw ExceptionInInitializerError(ex)
        }
    }

    fun setConfiguration(config: Config): Configuration {
        val configuration = Configuration()
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver")
        configuration.setProperty(
            AvailableSettings.JAKARTA_JDBC_URL,
            "jdbc:mysql://" + config.dbHost + ":" + config.dbPort + "/" + config.dbDatabase
        )
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_USER, config.dbUsername)
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, config.dbPassword)

        // Enable Hibernate's automatic session context management
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread")

        // Echo all executed SQL to stdout
        configuration.setProperty(AvailableSettings.SHOW_SQL, "false")
        configuration.setProperty(AvailableSettings.FORMAT_SQL, "true")

        configuration.setProperty(AvailableSettings.HBM2DDL_AUTO, "update")
        return configuration
    }
}
