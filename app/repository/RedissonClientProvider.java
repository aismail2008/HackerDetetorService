package repository;

import com.google.inject.Provider;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.api.PlayException;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * Created by aliismail on 24/11/2017.
 */
public class RedissonClientProvider implements Provider<RedissonClient> {
    private static final String CONF_KEY = "redisson";
    private static final String CONF_KEY_ADDRESS = "address";
    private static final String CONF_KEY_PASSWORD = "password";
    private static final String CONF_KEY_PING_TIMEOUT = "pingTimeout";
    private static final String CONF_KEY_CONNECT_TIMEOUT = "connectTimeout";
    private static final String CONF_KEY_IDLE_CONNECTION_TIMEOUT = "idleConnectionTimeout";
    private static final String CONF_KEY_TIMEOUT = "timeout";
    private static final String CONF_KEY_RETRY_ATTEMPTS = "retryAttempts";
    private static final String CONF_KEY_RETRY_INTERVAL = "retryInterval";
    private static final String CONF_KEY_RECONNECTION_TIMEOUT = "reconnectionTimeout";
    private static final String CONF_KEY_FAILED_ATTEMPTS = "failedAttempts";
    private static final String CONF_KEY_CONNECTION_POOL_SIZE = "connectionPoolSize";
    private static final String CONF_KEY_CONNECTION_MINIMUM_IDLE_SIZE = "connectionMinimumIdleSize";
    private static final String CONF_KEY_SUBSCRIPTIONS_PER_CONNECTION = "subscriptionsPerConnection";
    private static final String CONF_KEY_SUBSCRIPTION_CONNETION_MINIMUM_IDLE_SIZE = "subscriptionConnectionMinimumIdleSize";
    private static final String CONF_KEY_SUBSCRIPTION_CONNETION_POOLSIZE = "subscriptionConnectionPoolSize";
    private static final String CONF_KEY_DNS_MONITORING = "dnsMonitoring";
    private static final String CONF_KEY_DNS_MONITORING_INTERVAL = "dnsMonitoringInterval";
    private static final String CONF_KEY_DATABASE = "database";
    private static final String CONF_KEY_CLIENT_NAME = "clientName";

    private static final String CONF_KEY_THREADS = "threads";
    private static final String CONF_KEY_NETTY_THREADS = "nettyThreads";
    private static final String CONF_KEY_USE_LINUX_NATIVE_EPOLL = "useLinuxNativeEpoll";

    private RedissonClient client;

    final static Logger.ALogger logger = Logger.of(RedissonClientProvider.class);

    /**
     * @param applicationLifecycle This object is an interface to the lifecycle events
     */
    @Inject
    RedissonClientProvider(Environment environment, Configuration configuration, ApplicationLifecycle applicationLifecycle) {
        try {
            Config redissonConfig = getRedissonConfig(configuration);
            client = Redisson.create(redissonConfig);
            logger.info("RedissonClient created");
            if (!environment.isTest()) {
                logger.info("Adding stopHook for RedissonClient");
                applicationLifecycle.addStopHook(() -> {
                    logger.info("Shutting down RedissonClient");
                    client.shutdown();
                    return CompletableFuture.completedFuture(null);
                });
            }
        } catch (Exception e) {
            throw new PlayException("RedissonClient not configured", e.getMessage());
        }
    }

    @Override
    public RedissonClient get() {
        return client;
    }

    /*

    All Config properties:

    					codec							Codec
						codecProvider					CodecProvider
						threads							int
						executor						ExecutorService
						nettyThreads					int
						eventLoopGroup					EventLoopGroup
						resolverProvider				ResolverProvider
						useLinuxNativeEpoll			    bool
						redissonReferenceEnabled        bool

   These are configurable via application.conf at the moment:
        threads
        nettyThreads
        useLinuxNativeEpoll

    If we want the rest configurable via application.conf, they should be added. All take the default values now.

    All SingleServerConfig properties are configurable via application.conf

    Full config below, only one is mandatory: address

    redisson {
        threads
        nettyThreads
        useLinuxNativeEpoll
        address="localhost:6379"
        password
        pingTimeout
        connectTimeout
        idleConnectionTimeout
        timeout
        retryAttempts
        retryInterval
        reconnectionTimeout
        failedAttempts
        connectionPoolSize
        connectionMinimumIdleSize
        subscriptionsPerConnection
        subscriptionConnectionMinimumIdleSize
        subscriptionConnectionPoolSize
        dnsMonitoring
        dnsMonitoringInterval
        database
        clientName
    }

     */

    private Config getRedissonConfig(Configuration conf) throws Exception {
        Configuration rc = conf.getConfig(CONF_KEY);
        if (rc == null) {
            throw new Exception("no redisson configuration found, key: " + CONF_KEY);
        }

        logger.info("configuring redisson");

        Config c = new Config();

        // threads
        Integer threads = rc.getInt(CONF_KEY_THREADS);
        if (threads != null) {
            logger.info("configuring redisson with threads {}", threads);
            c.setThreads(threads);
        }


        // nettyThreads
        Integer nettyThreads = rc.getInt(CONF_KEY_NETTY_THREADS);
        if (nettyThreads != null) {
            logger.info("configuring redisson with nettyThreads {}", nettyThreads);
            c.setNettyThreads(nettyThreads);
        }

        // useLinuxNativeEpoll
        Boolean useLinuxNativeEpoll = rc.getBoolean(CONF_KEY_USE_LINUX_NATIVE_EPOLL);
        if (useLinuxNativeEpoll != null) {
            logger.info("configuring redisson with useLinuxNativeEpoll {}", useLinuxNativeEpoll);
            c.setUseLinuxNativeEpoll(useLinuxNativeEpoll);
        }

        // address (mandatory)
        String address = rc.getString(CONF_KEY_ADDRESS);
        if (StringUtils.isEmpty(address)) {
            throw new Exception("no address found in redisson configuration, key " + CONF_KEY_ADDRESS);
        }
        logger.info("configuring redisson with address {}", address);
        c.useSingleServer().setAddress(address);

        // password
        String password = rc.getString(CONF_KEY_PASSWORD);
        if (!StringUtils.isEmpty(password)) {
            logger.info("configuring redisson with password {}", password);
            c.useSingleServer().setPassword(password);
        }

        // pingTimeout
        Integer pingTimeout = rc.getInt(CONF_KEY_PING_TIMEOUT);
        if (pingTimeout != null) {
            logger.info("configuring redisson with pingTimeout {}", pingTimeout);
            c.useSingleServer().setPingTimeout(pingTimeout);
        }

        // connectTimeout
        Integer connectTimeout = rc.getInt(CONF_KEY_CONNECT_TIMEOUT);
        if (connectTimeout != null) {
            logger.info("configuring redisson with connectTimeout {}", connectTimeout);
            c.useSingleServer().setConnectTimeout(connectTimeout);
        }

        // idleConnectionTimeout
        Integer idleConnectionTimeout = rc.getInt(CONF_KEY_IDLE_CONNECTION_TIMEOUT);
        if (idleConnectionTimeout != null) {
            logger.info("configuring redisson with idleConnectionTimeout {}", idleConnectionTimeout);
            c.useSingleServer().setIdleConnectionTimeout(idleConnectionTimeout);
        }

        // timeout
        Integer timeout = rc.getInt(CONF_KEY_TIMEOUT);
        if (timeout != null) {
            logger.info("configuring redisson with timeout {}", timeout);
            c.useSingleServer().setTimeout(timeout);
        }

        // retryAttempts
        Integer retryAttempts = rc.getInt(CONF_KEY_RETRY_ATTEMPTS);
        if (timeout != null) {
            logger.info("configuring redisson with retryAttempts {}", retryAttempts);
            c.useSingleServer().setRetryAttempts(retryAttempts);
        }

        // retryInterval
        Integer retryInterval = rc.getInt(CONF_KEY_RETRY_INTERVAL);
        if (retryInterval != null) {
            logger.info("configuring redisson with retryInterval {}", retryInterval);
            c.useSingleServer().setRetryInterval(retryInterval);
        }

        // reconnectionTimeout
        Integer reconnectionTimeout = rc.getInt(CONF_KEY_RECONNECTION_TIMEOUT);
        if (reconnectionTimeout != null) {
            logger.info("configuring redisson with reconnectionTimeout {}", reconnectionTimeout);
            c.useSingleServer().setReconnectionTimeout(reconnectionTimeout);
        }

        // failedAttempts
        Integer failedAttempts = rc.getInt(CONF_KEY_FAILED_ATTEMPTS);
        if (failedAttempts != null) {
            logger.info("configuring redisson with failedAttempts {}", failedAttempts);
            c.useSingleServer().setFailedAttempts(failedAttempts);
        }

        // connectionPoolSize
        Integer connectionPoolSize = rc.getInt(CONF_KEY_CONNECTION_POOL_SIZE);
        if (connectionPoolSize != null) {
            logger.info("configuring redisson with connectionPoolSize {}", connectionPoolSize);
            c.useSingleServer().setConnectionPoolSize(connectionPoolSize);
        }

        // connectionMinimumIdleSize
        Integer connectionMinimumIdleSize = rc.getInt(CONF_KEY_CONNECTION_MINIMUM_IDLE_SIZE);
        if (connectionMinimumIdleSize != null) {
            logger.info("configuring redisson with connectionMinimumIdleSize {}", connectionMinimumIdleSize);
            c.useSingleServer().setConnectionMinimumIdleSize(connectionMinimumIdleSize);
        }

        // subscriptionsPerConnection
        Integer subscriptionsPerConnection = rc.getInt(CONF_KEY_SUBSCRIPTIONS_PER_CONNECTION);
        if (subscriptionsPerConnection != null) {
            logger.info("configuring redisson with subscriptionsPerConnection {}", subscriptionsPerConnection);
            c.useSingleServer().setSubscriptionsPerConnection(subscriptionsPerConnection);
        }

        // subscriptionConnectionMinimumIdleSize
        Integer subscriptionConnectionMinimumIdleSize = rc.getInt(CONF_KEY_SUBSCRIPTION_CONNETION_MINIMUM_IDLE_SIZE);
        if (subscriptionConnectionMinimumIdleSize != null) {
            logger.info("configuring redisson with subscriptionConnectionMinimumIdleSize {}", subscriptionConnectionMinimumIdleSize);
            c.useSingleServer().setSubscriptionConnectionMinimumIdleSize(subscriptionConnectionMinimumIdleSize);
        }

        // subscriptionConnectionPoolSize
        Integer subscriptionConnectionPoolSize = rc.getInt(CONF_KEY_SUBSCRIPTION_CONNETION_POOLSIZE);
        if (subscriptionConnectionPoolSize != null) {
            logger.info("configuring redisson with subscriptionConnectionPoolSize {}", subscriptionConnectionPoolSize);
            c.useSingleServer().setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize);
        }

        // dnsMonitoring
        Boolean dnsMonitoring = rc.getBoolean(CONF_KEY_DNS_MONITORING);
        if (dnsMonitoring != null) {
            logger.info("configuring redisson with dnsMonitoring {}", dnsMonitoring);
            c.useSingleServer().setDnsMonitoring(dnsMonitoring);
        }

        // dnsMonitoringInterval
        Integer dnsMonitoringInterval = rc.getInt(CONF_KEY_DNS_MONITORING_INTERVAL);
        if (dnsMonitoringInterval != null) {
            logger.info("configuring redisson with dnsMonitoringInterval {}", dnsMonitoringInterval);
            c.useSingleServer().setDnsMonitoringInterval(dnsMonitoringInterval);
        }

        // database
        Integer database = rc.getInt(CONF_KEY_DATABASE);
        if (database != null) {
            logger.info("configuring redisson with database {}", database);
            c.useSingleServer().setDatabase(database);
        }

        // clientName
        String clientName = rc.getString(CONF_KEY_CLIENT_NAME);
        if (!StringUtils.isEmpty(clientName)) {
            logger.info("configuring redisson with clientName {}", clientName);
            c.useSingleServer().setClientName(clientName);
        }

        return c;
    }

}
