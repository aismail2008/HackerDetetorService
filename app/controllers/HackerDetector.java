package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.HackerDetectorService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by aliismail on 24/11/2017.
 */
public class HackerDetector extends Controller {
    private static final Logger.ALogger logger = Logger.of(HackerDetector.class);
    private HackerDetectorService hackerDetectorService;
    private HttpExecutionContext ec;

    @Inject
    public HackerDetector(final HttpExecutionContext ec, final HackerDetectorService hackerDetectorService){
        this.hackerDetectorService = hackerDetectorService;
        this.ec = ec;
    }

    public CompletionStage<Result> healthCheck() {
        logger.info("healthcheck requested");
        String data = String.format("Success");
        return CompletableFuture.completedFuture(ok(data));
    }

    public CompletionStage<Result> parseline() {
        logger.info("Request Receive");
        final JsonNode json = request().body().asJson();
        if (json == null) {
            logger.error("No JSON body present");
            return CompletableFuture.completedFuture(badRequest());
        }
        logger.debug("update() JSON : {}", json);
        return hackerDetectorService.parseLogLine(json.asText()).thenApplyAsync(result -> ok(result), ec.current());
    }
}
