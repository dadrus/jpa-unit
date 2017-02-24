package eu.drus.jpa.unit.cdi.rule;

import javax.persistence.EntityManager;

import eu.drus.jpa.unit.cdi.EntityManagerHolder;
import eu.drus.jpa.unit.rule.TestFixture;
import eu.drus.jpa.unit.rule.TestInvocation;

public class CdiProducerFixture implements TestFixture {

    private static final EntityManagerHolder emCtx = EntityManagerHolder.getInstance();

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public void apply(final TestInvocation invocation) throws Throwable {
        try {
            final EntityManager em = (EntityManager) invocation.getContext().getData("em");
            emCtx.setEntityManager(em);
            invocation.proceed();
        } finally {
            emCtx.setEntityManager(null);
        }
    }
}
