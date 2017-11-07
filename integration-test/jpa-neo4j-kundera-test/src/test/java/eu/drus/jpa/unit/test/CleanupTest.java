package eu.drus.jpa.unit.test;

import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.JpaUnitRunner;

@RunWith(JpaUnitRunner.class)
// @Ignore
public class CleanupTest extends AbstractCleanupTest {
    // the first test case runs already into NPE during transaction commit in hibernate OGM code
}
