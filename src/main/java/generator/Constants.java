package generator;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String CONTENT_ENDPOINT = "https://content-prod.api.beachbodyondemand.com";
    public static final String WORKOUTS_PROPERTY_FILE_NAME = "workout_list";
    public static final String SEPARATOR = "~";
    public static final Map<String, String> CONTENT_HEADERS;

    static {
        CONTENT_HEADERS = new HashMap<>();
        CONTENT_HEADERS.put("Accept","application/json");
        CONTENT_HEADERS.put("x-api-key", "2yPXMA9Tsd529LCH6WhQA13F5iO40mRW6qLTwgnh");
    }
}
