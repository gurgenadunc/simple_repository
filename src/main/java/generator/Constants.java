package generator;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String CONTENT_ENDPOINT = "https://content-prod.api.beachbodyondemand.com";
    public static final String WORKOUTS_PROPERTY_FILE_NAME = "workout_list";
    public static final String ALL_WORKOUTS_PROPERTY_FILE_NAME = "all_workouts";
    public static final String PROGRAM_MATERIALS_PROPERTY_FILE_NAME = "program_materials";
    public static final String DETAILS_PROPERTY_FILE_NAME = "details";
    public static final String NUTRITION_PROPERTY_FILE_NAME = "nutrition";
    public static final String NUTRITION_WORKOUT_LIST_PROPERTY_FILE_NAME = "nutrition_workout_list";
    public static final String NUTRITION_PAGE_PROPERTY_FILE_NAME = "nutrition_page";
    public static final String SEPARATOR = "~";
    public static final Map<String, String> CONTENT_HEADERS;
    public static final Map<String, String> QUERY_PARAMS;

    static {
        CONTENT_HEADERS = new HashMap<>();
        CONTENT_HEADERS.put("Accept","application/json");
        CONTENT_HEADERS.put("x-api-key", "2yPXMA9Tsd529LCH6WhQA13F5iO40mRW6qLTwgnh");
    }

    static {
        QUERY_PARAMS = new HashMap<>();
        QUERY_PARAMS.put("category","nutrition");
    }
}