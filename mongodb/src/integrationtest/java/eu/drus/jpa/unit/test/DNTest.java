package eu.drus.jpa.unit.test;

import org.junit.ClassRule;

import eu.drus.jpa.unit.test.util.TestArchive;

public class DNTest extends CleanupIT {

    // @formatter:off
    @ClassRule
    public static TestArchive archive = TestArchive.newTestArchive()
        .addManifestResource("datanucleus-persistence.xml", "persistence.xml").build();
    // @formatter:on
}
