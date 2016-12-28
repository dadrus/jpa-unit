package eu.drus.jpa.unit.core.metadata;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.junit.runners.model.TestClass;

import eu.drus.jpa.unit.annotation.ApplyScriptsAfter;
import eu.drus.jpa.unit.annotation.ApplyScriptsBefore;
import eu.drus.jpa.unit.annotation.Cleanup;
import eu.drus.jpa.unit.annotation.CleanupCache;
import eu.drus.jpa.unit.annotation.CleanupUsingScripts;
import eu.drus.jpa.unit.annotation.CustomColumnFilter;
import eu.drus.jpa.unit.annotation.ExpectedDataSets;
import eu.drus.jpa.unit.annotation.InitialDataSets;
import eu.drus.jpa.unit.annotation.Transactional;

public class MetadataExtractor {
    private final TestClass testClass;

    private final Map<Class<?>, AnnotationInspector<?>> inspectors = new HashMap<>();

    public MetadataExtractor(final TestClass testClass) {
        this.testClass = testClass;
    }

    private <K extends Annotation> void register(final TestClass testClass, final Class<K> annotation) {
        inspectors.put(annotation, new AnnotationInspector<>(testClass, annotation));
    }

    @SuppressWarnings("unchecked")
    private <K extends Annotation> AnnotationInspector<K> using(final Class<K> annotation) {
        if (inspectors.get(annotation) == null) {
            register(testClass, annotation);
        }
        return (AnnotationInspector<K>) inspectors.get(annotation);
    }

    public AnnotationInspector<Transactional> transactional() {
        return using(Transactional.class);
    }

    public AnnotationInspector<InitialDataSets> initialDataSets() {
        return using(InitialDataSets.class);
    }

    public AnnotationInspector<ExpectedDataSets> expectedDataSets() {
        return using(ExpectedDataSets.class);
    }

    public AnnotationInspector<CustomColumnFilter> customColumnFilter() {
        return using(CustomColumnFilter.class);
    }

    public AnnotationInspector<Cleanup> cleanup() {
        return using(Cleanup.class);
    }

    public AnnotationInspector<CleanupUsingScripts> cleanupUsingScripts() {
        return using(CleanupUsingScripts.class);
    }

    public AnnotationInspector<PersistenceContext> persistenceContext() {
        return using(PersistenceContext.class);
    }

    public AnnotationInspector<PersistenceUnit> persistenceUnit() {
        return using(PersistenceUnit.class);
    }

    public AnnotationInspector<ApplyScriptsBefore> applyScriptsBefore() {
        return using(ApplyScriptsBefore.class);
    }

    public AnnotationInspector<ApplyScriptsAfter> applyScriptsAfter() {
        return using(ApplyScriptsAfter.class);
    }

    public AnnotationInspector<CleanupCache> cleanupCache() {
        return using(CleanupCache.class);
    }
}