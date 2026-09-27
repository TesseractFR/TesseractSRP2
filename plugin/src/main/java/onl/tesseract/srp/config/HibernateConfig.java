package onl.tesseract.srp.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import onl.tesseract.srp.Config;
import onl.tesseract.srp.repository.hibernate.SrpPlayerEntity;

public final class HibernateConfig {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private HibernateConfig() {}

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = setConfiguration(Config.getInstance());
            configuration.addAnnotatedClass(SrpPlayerEntity.class);
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();
            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static Configuration setConfiguration(Config config) {
        Configuration configuration = new Configuration();
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.postgresql.Driver");
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_URL,
            "jdbc:postgresql://" + config.srpDbHost() + ":" + config.srpDbPort() + "/" + config.srpDbDatabase());
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_USER, config.srpDbUsername());
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, config.srpDbPassword());
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        configuration.setProperty(AvailableSettings.SHOW_SQL, "false");
        configuration.setProperty(AvailableSettings.FORMAT_SQL, "true");
        configuration.setProperty(AvailableSettings.HBM2DDL_AUTO, "update");
        return configuration;
    }
}

