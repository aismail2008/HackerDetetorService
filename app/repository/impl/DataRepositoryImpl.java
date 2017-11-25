package repository.impl;

import org.redisson.api.RSortedSet;
import org.redisson.api.RedissonClient;
import play.Logger;
import repository.DataRepository;

import javax.inject.Inject;
import java.util.SortedSet;

public class DataRepositoryImpl implements DataRepository {
    private static final Logger.ALogger logger = Logger.of(DataRepositoryImpl.class);

    // 2017-7-1 00:00:00.00 GMT
    public static final Long TS_OFFSET_SINCE = 1498867200L;
    private static final String IP_REDIS_KEY = "IP-%s";

    @Inject
    private RedissonClient redissonClient;

    public static String getIPKey(final String ip) {
        return String.format(IP_REDIS_KEY, ip);
    }


    @Override
    public void setData(final String key, final Long data) {
        final RSortedSet<Long> keySet = redissonClient.getSortedSet(getIPKey(key));
//        keySet.clear();
//        if (keySet != null && data != null)
            keySet.add(data);
    }

    @Override
    public SortedSet<Long> getData(final String key) {
        return redissonClient.getSortedSet(getIPKey(key));
    }
}
