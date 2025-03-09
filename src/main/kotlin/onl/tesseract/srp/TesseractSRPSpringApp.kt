package onl.tesseract.srp

import onl.tesseract.core.Config
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
import java.util.*
import javax.sql.DataSource


@SpringBootApplication(scanBasePackages = ["onl.tesseract.srp"])
@EnableJpaRepositories("onl.tesseract.srp.repository.hibernate", entityManagerFactoryRef = "defaultEntityManagerFactory")
open class TesseractSRPSpringApp {

    @Bean
    open fun coreConfig(): Config = Config()

    @Bean(name = ["defaultDataSource"])
    @Primary
    open fun defaultDataSource(config: Config): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver")
        dataSource.url = "jdbc:mysql://${config.dbHost}:3306/srp"
        dataSource.username = config.dbUsername
        dataSource.password = config.dbPassword
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
        build.setJpaProperties(jpaProperties)
        return build
    }
}