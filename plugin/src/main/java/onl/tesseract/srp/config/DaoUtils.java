package onl.tesseract.srp.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DaoUtils {
    private static final Logger log = LoggerFactory.getLogger(DaoUtils.class);

    private DaoUtils() {}

    public static void executeInsideTransaction(java.util.function.Consumer<Session> action) {
        Transaction transaction = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            action.accept(session);
            transaction.commit();
        } catch (Exception e) {
            log.error("Error executing inside transaction", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public static <T> java.util.List<T> loadAll(Class<T> type, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(type);
        criteria.from(type);
        return session.createQuery(criteria).getResultList();
    }

    public static void executeInsideJpaTransaction(java.util.function.Consumer<EntityManager> action) {
        executeInsideTransaction(session -> action.accept(session));
    }
}

