package eu.drus.jpa.unit.cdi;

import javax.persistence.EntityManager;

class EntityManagerHolder {

    public static final EntityManagerHolder INSTANCE = new EntityManagerHolder();
    private static final ThreadLocal<EntityManager> CONTEXT = new ThreadLocal<>();

    private EntityManagerHolder() {}

    public EntityManager getEntityManager() {
        return CONTEXT.get();
    }

    public void setEntityManager(final EntityManager value) {
        if (value != null) {
            CONTEXT.set(value);
        } else {
            CONTEXT.remove();
        }
    }
}
