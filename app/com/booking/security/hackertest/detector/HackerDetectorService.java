package com.booking.security.hackertest.detector;

import java.util.concurrent.CompletionStage;

/**
 * Created by aliismail on 24/11/2017.
 */
public interface HackerDetectorService {
    CompletionStage<String> parseLogLine(String line);
}