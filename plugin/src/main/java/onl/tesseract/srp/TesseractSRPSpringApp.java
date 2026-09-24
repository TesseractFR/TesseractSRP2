package onl.tesseract.srp;

import jakarta.persistence.EntityManagerFactory;
import org.bukkit.Bukkit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@SpringBootApplication(scanBasePackages = "onl.tesseract.srp")
@EnableJpaRepositories(value = "onl.tesseract.srp", entityManagerFactoryRef = "defaultEntityManagerFactory",
    transactionManagerRef = "defaultTransactionManager")
@EnableScheduling
@EnableCaching
public class TesseractSRPSpringApp {

    @Bean
    public Config srpConfig() {
        return new Config();
    }

    @Bean("defaultDataSource")
    @Primary
    public DataSource defaultDataSource(Config config) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" + config.srpDbHost() + ":" + config.srpDbPort() + "/" + config.srpDbDatabase());
        dataSource.setUsername(config.srpDbUsername());
        dataSource.setPassword(config.srpDbPassword());
        return dataSource;
    }

    @Bean("defaultEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean defaultEntityManagerFactory(
        @Qualifier("defaultDataSource") DataSource ds) {
        LocalContainerEntityManagerFactoryBean build = new LocalContainerEntityManagerFactoryBean();
        build.setDataSource(ds);
        build.setPackagesToScan("onl.tesseract.srp");
        build.setPersistenceUnitName("default");
        build.setEntityManagerFactoryInterface(EntityManagerFactory.class);
        build.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        jpaProperties.setProperty("spring.jpa.show-sql", "true");
        jpaProperties.setProperty("hibernate.show_sql", "true");
        jpaProperties.setProperty("logging.level.org.hibernate.SQL", "DEBUG");
        jpaProperties.setProperty("logging.level.org.hibernate.type.descriptor.sql.BasicBinder", "TRACE");
        jpaProperties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
        jpaProperties.setProperty("hibernate.generate_statistics", "false");
        jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "true");
        jpaProperties.setProperty("hibernate.cache.use_query_cache", "true");
        jpaProperties.setProperty("hibernate.javax.cache.missing_cache_strategy", "create");
        jpaProperties.setProperty("hibernate.javax.cache.provider", "com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider");
        build.setJpaProperties(jpaProperties);
        return build;
    }

    @Bean("bukkitScheduler")
    public TaskScheduler bukkitScheduler() {
        return new BukkitTaskScheduler(TesseractSRP.PLUGIN_INSTANCE, Bukkit.getScheduler());
    }

    @Bean("defaultTransactionManager")
    @Primary
    public PlatformTransactionManager defaultTransactionManager(
        @Qualifier("defaultEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

