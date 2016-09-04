package eu.drus.test.persistence.core.metadata;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

import eu.drus.test.persistence.annotation.ApplyScriptsAfter;
import eu.drus.test.persistence.annotation.ApplyScriptsBefore;
import eu.drus.test.persistence.annotation.Cleanup;
import eu.drus.test.persistence.annotation.CleanupPhase;
import eu.drus.test.persistence.annotation.CleanupUsingScripts;
import eu.drus.test.persistence.annotation.ExpectedDataSets;
import eu.drus.test.persistence.annotation.InitialDataSets;

public class FeatureResolverTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testSeedDataIsDisabledForClassWithoutInitialDataSetDefinition() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldSeedData(), is(Boolean.FALSE));
    }

    @Test
    public void testSeedDataIsEnabledForClassWithClassLevelInitialDataSetDefinition() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "TestDataSet.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldSeedData(), is(Boolean.TRUE));
    }

    @Test
    public void testSeedDataIsEnabledForClassWithMethodLevelInitialDataSetDefinition() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "TestDataSet.file");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldSeedData(), is(Boolean.TRUE));
    }

    @Test
    public void testApplyingCustomScriptBeforeTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptBefore(), is(Boolean.FALSE));
    }

    @Test
    public void testApplyingCustomScriptBeforeTestIsEnabledForClassWithCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(ApplyScriptsBefore.class);
        jAnnotationUse.param("value", "Script.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptBefore(), is(Boolean.TRUE));
    }

    @Test
    public void testApplyingCustomScriptBeforeTestIsEnabledForClassWithCorrespondingMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(ApplyScriptsBefore.class);
        jAnnotationUse.param("value", "Script.file");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptBefore(), is(Boolean.TRUE));
    }

    @Test
    public void testApplyingCustomScriptAfterTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptAfter(), is(Boolean.FALSE));
    }

    @Test
    public void testApplyingCustomScriptAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(ApplyScriptsAfter.class);
        jAnnotationUse.param("value", "Script.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testApplyingCustomScriptAfterTestIsEnabledForClassWithCorrespondingMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(ApplyScriptsAfter.class);
        jAnnotationUse.param("value", "Script.file");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testVerificationDataAfterTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldVerifyDataAfter(), is(Boolean.FALSE));
    }

    @Test
    public void testVerificationDataAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(ExpectedDataSets.class);
        jAnnotationUse.param("value", "ExpectedDataSets.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldVerifyDataAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testVerificationDataAfterTestIsEnabledForClassWithCorrespondingMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(ExpectedDataSets.class);
        jAnnotationUse.param("value", "ExpectedDataSets.file");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldVerifyDataAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupAfterTestIsEnabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.AFTER);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupAfterTestIsEnabledForClassWithCorrespondingMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.AFTER);

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupBeforeTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), is(Boolean.FALSE));
    }

    @Test
    public void testCleanupBeforeTestIsEnabledForClassWithCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupBeforeTestIsEnabledForClassWithCorrespondingMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupUsingScriptBeforeTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptBefore(), is(Boolean.FALSE));
    }

    @Test
    public void testCleanupUsingScriptBeforeTestIsEnabledForClassWithCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "CleanupUsingScripts.file");
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptBefore(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupUsingScriptBeforeTestIsEnabledForClassWithCorrespondingMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "CleanupUsingScripts.file");
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptBefore(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), is(Boolean.FALSE));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotationWithoutCleanupPhaseSpecified()
            throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "CleanupUsingScripts.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotationWithCleanupPhaseSpecified()
            throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "CleanupUsingScripts.file");
        jAnnotationUse.param("phase", CleanupPhase.AFTER);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsEnabledForClassWithCorrespondingMethodLevelAnnotationWithoutCleanupPhaseSpecified()
            throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "CleanupUsingScripts.file");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), is(Boolean.TRUE));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsEnabledForClassWithCorrespondingMethodLevelAnnotationWithCleanupPhaseSpecified()
            throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "CleanupUsingScripts.file");
        jAnnotationUse.param("phase", CleanupPhase.AFTER);

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), is(Boolean.TRUE));
    }

    private void compileModel(final File destinationFolder) throws IOException {

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        final File[] javaFiles = testFolder.getRoot().listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".java");
            }
        });

        final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(javaFiles);
        compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
        fileManager.close();
    }

    private Class<?> loadClass(final String className) throws MalformedURLException, ClassNotFoundException {
        final ClassLoader cl = new URLClassLoader(new URL[] {
                testFolder.getRoot().toURI().toURL()
        });

        return Class.forName(className, false, cl);
    }
}
