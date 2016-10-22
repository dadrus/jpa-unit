# JPA 2.1 persistence test [![Build Status](https://travis-ci.org/dadrus/persistence-test.svg?branch=master)](https://travis-ci.org/dadrus/persistence-test) [![Coverage Status](https://img.shields.io/sonar/http/sonarqube.com/eu.drus.test:persistence-test/coverage.svg?maxAge=3600)](https://sonarqube.com/dashboard/index?id=eu.drus.test%3Apersistence-test) [![Technical Debt](https://img.shields.io/sonar/http/sonarqube.com/eu.drus.test:persistence-test/tech_debt.svg?maxAge=3600)](https://sonarqube.com/dashboard/index?id=eu.drus.test%3Apersistence-test)

Implements [JUnit](http://junit.org) runner to test javax.persistence entities using an arbitrary persistence provider

## Features

- Makes use of standard `@PersistenceContext` annotation to inject the `EntityManager` or `EntityManagerFactory` required by the test and to access the database configuration.
- Implements automatic transaction management
- Incorporates [DbUnit](http://dbunit.sourceforge.net) to
	- seed the database using predefined data sets (defined in XML, JSON or YAML) or SQL statements
	- cleanup the database before or after the actual test execution based on data sets or arbitrary SQL script
	- execute arbitrary SQL statements before and/or after test execution
	- verify contents of the database after test execution
- Close to [Arquillian Persistence Extension](http://arquillian.org/modules/persistence-extension) on the annotation API level
	
## Credits

The implementation makes heavy use of code from the arquillian persistence extension, which was extracted out of it and adopted to suite the needs.
Because of this the license of the Arquillian project apply here as well.

## Maven integraton

Add the following dependencies to your Maven project:

```xml
<dependency>
  <groupId>eu.drus.test</groupId>
  <artifactId>persistence-test</artifactId>
  <version>${persistence-test.version}</version>
  <scope>test</scope>
</dependency>
```

In addition add the dependencies of your JPA 2.1 provider (e.g. [EclipseLink](http://www.eclipse.org/eclipselink) and of the database JDBC driver, you would like to use.
E.g.:

```xml
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>eclipselink</artifactId>
    <version>2.5.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>1.4.191</version>
    <scope>test</scope>
</dependency>
```

## Usage

### Persistence provider configuration

Like in any JPA application, you have to define a `persistence.xml` file in the `META-INF` directory which includes the configuration of your entities
and the used JPA provider. For test purposes the `transaction-type` of the configured `persistence-unit` must be `RESOURCE_LOCAL`. The test runner makes
use of the standard `javax.persistence.jdbc.driver`, `javax.persistence.jdbc.url`, `javax.persistence.jdbc.user` and `javax.persistence.jdbc.password`
properties to access the database directly. Here an example of a `persistence.xml` file which configures [EclipseLink](http://www.eclipse.org/eclipselink)
and [H2](http://www.h2database.com/html/main.html) database:

```xml
<persistence version="2.1"
    xmlns="http://xmlns.jcp.org/xml/ns/persistence" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://www.oracle.com/webfolder/technetwork/jsc/xml/ns/persistence/persistence_2_1.xsd">
	
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

### Test code

The basic requirements on the code level are the presence of the `@RunWith(JpaTestRunner.class)` annotation on the class level and a property of type 
`EntityManager` or `EntityManagerFactory` annotated with `@PersistenceContext` which at least references the required persistence unit:

```java
@RunWith(JpaTestRunner.class)
public class MyTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;
	
	@Test
	public void someTest() {
		// use manager here
	}
}
```

In the above example the `EntityManager` will be injected into the `MyTest` class. The transaction management is done by default and the transaction is
committed after the return of the test method (here `someTest`), respectively the method annotated with `@After`. The `@Transactional` annotation can be
used to overwrite this behavior and to configure the require behavior.

If the `@PersistenceContext` annotation is applied to a property of type `EntityManagerFactory`, no transaction management is done. The user is then
responsible for obtaining and closing the required `EntityManager` instance including the corresponding transaction management.

Irrespective of the transaction management and the usage of the either `EntityManager` or `EntityManagerFactory`, following further annotations are
available to prepare, clean or verify the content of the database in different stages of the test run:

- `@ApplyScriptsAfter`, which can be used to define arbitrary SQL scripts which shall be executed before running the test method.
- `@ApplyScriptsBefore`, which can be used to define arbitrary SQL scripts which shall be executed after running the test method.
- `@Cleanup`, which can be used to define when the database cleanup should be triggered.
- `@CleanupUsingScripts`, which can be used to define arbitrary SQL scripts which shall be used for cleaning the database.
- `@CustomColumnFilter`, which provides ability to define custom column filters. See also [IColumnFilter](http://www.dbunit.org/faq.html#columnfilter).
- `@ExpectedDataSets`, which provides the ability to verify the state of underlying database using data sets. Verification is invoked after test's execution.
- `@InitialDataSets`, which provides the ability to seed the database using data sets before test method execution.

All these annotations can be applied on class and method level, where the latter takes precedence over the former.

Here another example which shows the usage of some of the aforementioned annotations:

```java
@RunWith(JpaTestRunner.class)
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

## TODOs

- Implement tests and examples
- Enable usage with JPA 2.0
- Make the extension available in mavencentral
