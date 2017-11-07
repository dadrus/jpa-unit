package eu.drus.jpa.unit.neo4j.ext;

import javax.sql.DataSource;

public interface Configuration {

    DataSource createDataSource();
}
