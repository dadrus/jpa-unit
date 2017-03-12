package eu.drus.jpa.unit;

import javax.naming.OperationNotSupportedException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.JpaUnitExtension;

@Ignore
@RunWith(JUnitPlatform.class)
@ExtendWith(JpaUnitExtension.class)
public class Junit5Test {

    @PersistenceContext(unitName = "test-unit-1")
    private EntityManager manager;

    @Test
    @Cleanup(phase = CleanupPhase.NONE)
    public void test1() throws OperationNotSupportedException {

    }

    @Test
    @DisplayName("my test 2")
    public void test2() {

        throw new RuntimeException();
    }
}
