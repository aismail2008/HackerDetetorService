package services.impl;

import repository.DataRepository;
import services.HackerDetectorService;
import services.LogStatusEnum;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by aliismail on 24/11/2017.
 */
public class HackerDetectorServiceImpl implements HackerDetectorService {
    @Inject
    private DataRepository repositoryClient;

    private final long TIME_OUT_SEC = 300;
    private final long FAILURE_COUNT = 5;

    /**
     * This function assumes all logs come in order by time and that's why only search for past 5 minuts not including future 5 minutes as well
     * But if need we can call Db by adding new filter and consider timestamp of event + 300 sec
     *
     * @param line in this format : <date in the epoch format>,<IP>,<Username>,<SUCCESS or FAILURE>
     *             example : 1507365137,187.218.83.136,John.Smith,SUCCESS
     * @return String decides if hacker is detected
     */
    @Override
    public CompletionStage<String> parseLogLine(final String line) {
        if (line.split(",")[3].equals(LogStatusEnum.SUCCESS.toString()))
            return CompletableFuture.completedFuture("");

        final String ip = line.split(",")[1];
        final Long timestamp = Long.parseLong(line.split(",")[0]);
        final String username = line.split(",")[2];
        final long expire = timestamp - TIME_OUT_SEC;
        CompletionStage<String> res = repositoryClient.getLoginEvents(ip, expire).thenApplyAsync(total -> {
            if (total >= FAILURE_COUNT - 1) {
                return "suspicious IP address : " + ip;
            } else{
                return "";
            }
        });
        repositoryClient.setData(ip, timestamp, username);
        return res;
    }
}
