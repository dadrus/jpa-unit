package eu.drus.jpa.unit.test.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.net.HostAndPort;

public class MongodConfiguration {

    private List<HostAndPort> hosts = new ArrayList<>();

    private MongodConfiguration() {}

    public static Builder builder() {
        return new Builder(new MongodConfiguration());
    }

    public List<HostAndPort> getHosts() {
        return hosts;
    }

    public static class Builder {

        private MongodConfiguration config;

        private Builder(final MongodConfiguration config) {
            this.config = config;
        }

        public Builder addHost(final String host, final int port) {
            config.hosts.add(HostAndPort.fromParts(host, port));
            return this;
        }

        public Builder addHost(final String host) {
            config.hosts.add(HostAndPort.fromString(host));
            return this;
        }

        public Builder addHost(final HostAndPort hostAndPort) {
            config.hosts.add(hostAndPort);
            return this;
        }

        public MongodConfiguration build() {
            return config;
        }
    }
}
