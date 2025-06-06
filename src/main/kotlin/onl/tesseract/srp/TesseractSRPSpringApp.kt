package onl.tesseract.srp

import org.bukkit.Bukkit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.*
import javax.sql.DataSource


@SpringBootApplication(scanBasePackages = ["onl.tesseract.srp"])
@EnableJpaRepositories("onl.tesseract.srp.repository.hibernate", entityManagerFactoryRef = "defaultEntityManagerFactory")
@EnableScheduling
open class TesseractSRPSpringApp {

    @Bean
    open fun srpConfig(): Config = Config()

    @Bean(name = ["defaultDataSource"])
    @Primary
    open fun defaultDataSource(config: Config): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver")
        dataSource.url = "jdbc:mysql://${config.srpDbHost}:${config.srpDbPort}/${config.srpDbDatabase}"
        dataSource.username = config.srpDbUsername
        dataSource.password = config.srpDbPassword
        return dataSource
    }

    @Bean(name = ["defaultEntityManagerFactory"])
    @Primary
    @Autowired
    open fun defaultEntityManagerFactory(
        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
        @Qualifier("defaultDataSource") ds: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        val build = entityManagerFactoryBuilder
            .dataSource(ds)
            .packages("onl.tesseract.srp.repository.hibernate")
            .persistenceUnit("default")
            .build()
        build.entityManagerInterface = null
        build.jpaVendorAdapter = HibernateJpaVendorAdapter()
        val jpaProperties = Properties()
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update")
        jpaProperties.setProperty("spring.jpa.show-sql", "true")
        jpaProperties.setProperty("hibernate.show_sql", "true")
        jpaProperties.setProperty("logging.level.org.hibernate.SQL", "DEBUG")
        jpaProperties.setProperty("logging.level.org.hibernate.type.descriptor.sql.BasicBinder", "TRACE")
        jpaProperties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory")
        jpaProperties.setProperty("hibernate.generate_statistics", "false")
        jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "true")
        jpaProperties.setProperty("hibernate.cache.use_query_cache", "true")
        build.setJpaProperties(jpaProperties)
        return build
    }


    @Bean(name = ["bukkitScheduler"])
    open fun bukkitScheduler(): TaskScheduler {
        return BukkitTaskScheduler(PLUGIN_INSTANCE, Bukkit.getScheduler())
    }
}