package eu.drus.jpa.unit.cdi;

import javax.persistence.EntityManager;

public class EntityManagerHolder {

    private static EntityManagerHolder instance;

    private EntityManager entityManager;

    private EntityManagerHolder() {}

    public static EntityManagerHolder getInstance() {
        if (instance == null) {
            instance = new EntityManagerHolder();
        }
        return instance;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
