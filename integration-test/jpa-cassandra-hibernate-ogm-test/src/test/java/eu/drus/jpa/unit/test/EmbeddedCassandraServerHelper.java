package eu.drus.jpa.unit.test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.service.CassandraDaemon;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.reader.UnicodeReader;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;

/**
 * @author Jeremy Sevellec
 */
public class EmbeddedCassandraServerHelper {

    private static Logger log = LoggerFactory.getLogger(EmbeddedCassandraServerHelper.class);

    public static final long DEFAULT_STARTUP_TIMEOUT = 20000;
    public static final String DEFAULT_TMP_DIR = "target/embeddedCassandra";
    /** Default configuration file. Starts embedded cassandra under the well known ports */
    public static final String DEFAULT_CASSANDRA_YML_FILE = "cu-cassandra.yaml";
    /** Configuration file which starts the embedded cassandra on a random free port */
    public static final String CASSANDRA_RNDPORT_YML_FILE = "cu-cassandra-rndport.yaml";
    public static final String DEFAULT_LOG4J_CONFIG_FILE = "/log4j-embedded-cassandra.properties";
    private static final String INTERNAL_CASSANDRA_KEYSPACE = "system";
    private static final String INTERNAL_CASSANDRA_AUTH_KEYSPACE = "system_auth";
    private static final String INTERNAL_CASSANDRA_DISTRIBUTED_KEYSPACE = "system_distributed";
    private static final String INTERNAL_CASSANDRA_SCHEMA_KEYSPACE = "system_schema";
    private static final String INTERNAL_CASSANDRA_TRACES_KEYSPACE = "system_traces";

    private static CassandraDaemon cassandraDaemon = null;
    private static String launchedYamlFile;
    private static com.datastax.driver.core.Cluster cluster;
    private static Session session;

    public static void startEmbeddedCassandra() throws IOException, InterruptedException, ConfigurationException {
        startEmbeddedCassandra(DEFAULT_STARTUP_TIMEOUT);
    }

    public static void startEmbeddedCassandra(final long timeout) throws IOException, InterruptedException, ConfigurationException {
        startEmbeddedCassandra(DEFAULT_CASSANDRA_YML_FILE, timeout);
    }

    public static void startEmbeddedCassandra(final String yamlFile) throws IOException, ConfigurationException {
        startEmbeddedCassandra(yamlFile, DEFAULT_STARTUP_TIMEOUT);
    }

    public static void startEmbeddedCassandra(final String yamlFile, final long timeout) throws IOException, ConfigurationException {
        startEmbeddedCassandra(yamlFile, DEFAULT_TMP_DIR, timeout);
    }

    public static void startEmbeddedCassandra(final String yamlFile, final String tmpDir) throws IOException, ConfigurationException {
        startEmbeddedCassandra(yamlFile, tmpDir, DEFAULT_STARTUP_TIMEOUT);
    }

    public static void startEmbeddedCassandra(String yamlFile, final String tmpDir, final long timeout)
            throws IOException, ConfigurationException {
        if (cassandraDaemon != null) {
            /* nothing to do Cassandra is already started */
            return;
        }

        if (!StringUtils.startsWith(yamlFile, "/")) {
            yamlFile = "/" + yamlFile;
        }

        rmdir(tmpDir);
        copy(yamlFile, tmpDir);
        final File file = new File(tmpDir + yamlFile);
        readAndAdaptYaml(file);
        startEmbeddedCassandra(file, tmpDir, timeout);
    }

    public static void startEmbeddedCassandra(final File file, final long timeout) throws IOException, ConfigurationException {
        startEmbeddedCassandra(file, DEFAULT_TMP_DIR, timeout);
    }

    /**
     * Set embedded cassandra up and spawn it in a new thread.
     *
     * @throws TTransportException
     * @throws IOException
     * @throws ConfigurationException
     */
    public static void startEmbeddedCassandra(final File file, final String tmpDir, final long timeout)
            throws IOException, ConfigurationException {
        if (cassandraDaemon != null) {
            /* nothing to do Cassandra is already started */
            return;
        }

        checkConfigNameForRestart(file.getAbsolutePath());

        log.debug("Starting cassandra...");
        log.debug("Initialization needed");

        System.setProperty("cassandra.config", "file:" + file.getAbsolutePath());
        System.setProperty("cassandra-foreground", "true");
        System.setProperty("cassandra.native.epoll.enabled", "false"); // JNA doesnt cope with
                                                                       // relocated netty
        System.setProperty("cassandra.unsafesystem", "true"); // disable fsync for a massive speedup
                                                              // on old platters

        // If there is no log4j config set already, set the default config
        if (System.getProperty("log4j.configuration") == null) {
            copy(DEFAULT_LOG4J_CONFIG_FILE, tmpDir);
            System.setProperty("log4j.configuration", "file:" + tmpDir + DEFAULT_LOG4J_CONFIG_FILE);
        }

        DatabaseDescriptor.daemonInitialization();

        cleanupAndLeaveDirs();
        final CountDownLatch startupLatch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                cassandraDaemon = new CassandraDaemon();
                cassandraDaemon.activate();
                startupLatch.countDown();
            }
        });
        try {
            if (!startupLatch.await(timeout, MILLISECONDS)) {
                log.error("Cassandra daemon did not start after " + timeout + " ms. Consider increasing the timeout");
                throw new AssertionError("Cassandra daemon did not start within timeout");
            }

            final QueryOptions queryOptions = new QueryOptions();
            queryOptions.setRefreshSchemaIntervalMillis(0);
            queryOptions.setRefreshNodeIntervalMillis(0);
            queryOptions.setRefreshNodeListIntervalMillis(0);
            cluster = com.datastax.driver.core.Cluster.builder().addContactPoints(EmbeddedCassandraServerHelper.getHost())
                    .withPort(EmbeddedCassandraServerHelper.getNativeTransportPort()).withQueryOptions(queryOptions).build();

            session = cluster.connect();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    session.close();
                    cluster.close();
                }
            }));
        } catch (final InterruptedException e) {
            log.error("Interrupted waiting for Cassandra daemon to start:", e);
            throw new AssertionError(e);
        } finally {
            executor.shutdown();
        }
    }

    private static void checkConfigNameForRestart(final String yamlFile) {
        final boolean wasPreviouslyLaunched = launchedYamlFile != null;
        if (wasPreviouslyLaunched && !launchedYamlFile.equals(yamlFile)) {
            throw new UnsupportedOperationException("We can't launch two Cassandra configurations in the same JVM instance");
        }
        launchedYamlFile = yamlFile;
    }

    /**
     * Now deprecated, previous version was not fully operating. This is now an empty method, will
     * be pruned in future versions.
     */
    @Deprecated
    public static void stopEmbeddedCassandra() {
        log.warn("EmbeddedCassandraServerHelper.stopEmbeddedCassandra() is now deprecated, " + "previous version was not fully operating");
        cassandraDaemon.deactivate();
    }

    /**
     * drop all keyspaces (expect system)
     */
    public static void cleanEmbeddedCassandra() {
        dropKeyspaces();
    }

    /**
     * truncate data in keyspace, except specified tables
     */
    public static void cleanDataEmbeddedCassandra(final String keyspace, final String... excludedTables) {
        cleanDataWithNativeDriver(keyspace, excludedTables);
    }

    public static com.datastax.driver.core.Cluster getCluster() {
        return cluster;
    }

    public static Session getSession() {
        return session;
    }

    /**
     * Get the embedded cassandra cluster name
     *
     * @return the cluster name
     */
    public static String getClusterName() {
        return DatabaseDescriptor.getClusterName();
    }

    /**
     * Get embedded cassandra host.
     *
     * @return the cassandra host
     */
    public static String getHost() {
        return DatabaseDescriptor.getRpcAddress().getHostName();
    }

    /**
     * Get embedded cassandra RPC port.
     *
     * @return the cassandra RPC port
     */
    public static int getRpcPort() {
        return DatabaseDescriptor.getRpcPort();
    }

    /**
     * Get embedded cassandra native transport port.
     *
     * @return the cassandra native transport port.
     */
    public static int getNativeTransportPort() {
        return DatabaseDescriptor.getNativeTransportPort();
    }

    private static void cleanDataWithNativeDriver(final String keyspace, final String... excludedTables) {
        final KeyspaceMetadata keyspaceMetadata = cluster.getMetadata().getKeyspace(keyspace);
        final Collection<TableMetadata> tables = keyspaceMetadata.getTables();
        final List<String> excludeTableList = Arrays.asList(excludedTables);
        for (final TableMetadata table : tables) {
            final String tableName = table.getName();
            if (!excludeTableList.contains(tableName)) {
                session.execute("truncate table " + keyspace + "." + tableName);
            }
        }
    }

    private static void dropKeyspaces() {
        dropKeyspacesWithNativeDriver();
    }

    private static void dropKeyspacesWithNativeDriver() {
        final List<String> keyspaces = new ArrayList<>();
        for (final com.datastax.driver.core.KeyspaceMetadata keyspace : cluster.getMetadata().getKeyspaces()) {
            if (!isSystemKeyspaceName(keyspace.getName())) {
                keyspaces.add(keyspace.getName());
            }
        }
        for (final String keyspace : keyspaces) {
            session.execute("DROP KEYSPACE " + keyspace);
        }
    }

    private static boolean isSystemKeyspaceName(final String keyspaceName) {
        return INTERNAL_CASSANDRA_KEYSPACE.equals(keyspaceName) || INTERNAL_CASSANDRA_AUTH_KEYSPACE.equals(keyspaceName)
                || INTERNAL_CASSANDRA_DISTRIBUTED_KEYSPACE.equals(keyspaceName) || INTERNAL_CASSANDRA_SCHEMA_KEYSPACE.equals(keyspaceName)
                || INTERNAL_CASSANDRA_TRACES_KEYSPACE.equals(keyspaceName);
    }

    private static void rmdir(final String dir) throws IOException {
        final File dirFile = new File(dir);
        if (dirFile.exists()) {
            FileUtils.deleteRecursive(new File(dir));
        }
    }

    /**
     * Copies a resource from within the jar to a directory.
     *
     * @param resource
     * @param directory
     * @throws IOException
     */
    private static void copy(final String resource, final String directory) throws IOException {
        mkdir(directory);
        final String fileName = resource.substring(resource.lastIndexOf("/") + 1);
        final File file = new File(directory + System.getProperty("file.separator") + fileName);
        try (InputStream is = EmbeddedCassandraServerHelper.class.getResourceAsStream(resource);
                OutputStream out = new FileOutputStream(file)) {
            final byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
        }
    }

    /**
     * Creates a directory
     *
     * @param dir
     * @throws IOException
     */
    private static void mkdir(final String dir) throws IOException {
        FileUtils.createDirectory(dir);
    }

    private static void cleanupAndLeaveDirs() throws IOException {
        mkdirs();
        cleanup();
        mkdirs();
        final CommitLog commitLog = CommitLog.instance;
        commitLog.resetUnsafe(true); // cleanup screws w/ CommitLog, this brings it back to safe
                                     // state
    }

    private static void cleanup() throws IOException {
        // clean up commitlog
        final String[] directoryNames = {
                DatabaseDescriptor.getCommitLogLocation(),
        };
        for (final String dirName : directoryNames) {
            final File dir = new File(dirName);
            if (!dir.exists()) {
                throw new RuntimeException("No such directory: " + dir.getAbsolutePath());
            }
            FileUtils.deleteRecursive(dir);
        }

        // clean up data directory which are stored as data directory/table/data
        // files
        for (final String dirName : DatabaseDescriptor.getAllDataFileLocations()) {
            final File dir = new File(dirName);
            if (!dir.exists()) {
                throw new RuntimeException("No such directory: " + dir.getAbsolutePath());
            }
            FileUtils.deleteRecursive(dir);
        }
    }

    public static void mkdirs() {
        DatabaseDescriptor.createAllDirectories();
    }

    private static void readAndAdaptYaml(final File cassandraConfig) throws IOException {
        final String yaml = readYamlFileToString(cassandraConfig);

        // read the ports and replace them if zero. dump back the changed string, preserving
        // comments (thus no snakeyaml)
        final Pattern portPattern = Pattern.compile("^([a-z_]+)_port:\\s*([0-9]+)\\s*$", Pattern.MULTILINE);
        final Matcher portMatcher = portPattern.matcher(yaml);
        final StringBuffer sb = new StringBuffer();
        boolean replaced = false;
        while (portMatcher.find()) {
            final String portName = portMatcher.group(1);
            int portValue = Integer.parseInt(portMatcher.group(2));
            String replacement;
            if (portValue == 0) {
                portValue = findUnusedLocalPort();
                replacement = portName + "_port: " + portValue;
                replaced = true;
            } else {
                replacement = portMatcher.group(0);
            }
            portMatcher.appendReplacement(sb, replacement);
        }
        portMatcher.appendTail(sb);

        if (replaced) {
            writeStringToYamlFile(cassandraConfig, sb.toString());
        }
    }

    private static String readYamlFileToString(final File yamlFile) throws IOException {
        // using UnicodeReader to read the correct encoding according to BOM
        try (UnicodeReader reader = new UnicodeReader(new FileInputStream(yamlFile))) {
            final StringBuffer sb = new StringBuffer();
            final char[] cbuf = new char[1024];

            int readden = reader.read(cbuf);
            while (readden >= 0) {
                sb.append(cbuf, 0, readden);
                readden = reader.read(cbuf);
            }
            return sb.toString();
        }
    }

    private static void writeStringToYamlFile(final File yamlFile, final String yaml) throws IOException {
        // write utf-8 without BOM
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(yamlFile), "utf-8")) {
            writer.write(yaml);
        }
    }

    private static int findUnusedLocalPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

}
