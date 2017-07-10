package eu.drus.jpa.unit.core.metadata;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.junit.Test;

import eu.drus.jpa.unit.api.ApplyScriptsAfter;
import eu.drus.jpa.unit.api.ApplyScriptsBefore;
import eu.drus.jpa.unit.api.Bootstrapping;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupUsingScripts;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.Transactional;

public class MetadataExtractorTest {

    private MetadataExtractor metadataExtractor = new MetadataExtractor(MetadataExtractorTest.class);

    @Test
    public void testApplyScriptsAfter() {

        // WHEN
        final AnnotationInspector<ApplyScriptsAfter> ai = metadataExtractor.applyScriptsAfter();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testApplyScriptsBefore() {

        // WHEN
        final AnnotationInspector<ApplyScriptsBefore> ai = metadataExtractor.applyScriptsBefore();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testCleanup() {

        // WHEN
        final AnnotationInspector<Cleanup> ai = metadataExtractor.cleanup();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testCleanupUsingScripts() {

        // WHEN
        final AnnotationInspector<CleanupUsingScripts> ai = metadataExtractor.cleanupUsingScripts();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testExpectedDataSets() {

        // WHEN
        final AnnotationInspector<ExpectedDataSets> ai = metadataExtractor.expectedDataSets();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testInitialDataSets() {

        // WHEN
        final AnnotationInspector<InitialDataSets> ai = metadataExtractor.initialDataSets();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testPersistenceContext() {

        // WHEN
        final AnnotationInspector<PersistenceContext> ai = metadataExtractor.persistenceContext();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testPersistenceUnit() {

        // WHEN
        final AnnotationInspector<PersistenceUnit> ai = metadataExtractor.persistenceUnit();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testTransactional() {

        // WHEN
        final AnnotationInspector<Transactional> ai = metadataExtractor.transactional();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testBootstrapping() {

        // WHEN
        final AnnotationInspector<Bootstrapping> ai = metadataExtractor.bootstrapping();

        // THEN
        assertThat(ai, notNullValue());
    }
}
