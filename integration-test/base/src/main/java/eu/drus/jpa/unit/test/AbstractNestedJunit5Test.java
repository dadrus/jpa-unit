package eu.drus.jpa.unit.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import eu.drus.jpa.unit.api.TransactionMode;
import eu.drus.jpa.unit.api.Transactional;
import eu.drus.jpa.unit.test.model.Depositor;

public abstract class AbstractNestedJunit5Test {
    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager entityManager;

    @Nested
    @DisplayName("Given an empty test table")
    class GivenAnEmptyTestTable {

        @Test
        @DisplayName("Then the size sould be zero")
        void thenTheSizeShouldBeZero() {
            final List<Depositor> list = entityManager
                    .createQuery("select d from Depositor d", Depositor.class)
                    .getResultList();
            assertEquals(list.size(), 0);
        }

        @Nested
        @DisplayName("When a row is inserted")
        class WhenARowIsInserted {

            @BeforeEach
            @Transactional(TransactionMode.COMMIT)
            void setUp() {
                final Depositor depositor = new Depositor("Max", "Doe");
                entityManager.persist(depositor);
            }

            @Test
            @DisplayName("Then the size should be one")
            void thenTheSizeShouldBeOne() {
                final List<Depositor> list = entityManager
                        .createQuery("select d from Depositor d", Depositor.class)
                        .getResultList();
                assertEquals(list.size(), 0);
            }
        }
    }
}
