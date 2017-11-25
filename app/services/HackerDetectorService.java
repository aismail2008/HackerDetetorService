package services;

import play.mvc.Http;

import java.util.concurrent.CompletionStage;

/**
 * Created by aliismail on 24/11/2017.
 */
public interface HackerDetectorService {
    CompletionStage<String> parseLogLine(String line);
}