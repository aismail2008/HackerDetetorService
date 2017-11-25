package repository.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import play.Logger;

import java.text.SimpleDateFormat;
import java.util.concurrent.CompletionStage;

public abstract class BaseRepository {
    private static final Logger.ALogger logger = Logger.of(BaseRepository.class);

    protected Session session;

    protected Boolean enableTracing;

    protected CompletionStage<ResultSet> executeAsync(final Statement statement) {
        final ResultSetFuture resultSetFuture = session.executeAsync(statement);
        final scala.concurrent.Promise<ResultSet> promise = akka.dispatch.Futures.promise();

        Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                promise.success(result);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("Error executing statement '{}' error '{}'", statement.toString(), t.getMessage());
                promise.failure(t);
            }

        }, MoreExecutors.directExecutor());

        return scala.compat.java8.FutureConverters.toJava(promise.future());
    }
}
