package eu.drus.jpa.unit.cassandra.dataset;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.datastax.driver.core.LocalDate;

public enum ColumnType {
    // String types
    TEXT(String.class),
    ASCII(String.class),

    // int types
    INT(int.class),
    BIGINT(long.class),
    SMALINT(short.class),
    TINYINT(byte.class),
    VARINT(BigInteger.class),

    // floating point and decimal numbers
    FLOAT(float.class),
    DOUBLE(double.class),
    DECIMAL(BigDecimal.class),

    // date/time
    TIMESTAMP(Date.class),
    DATE(LocalDate.class),
    TIME(long.class),

    // uuids
    UUID(UUID.class),
    TIMEUUID(UUID.class),

    // bool
    BOOLEAN(boolean.class),

    // binary data
    BLOB(ByteBuffer.class),

    // collections
    LIST(List.class),
    SET(Set.class),
    MAP(Map.class),

    // other types
    COUNTER(long.class),
    INET(InetAddress.class),

    // unknown
    UNKNOWN(String.class);

    private Class<?> javaClass;

    private ColumnType(final Class<?> javaClass) {
        this.javaClass = javaClass;
    }

    public static ColumnType fromString(final String type) {
        for (final ColumnType val : values()) {
            if (val.name().equalsIgnoreCase(type)) {
                return val;
            }
        }

        throw new IllegalArgumentException("No enum constant ColumnType." + type.toUpperCase());
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }
}
