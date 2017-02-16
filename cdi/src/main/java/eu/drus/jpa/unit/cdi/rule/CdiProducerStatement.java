package eu.drus.jpa.unit.cdi.rule;

import javax.persistence.EntityManager;

import org.junit.runners.model.Statement;

import eu.drus.jpa.unit.cdi.EntityManagerHolder;
import eu.drus.jpa.unit.rule.ExecutionContext;
import eu.drus.jpa.unit.util.ReflectionUtils;

public class CdiProducerStatement extends Statement {

    private static final EntityManagerHolder emCtx = EntityManagerHolder.getInstance();

    private ExecutionContext ctx;
    private Statement base;
    private Object target;

    public CdiProducerStatement(final ExecutionContext ctx, final Statement base, final Object target) {
        this.ctx = ctx;
        this.base = base;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            final EntityManager em = (EntityManager) ReflectionUtils.getValue(ctx.getPersistenceField(), target);
            emCtx.setEntityManager(em);
            base.evaluate();
        } finally {
            emCtx.setEntityManager(null);
        }
    }
}
