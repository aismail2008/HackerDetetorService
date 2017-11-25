package services.impl;

import repository.DataRepository;
import services.HackerDetectorService;
import services.LogStatusEnum;

import javax.inject.Inject;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Created by aliismail on 24/11/2017.
 */
public class HackerDetectorServiceImpl implements HackerDetectorService {
    @Inject
    private DataRepository repositoryClient;

    private final long TIME_OUT_SEC = 300;
    private final long FAILURE_COUNT = 5;

    /**
     * This function assumes all logs come in order by time and that's why i always keep last five minutes of failures
     * @param line in this format : <date in the epoch format>,<IP>,<Username>,<SUCCESS or FAILURE>
     *             example : 1507365137,187.218.83.136,John.Smith,SUCCESS
     * @return String decides if hacker is detected
     */
    @Override
    public CompletionStage<String> parseLogLine(final String line) {
        if (line.split(",")[3].equals(LogStatusEnum.SUCCESS.toString()))
            return CompletableFuture.completedFuture("");

        //Processing failure
        String ip = line.split(",")[1];
        long timestamp = Long.parseLong(line.split(",")[0]);
        SortedSet<Long> originalSet = repositoryClient.getData(ip);

        final long expire = timestamp - TIME_OUT_SEC;
        long cnt = originalSet.stream().filter(val -> val >= expire).count() + 1;

        repositoryClient.setData(ip, timestamp);
        if (cnt >= FAILURE_COUNT)
            return CompletableFuture.completedFuture("suspicious IP address : " + ip);
        else
            return CompletableFuture.completedFuture("");
    }
}
