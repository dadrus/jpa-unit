package eu.drus.jpa.unit.neo4j.dataset;

import static eu.drus.jpa.unit.neo4j.test.utils.TestCodeUtils.buildModel;
import static eu.drus.jpa.unit.neo4j.test.utils.TestCodeUtils.compileModel;
import static eu.drus.jpa.unit.neo4j.test.utils.TestCodeUtils.loadClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

public class EntityUtilsTest {

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    private JCodeModel jCodeModel = new JCodeModel();

    @Test
    public void testGetEntityClassFromNodeLabelsHavingTheLabelDeclaredByTheClassNameWithoutInheritance() throws Exception {
        final String simpleClassName = "EntityClass";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, simpleClassName);
        jClass.annotate(Entity.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> entityClass = loadClass(testFolder.getRoot(), jClass.name());

        final Class<?> clazz = EntityUtils.getEntityClassFromNodeLabels(Arrays.asList(simpleClassName), Arrays.asList(entityClass));

        assertThat(clazz, equalTo(entityClass));
    }

    @Test
    public void testGetEntityClassFromNodeLabelsHavingTheLabelDeclaredByTheTableAnnotationWithoutInheritance() throws Exception {
        final String simpleClassName = "EntityClass";
        final String nodeLabel = "ENTITY_CLASS";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, simpleClassName);
        jClass.annotate(Entity.class);
        jClass.annotate(Table.class).param("name", nodeLabel);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> entityClass = loadClass(testFolder.getRoot(), jClass.name());

        final Class<?> clazz = EntityUtils.getEntityClassFromNodeLabels(Arrays.asList(nodeLabel), Arrays.asList(entityClass));

        assertThat(clazz, equalTo(entityClass));
    }

    @Test
    public void testGetEntityClassFromNodeLabelsHavingTheLabelDeclaredByTheClassNameWithSingleTableInheritance() throws Exception {
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameA = "SubEntityClassA";
        final String simpleClassNameB = "SubEntityClassB";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.SINGLE_TABLE);
        jBaseClass.annotate(DiscriminatorColumn.class).param("name", "TYPE");

        final JDefinedClass jSubclassA = jp._class(JMod.PUBLIC, simpleClassNameA)._extends(jBaseClass);
        jSubclassA.annotate(Entity.class);
        jSubclassA.annotate(DiscriminatorValue.class).param("value", "A");

        final JDefinedClass jSubclassB = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclassB.annotate(Entity.class);
        jSubclassB.annotate(DiscriminatorValue.class).param("value", "B");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> baseClass = loadClass(testFolder.getRoot(), jBaseClass.name());
        final Class<?> subClassA = loadClass(testFolder.getRoot(), jSubclassA.name());
        final Class<?> subClassB = loadClass(testFolder.getRoot(), jSubclassB.name());

        final Class<?> clazz = EntityUtils.getEntityClassFromNodeLabels(Arrays.asList(simpleClassNameBase),
                Arrays.asList(baseClass, subClassA, subClassB));

        assertThat(clazz, equalTo(baseClass));
    }

    @Test
    public void testGetEntityClassFromNodeLabelsHavingTheLabelDeclaredByTheTableAnnotationWithSingleTableInheritance() throws Exception {
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameA = "SubEntityClassA";
        final String simpleClassNameB = "SubEntityClassB";

        final String nodeLabel = "ENTITY_CLASS";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Table.class).param("name", nodeLabel);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.SINGLE_TABLE);
        jBaseClass.annotate(DiscriminatorColumn.class).param("name", "TYPE");

        final JDefinedClass jSubclassA = jp._class(JMod.PUBLIC, simpleClassNameA)._extends(jBaseClass);
        jSubclassA.annotate(Entity.class);
        jSubclassA.annotate(DiscriminatorValue.class).param("value", "A");

        final JDefinedClass jSubclassB = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclassB.annotate(Entity.class);
        jSubclassB.annotate(DiscriminatorValue.class).param("value", "B");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> baseClass = loadClass(testFolder.getRoot(), jBaseClass.name());
        final Class<?> subClassA = loadClass(testFolder.getRoot(), jSubclassA.name());
        final Class<?> subClassB = loadClass(testFolder.getRoot(), jSubclassB.name());

        final Class<?> clazz = EntityUtils.getEntityClassFromNodeLabels(Arrays.asList(nodeLabel),
                Arrays.asList(baseClass, subClassA, subClassB));

        assertThat(clazz, equalTo(baseClass));
    }

    @Test
    public void testGetEntityClassFromNodeLabelsHavingTheLabelDeclaredByTheClassNameWithTablePerClassInheritance() throws Exception {
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameA = "SubEntityClassA";
        final String simpleClassNameB = "SubEntityClassB";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.TABLE_PER_CLASS);

        final JDefinedClass jSubclassA = jp._class(JMod.PUBLIC, simpleClassNameA)._extends(jBaseClass);
        jSubclassA.annotate(Entity.class);

        final JDefinedClass jSubclassB = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclassB.annotate(Entity.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> baseClass = loadClass(testFolder.getRoot(), jBaseClass.name());
        final Class<?> subClassA = loadClass(testFolder.getRoot(), jSubclassA.name());
        final Class<?> subClassB = loadClass(testFolder.getRoot(), jSubclassB.name());

        final Class<?> clazz = EntityUtils.getEntityClassFromNodeLabels(Arrays.asList(simpleClassNameB),
                Arrays.asList(baseClass, subClassA, subClassB));

        assertThat(clazz, equalTo(subClassB));
    }

    @Test
    public void testGetEntityClassFromNodeLabelsHavingTheLabelDeclaredByTheTableAnnotationWithTablePerClassInheritance() throws Exception {
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameA = "SubEntityClassA";
        final String simpleClassNameB = "SubEntityClassB";

        final String nodeLabelBase = "ENTITY_CLASS";
        final String nodeLabelA = "ENTITY_CLASS_A";
        final String nodeLabelB = "ENTITY_CLASS_B";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Table.class).param("name", nodeLabelBase);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.TABLE_PER_CLASS);

        final JDefinedClass jSubclassA = jp._class(JMod.PUBLIC, simpleClassNameA)._extends(jBaseClass);
        jSubclassA.annotate(Entity.class);
        jSubclassA.annotate(Table.class).param("name", nodeLabelA);

        final JDefinedClass jSubclassB = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclassB.annotate(Entity.class);
        jSubclassB.annotate(Table.class).param("name", nodeLabelB);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> baseClass = loadClass(testFolder.getRoot(), jBaseClass.name());
        final Class<?> subClassA = loadClass(testFolder.getRoot(), jSubclassA.name());
        final Class<?> subClassB = loadClass(testFolder.getRoot(), jSubclassB.name());

        final Class<?> clazz = EntityUtils.getEntityClassFromNodeLabels(Arrays.asList(nodeLabelA),
                Arrays.asList(baseClass, subClassA, subClassB));

        assertThat(clazz, equalTo(subClassA));
    }

    @Test
    public void testGetEntityClassFromNodeLabelsHavingTheLabelDeclaredByTheClassNameWithJoinedInheritance() throws Exception {
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameA = "SubEntityClassA";
        final String simpleClassNameB = "SubEntityClassB";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.JOINED);

        final JDefinedClass jSubclassA = jp._class(JMod.PUBLIC, simpleClassNameA)._extends(jBaseClass);
        jSubclassA.annotate(Entity.class);

        final JDefinedClass jSubclassB = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclassB.annotate(Entity.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> baseClass = loadClass(testFolder.getRoot(), jBaseClass.name());
        final Class<?> subClassA = loadClass(testFolder.getRoot(), jSubclassA.name());
        final Class<?> subClassB = loadClass(testFolder.getRoot(), jSubclassB.name());

        final Class<?> clazz = EntityUtils.getEntityClassFromNodeLabels(Arrays.asList(simpleClassNameB),
                Arrays.asList(baseClass, subClassA, subClassB));

        assertThat(clazz, equalTo(subClassB));
    }

    @Test
    public void testGetEntityClassFromNodeLabelsHavingTheLabelDeclaredByTheTableAnnotationWithJoinedInheritance() throws Exception {
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameA = "SubEntityClassA";
        final String simpleClassNameB = "SubEntityClassB";

        final String nodeLabelBase = "ENTITY_CLASS";
        final String nodeLabelA = "ENTITY_CLASS_A";
        final String nodeLabelB = "ENTITY_CLASS_B";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Table.class).param("name", nodeLabelBase);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.JOINED);

        final JDefinedClass jSubclassA = jp._class(JMod.PUBLIC, simpleClassNameA)._extends(jBaseClass);
        jSubclassA.annotate(Entity.class);
        jSubclassA.annotate(Table.class).param("name", nodeLabelA);

        final JDefinedClass jSubclassB = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclassB.annotate(Entity.class);
        jSubclassB.annotate(Table.class).param("name", nodeLabelB);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> baseClass = loadClass(testFolder.getRoot(), jBaseClass.name());
        final Class<?> subClassA = loadClass(testFolder.getRoot(), jSubclassA.name());
        final Class<?> subClassB = loadClass(testFolder.getRoot(), jSubclassB.name());

        final Class<?> clazz = EntityUtils.getEntityClassFromNodeLabels(Arrays.asList(nodeLabelA),
                Arrays.asList(baseClass, subClassA, subClassB));

        assertThat(clazz, equalTo(subClassA));
    }

    @Test
    public void testGetNamesOfIdPropertiesFromASingleClassHavingAFieldAnnotatedWithId() throws Exception {
        // GIVEN
        final String simpleClassName = "EntityClass";
        final String idPropertyName = "key";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, simpleClassName);
        jClass.annotate(Entity.class);
        jClass.field(JMod.PRIVATE, String.class, idPropertyName).annotate(Id.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> entityClass = loadClass(testFolder.getRoot(), jClass.name());

        // WHEN
        final List<String> namesOfIdProperties = EntityUtils.getNamesOfIdProperties(entityClass);

        // THEN
        assertThat(namesOfIdProperties.size(), equalTo(1));
        assertThat(namesOfIdProperties, hasItem(idPropertyName));
    }

    @Test
    public void testGetNamesOfIdPropertiesFromASingleClassHavingAMethodAnnotatedWithId() throws Exception {
        // GIVEN
        final String simpleClassName = "EntityClass";
        final String idPropertyName = "key";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, simpleClassName);
        jClass.annotate(Entity.class);
        jClass.method(JMod.PUBLIC, jCodeModel.VOID, "getKey").annotate(Id.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> entityClass = loadClass(testFolder.getRoot(), jClass.name());

        // WHEN
        final List<String> namesOfIdProperties = EntityUtils.getNamesOfIdProperties(entityClass);

        // THEN
        assertThat(namesOfIdProperties.size(), equalTo(1));
        assertThat(namesOfIdProperties, hasItem(idPropertyName));
    }

    @Test
    public void testGetNamesOfIdPropertiesFromAClassHierarchyHavingAFieldAnnotatedWithId() throws Exception {
        // GIVEN
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameB = "SubEntityClass";
        final String idPropertyName = "key";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.TABLE_PER_CLASS);
        jBaseClass.field(JMod.PRIVATE, String.class, idPropertyName).annotate(Id.class);

        final JDefinedClass jSubclass = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclass.annotate(Entity.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> subClass = loadClass(testFolder.getRoot(), jSubclass.name());

        // WHEN
        final List<String> namesOfIdProperties = EntityUtils.getNamesOfIdProperties(subClass);

        // THEN
        assertThat(namesOfIdProperties.size(), equalTo(1));
        assertThat(namesOfIdProperties, hasItem(idPropertyName));
    }

    @Test
    public void testGetNamesOfIdPropertiesFromAClassHierarchyHavingAMethodAnnotatedWithId() throws Exception {
        // GIVEN
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameB = "SubEntityClass";
        final String idPropertyName = "key";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.TABLE_PER_CLASS);
        jBaseClass.method(JMod.PUBLIC, jCodeModel.VOID, "getKey").annotate(Id.class);

        final JDefinedClass jSubclass = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclass.annotate(Entity.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> subClass = loadClass(testFolder.getRoot(), jSubclass.name());

        // WHEN
        final List<String> namesOfIdProperties = EntityUtils.getNamesOfIdProperties(subClass);

        // THEN
        assertThat(namesOfIdProperties.size(), equalTo(1));
        assertThat(namesOfIdProperties, hasItem(idPropertyName));
    }

    @Test
    public void testGetNamesOfIdPropertiesFromASingleClassHavingAFieldAnnotatedWithEmbeddedId() throws Exception {
        // GIVEN
        final String simpleClassName = "EntityClass";
        final String compositeIdPropertyName = "compositeKey";
        final String id1PropertyName = "key1";
        final String id2PropertyName = "key2";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jIdTypeClass = jp._class(JMod.PUBLIC, "IdType");
        jIdTypeClass.annotate(Embeddable.class);
        jIdTypeClass.field(JMod.PRIVATE, Integer.class, id1PropertyName);
        jIdTypeClass.field(JMod.PRIVATE, String.class, id2PropertyName);

        final JDefinedClass jClass = jp._class(JMod.PUBLIC, simpleClassName);
        jClass.annotate(Entity.class);
        jClass.field(JMod.PRIVATE, jIdTypeClass, compositeIdPropertyName).annotate(EmbeddedId.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> entityClass = loadClass(testFolder.getRoot(), jClass.name());

        // WHEN
        final List<String> namesOfIdProperties = EntityUtils.getNamesOfIdProperties(entityClass);

        // THEN
        assertThat(namesOfIdProperties.size(), equalTo(2));
        assertThat(namesOfIdProperties,
                hasItems(compositeIdPropertyName + "." + id1PropertyName, compositeIdPropertyName + "." + id2PropertyName));
    }

    @Test
    public void testGetNamesOfIdPropertiesFromASingleClassHavingAMethodAnnotatedWithEmbeddedId() throws Exception {
        // GIVEN
        final String simpleClassName = "EntityClass";
        final String compositeIdPropertyName = "compositeKey";
        final String id1PropertyName = "key1";
        final String id2PropertyName = "key2";

        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jIdTypeClass = jp._class(JMod.PUBLIC, "IdType");
        jIdTypeClass.annotate(Embeddable.class);
        jIdTypeClass.field(JMod.PRIVATE, Integer.class, id1PropertyName);
        jIdTypeClass.field(JMod.PRIVATE, String.class, id2PropertyName);

        final JDefinedClass jClass = jp._class(JMod.PUBLIC, simpleClassName);
        jClass.annotate(Entity.class);
        final JMethod method = jClass.method(JMod.PUBLIC, jIdTypeClass, "getCompositeKey");
        method.annotate(EmbeddedId.class);
        method.body()._return(JExpr._null());

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> entityClass = loadClass(testFolder.getRoot(), jClass.name());

        // WHEN
        final List<String> namesOfIdProperties = EntityUtils.getNamesOfIdProperties(entityClass);

        // THEN
        assertThat(namesOfIdProperties.size(), equalTo(2));
        assertThat(namesOfIdProperties,
                hasItems(compositeIdPropertyName + "." + id1PropertyName, compositeIdPropertyName + "." + id2PropertyName));
    }

    @Test
    public void testGetNamesOfIdPropertiesFromAClassHierarchyHavingAFieldAnnotatedWithEmbeddedId() throws Exception {
        // GIVEN
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameB = "SubEntityClass";
        final String compositeIdPropertyName = "compositeKey";
        final String id1PropertyName = "key1";
        final String id2PropertyName = "key2";

        final JPackage jp = jCodeModel.rootPackage();

        final JDefinedClass jIdTypeClass = jp._class(JMod.PUBLIC, "IdType");
        jIdTypeClass.annotate(Embeddable.class);
        jIdTypeClass.field(JMod.PRIVATE, Integer.class, id1PropertyName);
        jIdTypeClass.field(JMod.PRIVATE, String.class, id2PropertyName);

        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.TABLE_PER_CLASS);
        jBaseClass.field(JMod.PRIVATE, jIdTypeClass, compositeIdPropertyName).annotate(EmbeddedId.class);

        final JDefinedClass jSubclass = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclass.annotate(Entity.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> entityClass = loadClass(testFolder.getRoot(), jSubclass.name());

        // WHEN
        final List<String> namesOfIdProperties = EntityUtils.getNamesOfIdProperties(entityClass);

        // THEN
        assertThat(namesOfIdProperties.size(), equalTo(2));
        assertThat(namesOfIdProperties,
                hasItems(compositeIdPropertyName + "." + id1PropertyName, compositeIdPropertyName + "." + id2PropertyName));
    }

    @Test
    public void testGetNamesOfIdPropertiesFromAClassHierarchyHavingAMethodAnnotatedWithEmbeddedId() throws Exception {
        // GIVEN
        final String simpleClassNameBase = "EntityClass";
        final String simpleClassNameB = "SubEntityClass";
        final String compositeIdPropertyName = "compositeKey";
        final String id1PropertyName = "key1";
        final String id2PropertyName = "key2";

        final JPackage jp = jCodeModel.rootPackage();

        final JDefinedClass jIdTypeClass = jp._class(JMod.PUBLIC, "IdType");
        jIdTypeClass.annotate(Embeddable.class);
        jIdTypeClass.field(JMod.PRIVATE, Integer.class, id1PropertyName);
        jIdTypeClass.field(JMod.PRIVATE, String.class, id2PropertyName);

        final JDefinedClass jBaseClass = jp._class(JMod.PUBLIC, simpleClassNameBase);
        jBaseClass.annotate(Entity.class);
        jBaseClass.annotate(Inheritance.class).param("strategy", InheritanceType.TABLE_PER_CLASS);
        final JMethod method = jBaseClass.method(JMod.PUBLIC, jIdTypeClass, "getCompositeKey");
        method.annotate(EmbeddedId.class);
        method.body()._return(JExpr._null());

        final JDefinedClass jSubclass = jp._class(JMod.PUBLIC, simpleClassNameB)._extends(jBaseClass);
        jSubclass.annotate(Entity.class);

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> entityClass = loadClass(testFolder.getRoot(), jSubclass.name());

        // WHEN
        final List<String> namesOfIdProperties = EntityUtils.getNamesOfIdProperties(entityClass);

        // THEN
        assertThat(namesOfIdProperties.size(), equalTo(2));
        assertThat(namesOfIdProperties,
                hasItems(compositeIdPropertyName + "." + id1PropertyName, compositeIdPropertyName + "." + id2PropertyName));
    }
}
