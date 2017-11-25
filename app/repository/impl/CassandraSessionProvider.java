package repository.impl;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class CassandraSessionProvider implements Provider<Session> {
    private final static Logger.ALogger logger = Logger.of(CassandraSessionProvider.class);

    public static final String CASSANDRA_HOSTS = "cassandra.hosts";
    public static final String CASSANDRA_KEYSPACE = "cassandra.keyspace";
    public static final String CASSANDRA_MULTIDC = "cassandra.dc.multi";
    public static final String CASSANDRA_DC_NAME = "cassandra.dc.name";
    public static final String CASSANDRA_CORE_LOCAL_DISTANCE = "cassandra.maxconnextions.core.localdistance";
    public static final String CASSANDRA_MAX_LOCAL_DISTANCE = "cassandra.maxconnextions.max.localdistance";
    public static final String CASSANDRA_CORE_REMOTE_DISTANCE = "cassandra.maxconnextions.core.remotedistance";
    public static final String CASSANDRA_MAX_REMOTE_DISTANCE = "cassandra.maxconnextions.max.remotedistance";


    private final Cluster cluster;

    private final Configuration configuration;
    private final String keySpace;

    @Inject
    CassandraSessionProvider(final Configuration configuration, final ApplicationLifecycle lifecycle){
        this.configuration = configuration;
        this.keySpace = configuration.getString(CASSANDRA_KEYSPACE, "");
        final Boolean multiDC = configuration.getBoolean(CASSANDRA_MULTIDC, false);
        final Integer coreLocalConnections = configuration.getInt(CASSANDRA_CORE_LOCAL_DISTANCE);
        final Integer maxLocalConnections = configuration.getInt(CASSANDRA_MAX_LOCAL_DISTANCE);
        final Integer coreRemoteConnections = configuration.getInt(CASSANDRA_CORE_REMOTE_DISTANCE);
        final Integer maxRemoteConnections = configuration.getInt(CASSANDRA_MAX_REMOTE_DISTANCE);
        
        final String DCName = configuration.getString(CASSANDRA_DC_NAME, "");

        /* Resolve the NS record, and add to the list of contact points */
        final List<String> hosts = resolveEndpoints();
        logger.info("Cassandra client configuration -> keyspace : {}, multi DC : {}, DC name : {}, contactpoints : {}", this.keySpace, multiDC, DCName, StringUtils.join(hosts, ","));
        if (StringUtils.isEmpty(this.keySpace)) {
            throw new RuntimeException(String.format("%s is not specified", CASSANDRA_KEYSPACE));
        }
        final String[] contactPoints = hosts.toArray(new String[hosts.size()]);

        /*
         * Handle pooling options
        */
        final PoolingOptions poolingOptions = new PoolingOptions();
        if (coreLocalConnections != null) {
            poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, coreLocalConnections);
        }
        if (maxLocalConnections != null) {
            poolingOptions.setMaxConnectionsPerHost(HostDistance.LOCAL, maxLocalConnections);
        }
        if (coreRemoteConnections != null) {
            poolingOptions.setCoreConnectionsPerHost(HostDistance.REMOTE, coreRemoteConnections);
        }
        if (maxRemoteConnections != null) {
            poolingOptions.setMaxConnectionsPerHost(HostDistance.REMOTE, maxRemoteConnections);
        }

        logger.info("Cassandra pool options : CL {}, ML {}, CR {}, MR {}", coreLocalConnections, maxLocalConnections, coreRemoteConnections,maxRemoteConnections);

        final LoadBalancingPolicy lbp;
        if (multiDC) {
            final DCAwareRoundRobinPolicy.Builder builder = DCAwareRoundRobinPolicy.builder();
            if (!StringUtils.isEmpty(DCName)) {
                builder.withLocalDc(DCName);
            }
            lbp = builder.build();
        } else {
            lbp = new RoundRobinPolicy();
        }
        final Cluster.Builder cb = Cluster.builder().
                addContactPoints(contactPoints).
                withLoadBalancingPolicy(lbp).
                withPoolingOptions(poolingOptions);

        cluster = cb.build();
        lifecycle.addStopHook(()-> {
            cluster.close();
            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public Session get() {
        return cluster.connect(this.keySpace);
    }

    private List<String> resolveEndpoints() {
        String NSRecord = configuration.getString(CASSANDRA_HOSTS, "");

        if (StringUtils.isEmpty(NSRecord)) {
            throw new RuntimeException(String.format("%s needs to point to a DNS record with a list of A records", CASSANDRA_HOSTS));
        }
        final List<String> hosts = new ArrayList<>();
        try {
            final InetAddress[] addresses = InetAddress.getAllByName(NSRecord);
            for (InetAddress address : addresses) {
                hosts.add(address.getHostAddress());
            }
        } catch (UnknownHostException e) {
            // Ignore
        }
        if (hosts.isEmpty()) {
            throw new RuntimeException(String.format("%s doesn't resolve to A records", NSRecord));
        }

        return hosts;
    }
}
