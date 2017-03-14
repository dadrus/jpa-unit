package eu.drus.jpa.unit.core.metadata;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import eu.drus.jpa.unit.api.ApplyScriptsAfter;
import eu.drus.jpa.unit.api.ApplyScriptsBefore;
import eu.drus.jpa.unit.api.Bootstrapping;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupCache;
import eu.drus.jpa.unit.api.CleanupUsingScripts;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.Transactional;

public class MetadataExtractor {
    private final Class<?> testClass;

    private final Map<Class<?>, AnnotationInspector<?>> inspectors = new HashMap<>();

    public MetadataExtractor(final Class<?> testClass) {
        this.testClass = testClass;
    }

    private <K extends Annotation> void register(final Class<?> testClass, final Class<K> annotation) {
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

    public AnnotationInspector<Bootstrapping> bootstrapping() {
        return using(Bootstrapping.class);
    }
}
