package repository;

import java.util.SortedSet;

public interface DataRepository {
    void setData(String key, Long data);
    SortedSet<Long> getData(String key);
}
