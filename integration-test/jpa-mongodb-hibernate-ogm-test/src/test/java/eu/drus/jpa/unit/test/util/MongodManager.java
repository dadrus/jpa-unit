package eu.drus.jpa.unit.test.util;

import static de.flapdoodle.embed.process.io.Processors.logTo;
import static de.flapdoodle.embed.process.io.Processors.named;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.HostAndPort;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import eu.drus.jpa.unit.suite.MongoSuite;

// Unfortunately JUnit 5 breaks the contract of JUnit 4 regarding the order of @BeforeAll/@AfterAll
// With JUnit 4 these methods are executed before any extensions/rules are executed. With JUnit 5
// these methods are executed after the execution of the corresponding functionality of the
// extension. To circumvent this, we let this class implement the JUnit 5 callbacks and register
// this extension before the JpaUnit extension
public class MongodManager implements BeforeAllCallback, AfterAllCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodManager.class.getName());

    private transient Map<MongodProcess, MongodExecutable> mongoProcesses = new HashMap<>();

    private static final MongodManager MONGO_MANAGER = new MongodManager();

    public synchronized static void start(final MongodConfiguration config) {
        if (!MONGO_MANAGER.isMongoRunning()) {
            MONGO_MANAGER.startMongod(config);
        }
    }

    public synchronized static void stop() throws InterruptedException {
        if (MONGO_MANAGER.isMongoRunning()) {
            MONGO_MANAGER.stopMongod();
        }
    }

    private boolean isMongoRunning() {
        return !mongoProcesses.isEmpty();
    }

    private void startMongod(final MongodConfiguration config) {

        List<IMongodConfig> mongodConfigList;
        try {
            if (config.getHosts().size() == 1) {
                mongodConfigList = buildMongodConfiguration(config.getHosts(), false);
                startMongo(mongodConfigList);
            } else if (config.getHosts().size() >= 3) {
                mongodConfigList = buildMongodConfiguration(config.getHosts(), true);
                startMongo(mongodConfigList);
                initializeReplicaSet(mongodConfigList);
            } else {
                throw new RuntimeException("It looks like replica set is configured. A replica set however requires at least 3 hosts");
            }

        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startMongo(final List<IMongodConfig> mongodConfigList) throws IOException {
        // @formatter:off
        final ProcessOutput processOutput = new ProcessOutput(
                logTo(LOGGER, Slf4jLevel.INFO),
                logTo(LOGGER, Slf4jLevel.ERROR),
                named("[console>]", logTo(LOGGER, Slf4jLevel.DEBUG)));

        final IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaultsWithLogger(Command.MongoD,LOGGER)
                .processOutput(processOutput)
                .artifactStore(new ExtractedArtifactStoreBuilder()
                    .defaults(Command.MongoD)
                    .download(new DownloadConfigBuilder()
                        .defaultsForCommand(Command.MongoD)
                        .progressListener(new Slf4jProgressListener(LOGGER))
                        .build()))
                .build();
        // @formatter:on
        final MongodStarter starter = MongodStarter.getInstance(runtimeConfig);

        for (final IMongodConfig mongodConfig : mongodConfigList) {
            final MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
            final MongodProcess mongod = mongodExecutable.start();

            mongoProcesses.put(mongod, mongodExecutable);
        }
    }

    private void initializeReplicaSet(final List<IMongodConfig> mongodConfigList) throws UnknownHostException, InterruptedException {
        Thread.sleep(1000);
        final MongoClientOptions mo = MongoClientOptions.builder().connectTimeout(10).build();
        final ServerAddress arbitrerAddress = new ServerAddress(mongodConfigList.get(0).net().getServerAddress().getHostName(),
                mongodConfigList.get(0).net().getPort());

        try (MongoClient mongo = new MongoClient(arbitrerAddress, mo)) {
            final MongoDatabase mongoAdminDB = mongo.getDatabase("admin");

            Document cr = mongoAdminDB.runCommand(new Document("isMaster", 1));
            LOGGER.info("isMaster: {}", cr);

            // Build replica set configuration settings
            final Document rsConfiguration = buildReplicaSetConfiguration(mongodConfigList);
            LOGGER.info("replSetSettings: {}", rsConfiguration);

            // Initialize replica set
            cr = mongoAdminDB.runCommand(new Document("replSetInitiate", rsConfiguration));
            LOGGER.info("replSetInitiate: {}", cr);

            // Check replica set status before to proceed
            int maxWaitRounds = 10;
            do {
                LOGGER.info("Waiting for 1 second...");
                Thread.sleep(1000);
                cr = mongoAdminDB.runCommand(new Document("replSetGetStatus", 1));
                LOGGER.info("replSetGetStatus: {}", cr);
                maxWaitRounds--;
            } while (!isReplicaSetStarted(cr) || maxWaitRounds != 0);

            if (!isReplicaSetStarted(cr) && maxWaitRounds == 0) {
                throw new RuntimeException("Could not initialize replica set");
            }
        }
    }

    private Document buildReplicaSetConfiguration(final List<IMongodConfig> configList) throws UnknownHostException {
        final Document replicaSetSetting = new Document();
        replicaSetSetting.append("_id", "test001");

        final List<Document> members = new ArrayList<>();
        int i = 0;
        for (final IMongodConfig mongoConfig : configList) {
            members.add(new Document().append("_id", i++).append("host",
                    mongoConfig.net().getServerAddress().getHostName() + ":" + mongoConfig.net().getPort()));
        }

        replicaSetSetting.append("members", members);
        return replicaSetSetting;
    }

    private boolean isReplicaSetStarted(final Document setting) {
        if (!setting.containsKey("members")) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final List<Document> members = setting.get("members", List.class);
        for (final Document member : members) {
            LOGGER.info("replica set member {}", member);
            final int state = member.getInteger("state");
            LOGGER.info("state: {}", state);
            // 1 - PRIMARY, 2 - SECONDARY, 7 - ARBITER
            if (state != 1 && state != 2 && state != 7) {
                return false;
            }
        }
        return true;
    }

    private List<IMongodConfig> buildMongodConfiguration(final List<HostAndPort> hosts, final boolean configureReplicaSet)
            throws IOException {
        final List<IMongodConfig> configs = new ArrayList<>(hosts.size());
        for (final HostAndPort hostAndPort : hosts) {
            configs.add(buildMongodConfiguration(hostAndPort, configureReplicaSet));
        }
        return configs;
    }

    private IMongodConfig buildMongodConfiguration(final HostAndPort hostAndPort, final boolean configureReplicaSet) throws IOException {
        final InetAddress address = InetAddress.getByName(hostAndPort.getHost());

        // @formatter:off
        final MongodConfigBuilder builder = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(hostAndPort.getHost(), hostAndPort.getPort(), address instanceof Inet6Address));
        // @formatter:on

        if (configureReplicaSet) {
            builder.replication(new Storage(null, "test001", 0));
        }
        return builder.build();
    }

    private void stopMongod() throws InterruptedException {
        for (final Entry<MongodProcess, MongodExecutable> entry : mongoProcesses.entrySet()) {
            entry.getKey().stop();
            entry.getValue().stop();
        }

        for (final Entry<MongodProcess, MongodExecutable> entry : mongoProcesses.entrySet()) {
            while (entry.getKey().isProcessRunning()) {
                Thread.sleep(1000);
            }
        }
        Thread.sleep(1000);
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        if (!MongoSuite.isActive()) {
            stopMongod();
        }
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        if (!MongoSuite.isActive()) {
            startMongod(MongodConfiguration.builder().addHost("localhost", 27017).build());
        }
    }
}
