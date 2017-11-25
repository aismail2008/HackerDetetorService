package repository;

import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

public interface DataRepository {
    CompletionStage<Boolean> setData(String ip, Long timestamp, String username);
    CompletionStage<Long> getLoginEvents(String ip, Long timestamp);
}
