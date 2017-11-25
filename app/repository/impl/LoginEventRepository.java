package repository.impl;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import play.Configuration;
import play.Logger;
import repository.DataRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;


public class LoginEventRepository extends BaseRepository implements DataRepository{
    private static final Logger.ALogger logger = Logger.of(LoginEventRepository.class);

    public static final String TABLE_LOGON_EVENTS = "loginevents";
    public static final String IP = "ip";
    public static final String TIMESTAMP = "timestamp";
    public static final String USERNAME = "username";

    @Inject
    public LoginEventRepository(Session session, Configuration configuration) {
        this.session = session;
        enableTracing = configuration.getBoolean("query.trace.enabled", false);
    }

    @Override
    public CompletionStage<Boolean> setData(final String ip, final Long timestamp, final String username){
        final Insert insert = QueryBuilder
                .insertInto(TABLE_LOGON_EVENTS)
                .value(IP, ip)
                .value(TIMESTAMP, timestamp)
                .value(USERNAME, username);

        return executeAsync(insert).thenApplyAsync(resultSet -> {
            return true;
        }).exceptionally(throwable ->  {
            logger.error("Inserting login event to Cassandra {} failed due to {}", ip, throwable);
            return false;
        });
    }

    /**
     * We want to handle window of 5 minutes as in past
     * @param ip
     * @param timestamp
     * @return count number of events
     */
    @Override
    public CompletionStage<Long> getLoginEvents(final String ip, final Long timestamp) {
        final Select.Where selectOnly = QueryBuilder.
                select(TIMESTAMP).
                from(TABLE_LOGON_EVENTS).
                where(QueryBuilder.eq(IP, ip)).
                and(QueryBuilder.gte(TIMESTAMP, timestamp));

        return executeAsync(selectOnly).thenApplyAsync(result -> {
            if (result.isExhausted())
                return new Long(0);

            return new Long(result.all().size());
        }).exceptionally(throwable -> {
            logger.error("Error {}", throwable);
            return new Long(0);
        });
    }

}
