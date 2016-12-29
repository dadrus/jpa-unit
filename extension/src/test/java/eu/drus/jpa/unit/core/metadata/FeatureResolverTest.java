package eu.drus.jpa.unit.core.metadata;

import static eu.drus.jpa.unit.core.metadata.TestCodeUtils.buildModel;
import static eu.drus.jpa.unit.core.metadata.TestCodeUtils.compileModel;
import static eu.drus.jpa.unit.core.metadata.TestCodeUtils.loadClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

import eu.drus.jpa.unit.annotation.ApplyScriptsAfter;
import eu.drus.jpa.unit.annotation.ApplyScriptsBefore;
import eu.drus.jpa.unit.annotation.Cleanup;
import eu.drus.jpa.unit.annotation.CleanupCache;
import eu.drus.jpa.unit.annotation.CleanupPhase;
import eu.drus.jpa.unit.annotation.CleanupStrategy;
import eu.drus.jpa.unit.annotation.CleanupUsingScripts;
import eu.drus.jpa.unit.annotation.CustomColumnFilter;
import eu.drus.jpa.unit.annotation.DataSeedStrategy;
import eu.drus.jpa.unit.annotation.ExpectedDataSets;
import eu.drus.jpa.unit.annotation.InitialDataSets;

public class FeatureResolverTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testSeedDataIsDisabledForClassWithoutInitialDataSetAnnotationn() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldSeedData(), equalTo(Boolean.FALSE));

        final List<String> seedScripts = resolver.getSeedData();
        assertThat(seedScripts.isEmpty(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testSeedDataIsEnabledForClassWithClassLevelInitialDataSetAnnotationWithDefaultDataSeedStrategy() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "Script.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldSeedData(), equalTo(Boolean.TRUE));

        final List<String> seedScripts = resolver.getSeedData();
        assertThat(seedScripts.size(), equalTo(1));
        assertThat(seedScripts, hasItem("Script.file"));

        final DataSeedStrategy dataSeedStrategy = resolver.getDataSeedStrategy();
        assertThat(dataSeedStrategy, equalTo(DataSeedStrategy.INSERT));
    }

    @Test
    public void testSeedDataIsEnabledForClassWithClassLevelInitialDataSetAnnotationWithSpecifiedDataSeedStrategy() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "Script.file");
        jAnnotationUse.param("seedStrategy", DataSeedStrategy.UPDATE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldSeedData(), equalTo(Boolean.TRUE));

        final List<String> seedScripts = resolver.getSeedData();
        assertThat(seedScripts.size(), equalTo(1));
        assertThat(seedScripts, hasItem("Script.file"));

        final DataSeedStrategy dataSeedStrategy = resolver.getDataSeedStrategy();
        assertThat(dataSeedStrategy, equalTo(DataSeedStrategy.UPDATE));
    }

    @Test
    public void testSeedDataIsEnabledForClassWithClassAndMethodLevelInitialDataSetAnnotationWithDefaultDataSeedStrategy() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "ClassScript.file");
        jAnnotationUse.param("seedStrategy", DataSeedStrategy.UPDATE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "MethodScript.file");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldSeedData(), equalTo(Boolean.TRUE));

        final List<String> seedScripts = resolver.getSeedData();
        assertThat(seedScripts.size(), equalTo(1));
        assertThat(seedScripts, hasItem("MethodScript.file"));

        final DataSeedStrategy dataSeedStrategy = resolver.getDataSeedStrategy();
        assertThat(dataSeedStrategy, equalTo(DataSeedStrategy.INSERT));
    }

    @Test
    public void testSeedDataIsEnabledForClassWithClassAndMethodLevelInitialDataSetAnnotaitonWithSpecifiedDataSeedStrategy()
            throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "ClassScript.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "MethodScript.file");
        jAnnotationUse.param("seedStrategy", DataSeedStrategy.UPDATE);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldSeedData(), equalTo(Boolean.TRUE));

        final List<String> seedScripts = resolver.getSeedData();
        assertThat(seedScripts.size(), equalTo(1));
        assertThat(seedScripts, hasItem("MethodScript.file"));

        final DataSeedStrategy dataSeedStrategy = resolver.getDataSeedStrategy();
        assertThat(dataSeedStrategy, equalTo(DataSeedStrategy.UPDATE));
    }

    @Test
    public void testApplyingCustomScriptBeforeTestIsDisabledForClassWithoutCorrespondingAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptBefore(), equalTo(Boolean.FALSE));

        final List<String> preExecutionScripts = resolver.getPreExecutionScripts();
        assertThat(preExecutionScripts.isEmpty(), equalTo(Boolean.TRUE));
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

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptBefore(), equalTo(Boolean.TRUE));

        final List<String> preExecutionScripts = resolver.getPreExecutionScripts();
        assertThat(preExecutionScripts.size(), equalTo(1));
        assertThat(preExecutionScripts, hasItem("Script.file"));
    }

    @Test
    public void testApplyingCustomScriptBeforeTestIsEnabledForClassWithCorrespondingClassAndMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(ApplyScriptsBefore.class);
        jAnnotationUse.param("value", "ClassScript.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(ApplyScriptsBefore.class);
        jAnnotationUse.param("value", "MethodScript.file");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptBefore(), equalTo(Boolean.TRUE));

        final List<String> preExecutionScripts = resolver.getPreExecutionScripts();
        assertThat(preExecutionScripts.size(), equalTo(1));
        assertThat(preExecutionScripts, hasItem("MethodScript.file"));
    }

    @Test
    public void testApplyingCustomScriptAfterTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptAfter(), equalTo(Boolean.FALSE));

        final List<String> postExecutionScripts = resolver.getPostExecutionScripts();
        assertThat(postExecutionScripts.isEmpty(), equalTo(Boolean.TRUE));
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

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptAfter(), equalTo(Boolean.TRUE));

        final List<String> postExecutionScripts = resolver.getPostExecutionScripts();
        assertThat(postExecutionScripts.size(), equalTo(1));
        assertThat(postExecutionScripts, hasItem("Script.file"));
    }

    @Test
    public void testApplyingCustomScriptAfterTestIsEnabledForClassWithCorrespondingClassAndMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(ApplyScriptsAfter.class);
        jAnnotationUse.param("value", "ClassScript.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(ApplyScriptsAfter.class);
        jAnnotationUse.param("value", "MethodScript.file");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldApplyCustomScriptAfter(), equalTo(Boolean.TRUE));

        final List<String> postExecutionScripts = resolver.getPostExecutionScripts();
        assertThat(postExecutionScripts.size(), equalTo(1));
        assertThat(postExecutionScripts, hasItem("MethodScript.file"));
    }

    @Test
    public void testVerificationDataAfterTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldVerifyDataAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testVerificationDataAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(ExpectedDataSets.class);
        jAnnotationUse.param("value", "ClassLevelScript.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldVerifyDataAfter(), equalTo(Boolean.TRUE));

        final ExpectedDataSets expectedDataSets = resolver.getExpectedDataSets();
        assertThat(expectedDataSets.value(), equalTo(new String[] {
                "ClassLevelScript.file"
        }));
    }

    @Test
    public void testVerificationDataAfterTestIsEnabledForClassWithCorrespondingClassAndMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(ExpectedDataSets.class);
        jAnnotationUse.param("value", "ClassLevelScript.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(ExpectedDataSets.class);
        jAnnotationUse.param("value", "MethodLevelScript.file");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldVerifyDataAfter(), equalTo(Boolean.TRUE));

        final ExpectedDataSets expectedDataSets = resolver.getExpectedDataSets();
        assertThat(expectedDataSets.value(), equalTo(new String[] {
                "MethodLevelScript.file"
        }));
    }

    @Test
    public void testCustomColumnFilterIsNotAllowedWithoutExpectedDataSetsAnnotationUsage() throws Exception {

        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ColumnFilterImpl");
        jClass._implements(IColumnFilter.class);
        JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "accept");
        jMethod.param(String.class, "tableName");
        jMethod.param(Column.class, "column");
        jMethod.body()._return(JExpr.TRUE);
        final JDefinedClass jTestClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jTestClass.annotate(CustomColumnFilter.class);
        jAnnotationUse.param("value", jClass);
        jMethod = jTestClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jTestClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        try {
            resolver.getCustomColumnFilter();
            fail("Exception expected");
        } catch (final Exception e) {

        }
    }

    @Test
    public void testCustomColumnFilterEnabledOnClassLevel() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jFilterClass = jp._class(JMod.PUBLIC, "ColumnFilterImpl");
        jFilterClass._implements(IColumnFilter.class);
        JMethod jMethod = jFilterClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "accept");
        jMethod.param(String.class, "tableName");
        jMethod.param(Column.class, "column");
        jMethod.body()._return(JExpr.TRUE);
        final JDefinedClass jTestClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jTestClass.annotate(ExpectedDataSets.class);
        jAnnotationUse.param("value", "MethodLevelScript.file");
        jAnnotationUse = jTestClass.annotate(CustomColumnFilter.class);
        jAnnotationUse.param("value", jFilterClass);
        jMethod = jTestClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jTestClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        final Set<Class<? extends IColumnFilter>> filterSet = resolver.getCustomColumnFilter();
        assertThat(filterSet.size(), equalTo(1));
    }

    @Test
    public void testCustomColumnFilterEnabledOnMethodLevel() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jFilterClass = jp._class(JMod.PUBLIC, "ColumnFilterImpl");
        jFilterClass._implements(IColumnFilter.class);
        JMethod jMethod = jFilterClass.method(JMod.PUBLIC, jCodeModel.BOOLEAN, "accept");
        jMethod.param(String.class, "tableName");
        jMethod.param(Column.class, "column");
        jMethod.body()._return(JExpr.TRUE);
        final JDefinedClass jTestClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        jMethod = jTestClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        JAnnotationUse jAnnotationUse = jMethod.annotate(ExpectedDataSets.class);
        jAnnotationUse.param("value", "MethodLevelScript.file");
        jAnnotationUse = jMethod.annotate(CustomColumnFilter.class);
        jAnnotationUse.param("value", jFilterClass);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jTestClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        final Set<Class<? extends IColumnFilter>> filterSet = resolver.getCustomColumnFilter();
        assertThat(filterSet.size(), equalTo(1));
    }

    @Test
    public void testCleanupAfterTestIsEnabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.TRUE));
        assertThat(resolver.getCleanupStrategy(), equalTo(CleanupStrategy.STRICT));
    }

    @Test
    public void testCleanupAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotationWithDefaultCleanupStrategy() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        jClass.annotate(Cleanup.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.TRUE));
        assertThat(resolver.getCleanupStrategy(), equalTo(CleanupStrategy.STRICT));
    }

    @Test
    public void testCleanupAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotationAndNotDefaultCleanupStrategy() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        jAnnotationUse.param("strategy", CleanupStrategy.USED_ROWS_ONLY);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.TRUE));
        assertThat(resolver.getCleanupStrategy(), equalTo(CleanupStrategy.USED_ROWS_ONLY));
    }

    @Test
    public void testCleanupAfterTestIsEnabledForClassWithCorrespondingClassAndMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        jAnnotationUse.param("strategy", CleanupStrategy.USED_ROWS_ONLY);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(Cleanup.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.TRUE));
        assertThat(resolver.getCleanupStrategy(), equalTo(CleanupStrategy.STRICT));
    }

    @Test
    public void testCleanupAfterTestIsDisabledIfCleanupUsingScriptsAfterIsEnabled() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.AFTER);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "schema.sql");
        jAnnotationUse.param("phase", CleanupPhase.AFTER);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupUsingScriptAfter(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testCleanupIsDisabledUsingCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.NONE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testCleanupIsDisabledUsingCorrespondingMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.NONE);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testCleanupBeforeTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
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

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.TRUE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testCleanupBeforeTestIsEnabledForClassWithCorrespondingClassAndMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.TRUE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testCleanupBeforeTestIsDisabledIfCleanupUsingScriptsBeforeIsEnabled() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(Cleanup.class);
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "schema.sql");
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupAfter(), equalTo(Boolean.FALSE));
        assertThat(resolver.shouldCleanupUsingScriptBefore(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testCleanupUsingScriptBeforeTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptBefore(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testCleanupUsingScriptBeforeTestIsEnabledForClassWithCorrespondingClassLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "Script.file");
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptBefore(), equalTo(Boolean.TRUE));

        final List<String> cleanupScripts = resolver.getCleanupScripts();
        assertThat(cleanupScripts.size(), equalTo(1));
        assertThat(cleanupScripts, hasItem("Script.file"));

    }

    @Test
    public void testCleanupUsingScriptBeforeTestIsEnabledForClassWithCorrespondingClassAndMethodLevelAnnotation() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "ClassScripts.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "MethodScripts.file");
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptBefore(), equalTo(Boolean.TRUE));
        final List<String> cleanupScripts = resolver.getCleanupScripts();
        assertThat(cleanupScripts.size(), equalTo(1));
        assertThat(cleanupScripts, hasItem("MethodScripts.file"));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotationWithoutCleanupPhaseSpecified()
            throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "Script.file");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), equalTo(Boolean.TRUE));

        final List<String> cleanupScripts = resolver.getCleanupScripts();
        assertThat(cleanupScripts.size(), equalTo(1));
        assertThat(cleanupScripts, hasItem("Script.file"));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotationWithCleanupPhaseSpecified()
            throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "Script.file");
        jAnnotationUse.param("phase", CleanupPhase.AFTER);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), equalTo(Boolean.TRUE));

        final List<String> cleanupScripts = resolver.getCleanupScripts();
        assertThat(cleanupScripts.size(), equalTo(1));
        assertThat(cleanupScripts, hasItem("Script.file"));
    }

    @Test
    public void testCleanupUsingScriptAfterTestIsEnabledForClassWithCorrespondingMethodLevelAnnotationWithoutCleanupPhaseSpecified()
            throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "ClassScript.file");
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        jAnnotationUse = jMethod.annotate(CleanupUsingScripts.class);
        jAnnotationUse.param("value", "MethodScript.file");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), equalTo(Boolean.TRUE));

        final List<String> cleanupScripts = resolver.getCleanupScripts();
        assertThat(cleanupScripts.size(), equalTo(1));
        assertThat(cleanupScripts, hasItem("MethodScript.file"));
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
        jAnnotationUse.param("value", "Script.file");
        jAnnotationUse.param("phase", CleanupPhase.AFTER);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldCleanupUsingScriptAfter(), equalTo(Boolean.TRUE));

        final List<String> cleanupScripts = resolver.getCleanupScripts();
        assertThat(cleanupScripts.size(), equalTo(1));
        assertThat(cleanupScripts, hasItem("Script.file"));
    }

    @Test
    public void testEvictCacheAfterTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testEvictCacheAfterTestIsDisabledForClassWithCorrespondingClassLevelAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupCache.class);
        jAnnotationUse.param("value", Boolean.FALSE);
        jAnnotationUse.param("phase", CleanupPhase.AFTER);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testEvictCacheAfterTestIsEnabledForClassWithCorrespondingClassLevelAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupCache.class);
        jAnnotationUse.param("value", Boolean.TRUE);
        jAnnotationUse.param("phase", CleanupPhase.AFTER);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheAfter(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testEvictCacheAfterTestIsDisabledForClassWithCorrespondingMethodLevelAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(CleanupCache.class);
        jAnnotationUse.param("value", Boolean.FALSE);
        jAnnotationUse.param("phase", CleanupPhase.AFTER);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheAfter(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testEvictCacheAfterTestIsEnabledForClassWithCorrespondingMethodLevelAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(CleanupCache.class);
        jAnnotationUse.param("value", Boolean.TRUE);
        jAnnotationUse.param("phase", CleanupPhase.AFTER);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheAfter(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testEvictCacheBeforeTestIsDisabledForClassWithoutCorrespondingAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheBefore(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testEvictCacheBeforeTestIsDisabledForClassWithCorrespondingClassLevelAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupCache.class);
        jAnnotationUse.param("value", Boolean.FALSE);
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheBefore(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testEvictCacheBeforeTestIsEnabledForClassWithCorrespondingClassLevelAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(CleanupCache.class);
        jAnnotationUse.param("value", Boolean.TRUE);
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheBefore(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testEvictCacheBeforeTestIsDisabledForClassWithCorrespondingMethodLevelAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(CleanupCache.class);
        jAnnotationUse.param("value", Boolean.FALSE);
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheBefore(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testEvictCacheBeforeTestIsEnabledForClassWithCorrespondingMethodLevelAnnotations() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");
        final JAnnotationUse jAnnotationUse = jMethod.annotate(CleanupCache.class);
        jAnnotationUse.param("value", Boolean.TRUE);
        jAnnotationUse.param("phase", CleanupPhase.BEFORE);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = new FeatureResolver(method, cut);

        // THEN
        assertThat(resolver.shouldEvictCacheBefore(), equalTo(Boolean.TRUE));
    }
}
