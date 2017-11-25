package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Created by aliismail on 25/11/2017.
 */
public class TestController extends Controller {
    private static final Logger.ALogger logger = Logger.of(TestController.class);
    final String ENDPOINT_URL = "http://localhost:9000/parselogline";

    @Inject
    private WSClient ws;

    public Result hammerTest() {
        int[] s = new int[]{0};
        for (int i = 0; i < 100; i++) {
            Runnable task = () -> {
                final WSRequest request = ws.url(ENDPOINT_URL);
                int val = s[0]++ + 1507395745;
                request.post(play.libs.Json.toJson(val + ",187.218.83.190,John.Smith,FAILURE")).thenApplyAsync(res -> {
                    if (res.getStatus() == Http.Status.OK) {
                        System.out.println(res.getBody().toString().isEmpty()? "Success": res.getBody().toString());
                        logger.info(res.getBody().toString().isEmpty()? "Success": res.getBody().toString());
                    } else {
                        System.out.println(res.getStatus());
                        logger.error(String.valueOf(res.getStatus()));
                    }
                    return true;
                });
            };
            task.run();
        }
        return ok("");
    }
}
