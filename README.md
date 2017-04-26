# JPA Unit [![Build Status](https://travis-ci.org/dadrus/jpa-unit.svg?branch=master)](https://travis-ci.org/dadrus/jpa-unit) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=com.github.dadrus:jpa-unit-parent)](https://sonarqube.com/dashboard?id=com.github.dadrus%3Ajpa-unit-parent) [![Coverage Status](https://sonarqube.com/api/badges/measure?key=com.github.dadrus:jpa-unit-parent&metric=coverage)](https://sonarqube.com/dashboard?id=com.github.dadrus%3Ajpa-unit-parent) [![Technical Debt](https://sonarqube.com/api/badges/measure?key=com.github.dadrus:jpa-unit-parent&metric=sqale_debt_ratio)](https://sonarqube.com/component_measures/?id=com.github.dadrus%3Ajpa-unit-parent)

Implements [JUnit 4](http://junit.org/junit4) runner and rule, as well as [JUnit 5](http://junit.org/junit5) extension to enable easy testing of javax.persistence entities with an arbitrary persistence provider. Both JPA 2.0, as well as JPA 2.1 is supported (See [Issues](https://github.com/dadrus/jpa-unit/issues) for limitations).

## Features

- Makes use of standard `@PersistenceContext` and `@PersistenceUnit` annotations to inject the `EntityManager`, respectively `EntityManagerFactory`.
- Solely relies on the JPA configuration (`persistence.xml`). No further configuration required.
- Implements automatic transaction management.
- Enables JPA second level cache control 
- Incorporates [DbUnit](http://dbunit.sourceforge.net) to
    - seed the database using predefined data sets (defined in XML, JSON or YAML) or SQL statements
    - cleanup the database before or after the actual test execution based on data sets or arbitrary SQL script
    - execute arbitrary SQL statements before and/or after test execution
    - verify contents of the database after test execution
- Enables bootstrapping of the database schema and contents using plain SQL statements or arbitrary frameworks, like e.g. [FlywayDB](https://flywaydb.org) or [Liquibase](http://www.liquibase.org) before the starting of JPA provider
- Implements seamless integration with CDI.
	
## Credits

The implementation is inspired by the [Arquillian Persistence Extension](http://arquillian.org/modules/persistence-extension). Some of the code fragments are extracted out of it and adopted to suit the needs.

## Maven Integraton

To be able to use the JPA Unit you'll need to add a dependency to your Maven project.

For JUnit 4:

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit4</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

For JUnit 5:

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit5</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

In addition you'll want to add the dependencies for your JPA provider (e.g. [EclipseLink](http://www.eclipse.org/eclipselink) and the database JDBC driver.
E.g.:

```xml
<dependency>
  <groupId>org.eclipse.persistence</groupId>
  <artifactId>eclipselink</artifactId>
  <version>${eclipselink.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <version>${h2.version}</version>
  <scope>test</scope>
</dependency>
```

JPA Unit does not impose any JPA provider dependencies by itself.

## Usage

### Persistence provider configuration

Like in any JPA application, you have to define a `persistence.xml` file in the `META-INF` directory which includes the configuration of your entities and the used JPA provider. For test purposes the `transaction-type` of the configured `persistence-unit` must be `RESOURCE_LOCAL`. The test runner, as well as the rule implementation make use of the standard `javax.persistence.jdbc.driver`, `javax.persistence.jdbc.url`, `javax.persistence.jdbc.user` and `javax.persistence.jdbc.password` properties to access the database directly. Here an example of a `persistence.xml` file which configures [EclipseLink](http://www.eclipse.org/eclipselink) and [H2](http://www.h2database.com/html/main.html) database:

```xml
<persistence version="2.1"
  xmlns="http://xmlns.jcp.org/xml/ns/persistence" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence 
    http://www.oracle.com/webfolder/technetwork/jsc/xml/ns/persistence/persistence_2_1.xsd">
	
  <persistence-unit name="my-test-unit" transaction-type="RESOURCE_LOCAL">
    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>

    <!-- your classes converters, etc -->

    <properties>
      <property name="eclipselink.ddl-generation" value="drop-and-create-tables" />
      <property name="eclipselink.target-database" value="org.eclipse.persistence.platform.database.H2Platform" />
      <property name="javax.persistence.jdbc.driver" value="org.h2.Driver" />
      <property name="javax.persistence.jdbc.url" value="jdbc:h2:mem:serviceEnablerDB;DB_CLOSE_DELAY=-1" />
      <property name="javax.persistence.jdbc.user" value="test" />
      <property name="javax.persistence.jdbc.password" value="test" />
    </properties>
  </persistence-unit>
</persistence>
```

### JPA Unit integration with Junit

JPA Unit follows the concept of configuration by exception whenever possible. To support this concept its API consists mainly of annotations with meaningful defaults (if the annotation is not present) used to drive the test. 

For JUnit 4, the basic requirements on the code level are the presence of either

- the `@RunWith(JpaUnitRunner.class)` annotation on the class level, or
- the `JpaUnitRule` property, annotated with `@Rule`

For JUnit 5, there is no much choice

- the test class needs to be annotated with `@ExtendWith(JpaUnit.class)` annotation.

Irrespectively the JUnit version, the presence of either

- an `EntityManager` property annotated with `@PersistenceContext`. In this case a new `EntityManager` instance is acquired for each test case. On test case exit it is cleared and closed. Furthermore the usage of an `EntityManager` instance managed by JPA Unit, enables automatic transaction management, where a new transaction is started before each test case and committed after the test case returns, respectively the method annotated with `@After`. The `@Transactional` annotation (see below) can be used to overwrite and configure the required behavior.
- or an `EntityManagerFactory` property annotated with `@PersistenceUnit`. In this case the user is responsible for obtaining and closing the required `EntityManager` instance including the corresponding transaction management. There are however some utility functions which can ease the test implementation (see `TransactionSupport` class).

is required.

In both cases the reference to the persistence unit is required as well (e.g. `@PersistenceContext(unitName = "my-test-unit")` or `@PersistenceUnit(unitName = "my-test-unit")`). Thus, given the presence of a persistence provider configuration, like given above, the following examples implement full functional tests.

Example using `JpaUnitRunner`:

```java
@RunWith(JpaUnitRunner.class)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    public void someTest() {
		// your code here
    }
}
```

Example using `JpaUnitRule`:

```java
public class MyTest {

    @Rule
    public JpaUnitRule rule = new JpaUnitRule(getClass());

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    public void someTest() {
		// your code here
    }
}
```

Example using `JpaUnit`:

```java
@ExtendWith(JpaUnit.class)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    public void someTest() {
		// your code here
    }
}
```

Irrespective of the used configuration, the `EntityManagerFactory` instance is acquired once and lives for the duration of the entire test suite implemented by the given test class. That also means one can test the behavior given by the JPA second level cache.

### Control the behavior

To control the test behavior, JPA Unit comes with a handful of annotations and some utility classes. All these annotations can be applied on class and method level, where the latter always takes precedence over the former.

- `@ApplyScriptsAfter`, which can be used to define arbitrary SQL scripts which shall be executed before running the test method.
- `@ApplyScriptsBefore`, which can be used to define arbitrary SQL scripts which shall be executed after running the test method.
- `@Bootstrapping`, which can be used to define a method executed only once before the bootstrapping of a JPA provider happens. This can be handy e.g. to setup a test specific DB schema. 
- `@Cleanup`, which can be used to define when the database cleanup should be triggered.
- `@CleanupCache`, which can be used to define whether and when the JPA L2 cache should be evicted.
- `@CleanupUsingScripts`, which can be used to define arbitrary SQL scripts which shall be used for cleaning the database.
- `@ExpectedDataSets`, which provides the ability to verify the state of underlying database using data sets. Verification is invoked after test's execution.
- `@InitialDataSets`, which provides the ability to seed the database using data sets before test method execution.
- `@Transactional`, which can be used to control the automatic transaction management for a test.
- `TransactionSupport`, comes in handy when fine graned transaction management is required or automatic transaction management is disabled.

All these elements are described in more detail below.

### Transactional tests

Like already written above automatic transaction management is active if the test uses an `EntityManager` instance controlled by JPA Unit. To tweak the required behavior you can use the `@Transactional` annotation either on a test class to apply the same behavior for all tests, or on a single test. This annotation has following properties:

- `value` of type `TransactionMode`. Following modes are available:
    - `COMMIT`. The test is wrapped in a transaction which is committed on return. This is the **default** behavior.
    - `DISABLED`. The transactional support is disabled.
    - `ROLLBACK`. Perform a _rollback_ on test return.
    
Example which disables transactional support for all tests implemented by a given class:

```java
@RunWith(JpaUnitRunner.class)
@Transactional(TransactionMode.DISABLED)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    public void someTest() {
		// your code here
    }
}
```
    
`TransactionSupport` becomes handy, when fine graned transaction management is desired or automatic transaction management is disabled (e.g. the test injects `EntityManagerFactory`). Following methods are available:

- `newTransaction(EntityManager em)` is a static factory method to create new `TransactionSupport` object
- `flushContextOnCommit(boolean flag)` can be used to configure the `TransactionSupport` object to *flush* the `EntityManager` after the transaction is committed.
- `clearContextOnCommit(boolean flag)` can be used to configure the `TransactionSupport` object to *clear* the `EntityManager` after the transaction is committed.
- `execute(<Expression>)` executes the given expression and wraps it in a new transaction. If the expression returns a result, it is returned to the caller. Following behavior is implemented:
    - Before the execution of `<Expression>`: If an active transaction is already running, it is committed and a new transaction is started. Otherwise just a new transaction is started.
    - After the execution of `<Expression>`: The transaction wrapping the `<Expression>` is committed. If an active transaction was running and was committed before the `<Expression>` wrapping transaction was started, a new transaction is started. 
    
Here a usage example:

```java
@RunWith(JpaUnitRunner.class)
@Transactional(TransactionMode.DISABLED)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    public void someTest() {
		newTransaction(manager).execute(() -> {
			// some code wrapped in a transaction
		});
		
		int result = newTransaction(manager)
		.clearContextOnCommit(true)
		.execute(() -> {
			// some code wrapped in a transaction
			return 1;
		});
    }
}
```

### Seeding the database

Creating ad-hoc object graphs in a test to seed the database can be a complex task on the one hand and made the test less readable. On the other hand it is usually not the goal of a test case, rather a prerequisite. To address this, JPA Unit provides an alternative way in a form of database fixtures, which are easy configurable and can be applied for all tests or for a single test. To achieve this JPA Unit uses the concept of data sets from [DbUnit](http://dbunit.sourceforge.net). In essence, data sets are files containing rows to be inserted into the database. JPA Unit supports following data set formats:

- XML (Flat XML Data Set). A simple XML structure, where each element represents a single row in a given table and attribute names correspond to the table columns as illustrated below.
- YAML.
- JSON.
- XSL(X)
- CSV

Here some data set examples:

```xml
<dataset>
	<DEPOSITOR id="100" version="1" name="John" surname="Doe" />
	<ADDRESS id="100" city="SomeCity" country="SomeCountry" street="SomeStreet 1" 
	         zip_code="12345" owner_id="100"/>
	<ADDRESS id="101" city="SomeOtherCity" country="SomeOtherCountry" street="SomeStreet 2" 
	         zip_code="54321" owner_id="100"/>
<dataset>
```

```yaml
DEPOSITOR:
  - id: 100
    version: 1
    name: John
    surname: Doe

ADDRESS:
  - id: 100
    city: SomeCity
    country: SomeCountry
    street: SomeStreet 1
    zip_code: 12345
    owner_id: 100
  - id: 101
    city: SomeOtherCity
    country: SomeOtherCountry
    street: SomeStreet 2
    zip_code: 54321
    owner_id: 100
```

```json
"DEPOSITOR": [
	{ "id": "100", "version": "1", "name": "John", "surname": "Doe" }
],
"ADDRESS": [
	{ "id":"100", "city":"SomeCity", "country": "SomeCountry", "street": "SomeStreet 1", 
	  "zip_code": "12345", "owner_id": "100" },
	{ "id":"101", "city":"SomeOtherCity", "country": "SomeOtherCountry", "street": "SomeStreet 2", 
	  "zip_code": "54321", "owner_id": "100" }
]
```

To seed the database using data set files put the `@InitialDataSets` annotation either on the test itself or on the test class. This annotation has following properties:

- `value` of type `String[]` which takes a list of data set files used to seed the database.
- `seedStrategy` of type `DataSeedStrategy` which can be used to defined the seeding strategy. Following strategies are available (as implemented by DBUnit):
    - `CLEAN_INSERT`. Performs insert of the data defined in provided data sets, after removal of all data present in the tables referred in provided files.
    - `INSERT`. Performs insert of the data defined in provided data sets. This is the **default** strategy.
    - `REFRESH`. During this operation existing rows are updated and new ones are inserted. Entries already existing in the database which are not defined in the provided data set are not affected.
    - `UPDATE`. This strategy updates existing rows using data provided in the data sets. If data set contain a row which is not present in the database (identified by its primary key) then exception is thrown.

Usage example:

```java
@RunWith(JpaUnitRunner.class)
@InitialDataSets("datasets/initial-data.json")
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    public void someTest() {
        // your code here
    }
}
```

### Running custom SQL scripts

Seeding the database as described above introduces an additional abstraction level, which is not always desired on one hand. On other hand, there might be a need to disable specific database constraint checks before a database cleanup might be performed (latter only possible in a post test execution step). Usage of plain SQL comes in handy here to execute any action directly on the database level. Simply put `@ApplyScriptBefore` and/or `@ApplyScriptAfter` annotation either on your test class or directly on your test method. Corresponding scripts will be executed before and/or after test method accordingly. If there is definition on both, test method level annotation takes precedence.

Both annotation have the following properties:

- `value` of type `String[]` which needs to be set to reference the required SQL scripts.
    
Usage example:

```java
@RunWith(JpaUnitRunner.class)
@ApplyScriptBefore("scrips/some-sql-script.sql")
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    @ApplyScriptAfter({
        "scrips/some-other-script-1.sql",
        "scrips/some-other-script-2.sql"
    })
    public void someTest() {
        // your code here
    }
}
```

### Database content verification

### Cleaning database

### Controlling second level cache

The JPA L2 cache can be a two-edged sword if configured or used improperly. Therefore it is crucial to test the corresponding behavior as early as possible. JPA Unit enables this by the usage of the `@CleanupCache` annotation either on a test class, to apply the same behavior for all tests, or on a single test level to define whether and when the JPA L2 cache should be evicted . Please note: The behavior of the second level can be configured in the `persistence.xml`. If `@CleanupCache` is used and the defined `phase` (see below) is not `NONE`, the second level cache will be evicted regardless the settings defined in the `persistence.xml`. This annotation has following properties:

- `phase` of type `CleanupPhase`. Defines the phase when the second level cache cleanup should be triggered. Default phase is `CleanupPhase#AFTER`. Following phases are available:
    - `BEFORE`. The L2 cache is evicted before the test method is executed.
    - `AFTER`. The L2 cache is evicted after the test method is executed.
    - `NONE`. The eviction of the L2 cache is disabled.
    
Example which evicts the JPA L2 cache before the execution of each test method implemented by a given class:

```java
@RunWith(JpaUnitRunner.class)
@CleanupCache(TransactionMode.BEFORE)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    public void someTest() {
		// your code here
    }
}
```

### Bootstrapping of DB schema & contents

Bootstrapping of the data base schema, as well as the handling of its evolution over a period of time is a crucial topic. To enable a data base schema & contents setup close to the productive environment in which the JPA provider usually relies on this given DB setup, the corresponding data base specific actions need to be done before the JPA provider is loaded by accessing the data base directly. JPA Unit enables this by the usage of the `@Bootstrapping` annotation. A dedicated method of a test class, which implements a data base scheme & contents setup can be annotated with this annotation and is required to have one parameter of type `DataSource`. JPA Unit will execute this method very early in its bootstrapping process. Because of this neither `EntityManager` nor `EntityManagerFactory` cannot be used at this time.

For tests, which use this feature, the JPA provider should be configured not to drop and create the data base schema on start, rather to verify it. For e.g. Hibernate this can be achieved by setting the `hibernate.hbm2ddl.auto` property to the value `validate`.

Usage example (bootstrapping with FlywayDB):

```.java
@RunWith(JpaUnitRunner.class)
public class FlywaydbTest {

    @PersistenceContext(unitName = "my-verification-unit")
    private EntityManager manager;

    @Bootstrapping
    public void prepareDataBase(final DataSource ds) {
        // creates db schema and puts some data
        final Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void someTest() {
        // your test specific code here
    }
}
```

### CDI integration

To be able to use the JPA Unit with CDI, all you need in addition to your CDI test dependency, like [DeltaSpike Test-Control Module](https://deltaspike.apache.org/documentation/test-control.html) or [Gunnar's CDI Test](https://github.com/guhilling/cdi-test), is to add the following dependency to your Maven project :

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit-cdi</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

This dependecy implements a CDI extension, which proxies the configured `EntityManager` producer. During a JPA Unit test run it uses the `EntityManager` configured in the test class instance. In all other cases it just uses the proxied producer.

Usage example:

```.java
@RunWith(CdiTestRunner.class)
public class CdiEnabledJpaUnitTest {

    @Rule
    public JpaUnitRule rule = new JpaUnitRule(getClass());

    @PersistenceContext(unitName = "my-test-unit")
    private static EntityManager manager;

    @Inject
    private SomeRepository repo;

    @Test
    public void someTest() {
        // use CDI managed objects, like the repo from above
    }
}
```

## Examples

Here another example which shows the usage of some of the aforementioned annotations:

```java
@RunWith(JpaUnitRunner.class)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    @InitialDataSets("test-data.json")
    @Transactional(TransactionMode.DISABLED)
    public void someReadDataTest() {
		final TestEntity entity = manager.find(TestEntity.class, 1L);
		
		// do something with entity
    }
	
    @Test
    @InitialDataSets("test-data.json")
    @ExpectedDataSets("expected-data.json")
    public void someUpdateDataTest() {
		final TestEntity entity = manager.find(TestEntity.class, 1L);
		
		// update entity. It is attached to the persistence context.
    }
}
```

You can find working examples in the `integration-test` subproject. As for today it implements a simple model and defines four maven profiles to run tests with EclipseLink and Hibernate:

- `jpa2.0-eclipselink`
- `jpa2.1-eclipselink`
- `jpa2.0-hibernate`
- `jpa2.1-hibernate`

## TODOs

- Make the extension available in mavencentral
