# JPA Unit [![Build Status](https://travis-ci.org/dadrus/jpa-unit.svg?branch=master)](https://travis-ci.org/dadrus/jpa-unit) [![Quality Gate](https://sonarqube.com/api/badges/gate?key=com.github.dadrus.jpa-unit:jpa-unit)](https://sonarqube.com/dashboard?id=com.github.dadrus.jpa-unit%3Ajpa-unit) [![Coverage Status](https://sonarqube.com/api/badges/measure?key=com.github.dadrus.jpa-unit:jpa-unit&metric=coverage)](https://sonarqube.com/dashboard?id=com.github.dadrus.jpa-unit%3Ajpa-unit) [![Technical Debt](https://sonarqube.com/api/badges/measure?key=com.github.dadrus.jpa-unit:jpa-unit&metric=sqale_debt_ratio)](https://sonarqube.com/component_measures/?id=com.github.dadrus.jpa-unit%3Ajpa-unit)

Implements [JUnit 4](http://junit.org/junit4) runner and rule, as well as [JUnit 5](http://junit.org/junit5) extension to enable easy testing of javax.persistence entities with an arbitrary persistence provider. Both JPA 2.0, as well as JPA 2.1 is supported (See [Issues](https://github.com/dadrus/jpa-unit/issues) for limitations).

## Features

- Makes use of standard `@PersistenceContext` and `@PersistenceUnit` annotations to inject the `EntityManager`, respectively `EntityManagerFactory`.
- Solely relies on the JPA configuration (`persistence.xml`). No further JPA Unit specific configuration required. 
- Does not impose any JPA provider dependencies.
- Implements automatic transaction management.
- Enables JPA second level cache control 
- Offers different strategies to
    - seed the database using predefined data sets (depending on the used data base - defined in XML, JSON, YAML or SQL statements)
    - cleanup the database before or after the actual test execution based on data sets or arbitrary scripts
    - execute arbitrary scripts before and/or after test execution
    - verify contents of the database after test execution
- Enables bootstrapping of the database schema and contents using plain data base statements (e.g. SQL) or arbitrary frameworks, like e.g. [FlywayDB](https://flywaydb.org) or [Liquibase](http://www.liquibase.org) before the starting of JPA provider
- Implements seamless integration with CDI.
- Supports acceptance based testing using [Cucumber](https://cucumber.io/docs/reference/jvm#java)
- Supports SQL and NoSQL (see below for a list of supported NoSQL databases and known limitations) databases (based on what is possible with the chosen JPA provider).
	
## Credits

The implementation is inspired by the [Arquillian Persistence Extension](http://arquillian.org/modules/persistence-extension). Some of the code fragments are extracted out of it and adopted to suit the needs.

# Maven Integraton

To be able to use the JPA Unit you will have to add some dependencies to your Maven project. For easier dependency management, there is a bom available which you can add to your `dependencyManagement` section:

```xml
<dependencyManagement>
  <dependency>
    <groupId>com.github.dadrus</groupId>
    <artifactId>jpa-unit-bom</artifactId>
    <version>${jpa-unit.version}</version>
    <type>pom</type>
    <scope>import</scope>
  </dependency>
</dependencyManagement>
```

The actual dependencies are listed in sections addressing the different possible integration types.

## JPA Unit with JUnit 4

To work with JUnit 4, you would need to add `jpa-unit4` to your test dependencies:

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit4</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

The basic requirements on the code level are the presence of either

- the `@RunWith(JpaUnitRunner.class)` annotation on the class level, or
- the `JpaUnitRule` property, annotated with `@Rule`

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

## JPA Unit with JUnit 5

To work with JUnit 5, you would need to add `jpa-unit5` to your test dependencies:

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit5</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

On the code level, there is no much choice for JUnit 5

- the test class needs to be annotated with `@ExtendWith(JpaUnit.class)` annotation.

Example:

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


## Basic Configuration

Irrespectively the JUnit version, the presence of either

- an `EntityManager` property annotated with `@PersistenceContext`. In this case a new `EntityManager` instance is acquired for each test case. On test case exit it is cleared and closed. Furthermore the usage of an `EntityManager` instance managed by JPA Unit, enables automatic transaction management, where a new transaction is started before each test case and committed after the test case returns, respectively the method annotated with `@After`. The `@Transactional` annotation (see below) can be used to overwrite and configure the required behavior.
- or an `EntityManagerFactory` property annotated with `@PersistenceUnit`. In this case the user is responsible for obtaining and closing the required `EntityManager` instance including the corresponding transaction management. There are however some utility functions which can ease the test implementation (see `TransactionSupport` class).

is required. Irrespective of the used configuration, the `EntityManagerFactory` instance is acquired once and lives for the duration of the entire test suite implemented by the given test class.

In both cases the reference to the persistence unit is required as well (e.g. `@PersistenceContext(unitName = "my-test-unit")` or `@PersistenceUnit(unitName = "my-test-unit")`). Thus, given the presence of a persistence provider configuration, the examples, shown above, already implement full functional tests.

Like in any JPA application, you have to define a `persistence.xml` file in the `META-INF` directory which includes the JPA provider and `persistence-unit` configuration. 
For test purposes the `transaction-type` of the configured `persistence-unit` must be `RESOURCE_LOCAL`. 

# Control the Behavior

To control the test behavior, JPA Unit comes with a handful of annotations and some utility classes. All these annotations can be applied on class and method level, where the latter always takes precedence over the former.
JPA Unit follows the concept of configuration by exception whenever possible. To support this concept its API consists mainly of annotations with meaningful defaults (if the annotation is not present) used to drive the test. 

- `@ApplyScriptsAfter`, which can be used to define arbitrary scripts which shall be executed before running the test method.
- `@ApplyScriptsBefore`, which can be used to define arbitrary scripts which shall be executed after running the test method.
- `@Bootstrapping`, which can be used to define a method executed only once before the bootstrapping of a JPA provider happens. This can be handy e.g. to setup a test specific DB schema. 
- `@Cleanup`, which can be used to define when the database cleanup should be triggered.
- `@CleanupCache`, which can be used to define whether and when the JPA L2 cache should be evicted.
- `@CleanupUsingScripts`, which can be used to define arbitrary scripts which shall be used for cleaning the database.
- `@ExpectedDataSets`, which provides the ability to verify the state of underlying database using data sets. Verification is invoked after test's execution.
- `@InitialDataSets`, which provides the ability to seed the database using data sets before test method execution.
- `@Transactional`, which can be used to control the automatic transaction management for a test if supported by the chosen JPA provider for the chosen database. Otherwise it does not have any effect.
- `TransactionSupport`, comes in handy when fine graned transaction management is required or automatic transaction management is disabled. As for the `@Transactional` annotation, if not supported by the chosen JPA provider for the chosen database, the usage of these functions has no effect.

All these elements are described in more detail below.

## Transactional Tests

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

## Seeding the Database

Creating ad-hoc object graphs in a test to seed the database can be a complex task on the one hand and made the test less readable. On the other hand it is usually not the goal of a test case, rather a prerequisite. 
To address this, JPA Unit provides an alternative way in a form of database fixtures, which are easy configurable and can be applied for all tests or for a single test. To achieve this JPA Unit uses the concept of data sets.
In essence, data sets are files containing data to be inserted into the database. Since data sets are database specific, see the corresponding database specific sections for details on supported types and formats.

To seed the database using data set files put the `@InitialDataSets` annotation either on the test itself or on the test class. This annotation has following properties:

- `value` of type `String[]` which takes a list of data set files used to seed the database.
- `seedStrategy` of type `DataSeedStrategy` which can be used to defined the seeding strategy. Following strategies are available:
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

## Running Custom Catabase Scripts

Seeding the database as described above introduces an additional abstraction level, which is not always desired on one hand. On other hand, there might be a need to disable specific database constraint checks before a database cleanup might be performed (latter only possible in a post test execution step). Usage of plain scripts (e.g. SQL) comes in handy here to execute any action directly on the database level. Simply put `@ApplyScriptBefore` and/or `@ApplyScriptAfter` annotation on your test class and/or directly on your test method. Corresponding scripts will be executed before and/or after test method accordingly. If there is definition on both, test method level annotation takes precedence.

Both annotation have the following properties:

- `value` of type `String[]` which needs to be set to reference the required database specific scripts (e.g. SQL for an SQL database).
    
Usage example:

```java
@RunWith(JpaUnitRunner.class)
@ApplyScriptBefore("scrips/some-script.script")
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    @ApplyScriptAfter({
        "scrips/some-other-script-1.script",
        "scrips/some-other-script-2.script"
    })
    public void someTest() {
        // your code here
    }
}
```

## Database Content Verification

Asserting database state directly from testing code might imply a huge amount of work. `@ExpectedDataSets` comes in handy here. Just put this annotation either on a test class to apply the same assertions for all tests, or on a single test method (the latter takes precedence) and JPA Unit will use the referenced files to check whether the database contains entries you are expecting after the test execution. 

The `@ExpectedDataSets` annotation has the following properties:

- `value` of type `String[]` which takes a list of data set files used for post-test verification of the database's state.
- `orderBy` of type `String[]` which takes a list of columns to be used for sorting rows to determine the order of data sets comparison (Not supported by all database types).
- `excludeColumns` of type `String[]` which takes a list of columns to be excluded during the comparison.
- `filter` of type `Class<?>[]` which takes a list of custom filters to be applied during verification in the specified order (Not supported by all database types)
- `strict` of type `boolean` which defines whether the performed verification about expected data sets is strict or not. In strict mode all tables/collections and entries not defined in the expected data sets are considered to be an error. This is the **default** strategy.

Both `orderBy` and `excludeColumns` properties can be used to define columns with and without dotted notation. With dotted notation one can explicitly define the table/collection in addition to the actual field/property (see also the example below).

Usage example:

```java
@RunWith(JpaUnitRunner.class)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    private Depositor depositor;

    @Before
    public void createTestData() throws OperationNotSupportedException {
        depositor = new Depositor("Max", "Doe");
        depositor.addContactDetail(new ContactDetail(ContactType.EMAIL, "john.doe@acme.com"));
        depositor.addContactDetail(new ContactDetail(ContactType.TELEPHONE, "+1 22 2222 2222"));
        depositor.addContactDetail(new ContactDetail(ContactType.MOBILE, "+1 11 1111 1111"));
        final InstantAccessAccount instantAccessAccount = new InstantAccessAccount(depositor);
        final GiroAccount giroAccount = new GiroAccount(depositor);
        giroAccount.setCreditLimit(1000.0f);
        giroAccount.deposit(100.0f);
        giroAccount.transfer(150.0f, instantAccessAccount);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test1() {
        manager.persist(depositor);
    }
}
```

## Cleaning the Database

### Strategy based Cleanup

By default the database content is entirely erased before each test. If you want to control this behavior, `@Cleanup` annotation is your friend. It defines when database cleanup should be triggered and which cleanup strategy to apply. 
As always, you can use this annotation globally on a class level or on a method level. The latter takes precedence.

The `@Cleanup` annotation has the following properties:

- `strategy` of type `CleanupStrategy`. Defines which strategy to apply while erasing the database content. Following strategies are available:
    - `STRICT`. Cleans entire database. This is the **default** strategy. Might require turning off database constraints (e.g. referential integrity).
    - `USED_ROWS_ONLY`. Deletes only those entries which were defined in data sets.
    - `USED_TABLES_ONLY`. Deletes only those tables/collections which were used in data sets.
- `phase` of type `CleanupPhase`. Defines the phase when the database cleanup should be triggered. Following phases are available:
    - `BEFORE`. The contents of database are deleted (based on the strategy) before the test method is executed.
    - `AFTER`. The contents of database are deleted (based on the strategy) after the test method is executed. This is the **default** phase.
    - `NONE`. The cleanup of the database is disabled.
    

Usage example:

```java
@RunWith(JpaUnitRunner.class)
@Cleanup(TransactionMode.BEFORE) // change the default phase from AFTER to BEFORE
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
    @Test
    public void someTest() {
        // your code here
    }
    
    @Test
    @Cleanup(phase = CleanupPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
    public void otherTest() {
        // your code here
    }
}
```

### Using Custom Scripts

If automatic cleanup as described in [Strategy based Cleanup](#strategy-based-cleanup) does not suit your needs, `@CleanupUsingScripts` might be your friend. You can use it to execute custom scripts to clean your database before or after the test. Just put this annotation either on the test itself or on the test class. As always the definition applied on the test method level takes precedence.

This annotation has following properties:

- `value` of type `String[]` which needs to be set to reference the required database specific cleanup scripts.
- `phase` of type `CleanupPhase`. Defines the phase when the cleanup scripts should be executed. Following phases are available:
    - `BEFORE`. Before the test method is executed.
    - `AFTER`. After the test method is executed. This is the **default** phase.
    - `NONE`. The execution of scripts is disabled.
    
Usage example:

```java
@RunWith(JpaUnitRunner.class)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
    
    @Test
    @CleanupUsingScripts(phase = CleanupPhase.AFTER, value = "scripts/delete-all.script")
    public void otherTest() {
        // your code here
    }
}
```

## Controlling Second Level Cache

The JPA L2 cache can be a two-edged sword if configured or used improperly. Therefore it is crucial to test the corresponding behavior as early as possible. JPA Unit enables this by the usage of the `@CleanupCache` annotation either on a test class, to apply the same behavior for all tests, or on a single test level to define whether and when the JPA L2 cache should be evicted . Please note: The behavior of the second level can be configured in the `persistence.xml`. If `@CleanupCache` is used and the defined `phase` (see below) is not `NONE`, the second level cache will be evicted regardless the settings defined in the `persistence.xml`. This annotation has following properties:

- `phase` of type `CleanupPhase`. Defines the phase when the second level cache cleanup should be triggered. Following phases are available:
    - `BEFORE`. The L2 cache is evicted before the test method is executed.
    - `AFTER`. The L2 cache is evicted after the test method is executed. This is the **default** phase.
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

## Bootstrapping of DB Schema & Contents

Bootstrapping of the data base schema, as well as the handling of its evolution over a period of time is a crucial topic. To enable a data base schema & contents setup close to the productive environment in which the JPA provider usually relies on this given DB setup, the corresponding database specific actions need to be done before the JPA provider is loaded by accessing the data base directly. JPA Unit enables this by the usage of the `@Bootstrapping` annotation. A dedicated method of a test class, which implements a data base scheme & contents setup can be annotated with this annotation and is required to have one parameter of type `DataSource`. JPA Unit will execute this method very early in its bootstrapping process. Because of this neither `EntityManager` nor `EntityManagerFactory` cannot be used at this time.

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

# Supported Databases

Depending on the used database, you will have to add a dependency for a database specific JPA-Unit plugin.

## SQL Databases

For all SQL databases the `jpa-unit-sql` dependency needs to be added:

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit-sql</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

For SQL databases JPA Unit makes use of the standard 
- `javax.persistence.jdbc.driver`,
- `javax.persistence.jdbc.url`,
- `javax.persistence.jdbc.user` and
- `javax.persistence.jdbc.password` 

properties to access the database directly.

Here an example of a `persistence.xml` file which configures [EclipseLink](http://www.eclipse.org/eclipselink) and [H2](http://www.h2database.com/html/main.html) database:

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
      <property name="eclipselink.target-database" 
          value="org.eclipse.persistence.platform.database.H2Platform" />
      <property name="javax.persistence.jdbc.driver" value="org.h2.Driver" />
      <property name="javax.persistence.jdbc.url" 
          value="jdbc:h2:mem:serviceEnablerDB;DB_CLOSE_DELAY=-1" />
      <property name="javax.persistence.jdbc.user" value="test" />
      <property name="javax.persistence.jdbc.password" value="test" />
    </properties>
  </persistence-unit>
</persistence>
```

### Data Set Format

For SQL databases JPA Unit uses [DBUnit](http://dbunit.sourceforge.net/) initernally. Thanks to DBUnit, following data set formats are supported:

- XML (Flat XML Data Set). A simple XML structure, where each element represents a single row in a given table and attribute names correspond to the table columns as illustrated below.
- YAML. Similar to the flat XML layout, but has some improvements (columns are calculated by parsing the entire data set, not just the first row)
- JSON. Similar to YAML.
- XSL(X). With this data set format each sheet represents a table. The first row of a sheet defines the columns names and remaining rows contains the data.
- CSV. Here a data set can be constructed from a directory containing csv files, each representing a separate table with its entries.


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



## MongoDB

For [MongoDB](https://www.mongodb.com), the `jpa-unit-mongodb` dependency needs to be added:

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit-mongodb</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

JPA Unit needs to connect to a running MongoDB instance. This is done using [mongo-java-driver](https://mongodb.github.io/mongo-java-driver/). Usage of an in-process, in-memory MongoDB implementations, like [Fongo](https://github.com/fakemongo/fongo) is not possible.
To overcome this limitation, or made it at least less painful, one can use e.g.

- [Flapdoodle Embedded MongoDB](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo) for the lifecycle management of a MongoDB instance from code, e.g. from `@BeforeClass` and `@AfterClass` annotated methods. You can find working example within the JPA Unit integration test project for MongoDB. 
- [embedmongo-maven-plugin](https://github.com/joelittlejohn/embedmongo-maven-plugin) for the lifecycle management of a MongoDB instance through Maven.

### JPA Provider Dependencies

Since JPA does not address NoSQL databases, each JPA provider defines its own properties. These properties are also the only dependencies to a specific JPA provider implementation. As of todays JPA Unit MongoDB extension can use the properties of the following JPA provider:

- [Hibernate OGM (with MongoDB extension)](https://docs.jboss.org/hibernate/ogm/5.1/reference/en-US/html_single/#ogm-mongodb).
- [EclipseLink (with MongoDB extension)](https://www.eclipse.org/eclipselink/documentation/2.6/concepts/nosql002.htm)
- [DataNucleus (with MongoDB extension)](http://www.datanucleus.org/products/datanucleus/jpa/samples/tutorial_mongodb.html)
- [Kundera (with MongoDB extension)](https://github.com/impetus-opensource/Kundera/wiki/Kundera-with-MongoDB)

### Data Set Format

Default data set format for MongoDB is _JSON_. In a simple case it must comply with the following example structure:

```.json
{
  "collection_name_1": [
    {
      "property_1": "value_1",
      "property_2": "value_2"
    },
    {
      "property_3": NumberLong(10),
      "property_4": { "$date": "2017-06-07T15:19:10.460Z" }
    }
  ],
  
  "collection_name_2": [
    {
      "property_5": 4,
      "property_7": "value_7"
    }
  ]
}
```

If indexes (for more information on MongoDB indexes and types see [MongoDB Indexes](https://docs.mongodb.com/manual/indexes/)) need to be included as well, the following structure applies:

```.json
{
  "collection_name_1": {
    "indexes": [
      {
        "index": {
          "property_1": 1
        },
        "index": {
          "property_2": 1,
          "options": {
            "unique": true,
            "default_language": "english"
          }
        }
      },
      {
        "index": {
          "property_3": 1,
          "property_4": -1
        }
      }
    ],
    "data": [
      {
        "property_1": "value_1",
        "property_2": "value_2"
      },
      {
        "property_3": NumberLong(10),
        "property_4": { "$date": "2017-06-07T15:19:10.460Z" }
      }
    ]
  }
}
``` 

Please note, that in this case the collection document consists of two subdocuments. The first one - `indexes` is where the indexes are defined. Basically this is which fields of the collection are going to be indexed.
The second one - `data`, where all documents, which belong to the collection under test, are defined. In both cases all the types defined by MongoDB are supported.

# CDI Integration

To be able to use the JPA Unit with CDI, all you need in addition to your CDI test dependency, like [DeltaSpike Test-Control Module](https://deltaspike.apache.org/documentation/test-control.html) or [Gunnar's CDI Test](https://github.com/guhilling/cdi-test), is to add the following dependency to your Maven project :

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit-cdi</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

This dependency implements a CDI extension, which proxies the configured `EntityManager` producer. During a JPA Unit test run it uses the `EntityManager` configured in the test class instance. In all other cases it just uses the proxied producer.

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

# Cucumber Integration

Cucumber is a BDD test framework. To be able to use JPA Unit with it, all you need in addition to cucumber dependencies is to add the following dependency to your Maven project :

```xml
<dependency>
  <groupId>com.github.dadrus</groupId>
  <artifactId>jpa-unit-cucumber</artifactId>
  <version>${jpa-unit.version}</version>
  <scope>test</scope>
</dependency>
```

This dependency implements a Cucumber extension (`ObjectFactory`) which intercepts all cucumber feature glue methods to enable the usage of JPA Unit annotations. 

Since each feature/scenario glue, compared to regular JUnit tests, implements a single test specification, JPA Unit disables automatic data base cleanup. To avoid stale data
between the executions of different scenarios or more general different tests, you should take care of the cleanup by yourself. This is the only difference to the regular behavior. 
This cleanup can be achieved, e.g. using the `@Cleanup` annotation on e.g. a method annotated with the cucumber `@After` annotation.

Analogue to regular JUnit tests a cucumber glue needs to reference either an `EntityManager` or an `EntityManagerFactory`. The `EntityManagerFactory` lives for the
duration of the scenario execution.

Same rules as for regular JUnit tests apply for the `EntityManager` as well: An `EntityManager` for `TRANSACTION` `PersistenceContextType` lives only for the duration
of the execution of the glue method. An `EntityManager` for `EXTENDED` `PersistenceContextType` has the life time of the `EntityManagerFactory` and is closed after the
last glue method is executed. Latter configuration might be a better choice for cucumber glue.

Usage example:

```.java
@RunWith(Cucumber.class)
public class CucumberTest {
    // According to cucumber, this class should not implement any tests
}
```

```.java
public class CucumberGlue {

    @Rule
    public JpaUnitRule rule = new JpaUnitRule(getClass());

    @PersistenceContext(unitName = "my-test-unit", type = PersistenceContextType.EXTENDED)
    private EntityManager manager;
  
    @Given("^an some existing data in the db$")
    @InitialDataSets("datasets/initial-data.json")
    public void seedDatabase() {
        // data base is seeded thanks to the jpa unit annotation
    }
  
    @When("^a new object is inserted$")
    public void updataData() {
        // e.g. manager.persist(new SomeEntity());
    }
  
    @Then("^it is expected in the db")
    @ExpectedDataSets(value = "datasets/expected-data.json")
    public void verifyData() {
        // db verification is done thanks to the jpa unit annotation
    }
  
    @After
    @Cleanup
    public void cleanupDb() {
        // to avoid stale data
    } 
}
```

If you would like to use cucumber with JPA Unit and CDI, you will have to follow the requirements from [CDI Integration](#cdi-integration). However, since
the usage of multiple runners is not possible, you'll have to start the CDI container manually. Here an example with Deltaspike:

```.java
@RunWith(Cucumber.class)
public class CucumberTest {
    // According to cucumber, this class should not implement any tests
    // but we can implement global setup and tear down functionality here.
  
    private static CdiContainer cdiContainer;

    @BeforeClass
    public static void startContainer() {
        cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();
    }

    @AfterClass
    public static void stopContainer() {
        cdiContainer.shutdown();
    }
}
```

Now you can inject your dependencies into glue objects.