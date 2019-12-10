package generator;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import requests.RequestBuilder;
import requests.Response;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
public class Generator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private static final String BASE_URL = "https://d1m0rv1xg9oqxv.cloudfront.net";
    public static void main(String[] args) throws IOException {
        List<String> slugs = Arrays.asList(sendGetRequest(true,"v4","programs").get("$.items[*].slug").split("~")).stream().map(String::trim).collect(Collectors.toList());
        System.out.println(slugs);
        StringBuffer buffer = new StringBuffer();
        for (String slug : slugs) {

            Response r = sendGetRequest(true,"v4", "programs", slug);
            buffer.append(String.format("%s.workoutGroups=%s", slug ,r.get("$.items[*].workoutGroups[*].groupName")));
            buffer.append("\n");
            System.out.println(r.get("$.items[*].workoutGroups[*].groupName"));
            JSONArray workoutGroups = JsonPath.read(r.body(), "$.items[*].workoutGroups[*]");

            for (int i = 0; i < workoutGroups.size(); i++) {
                String groupName = r.get(String.format("$.items[*].workoutGroups[%s].groupName", i)).replaceAll(":", "").replaceAll(" ", "").toLowerCase();
                String w = "";
                JSONArray workouts = JsonPath.read(r.body(), String.format("$.items[*].workoutGroups[%s].workouts[*]",i));
                for (int j = 0; j < workouts.size(); j++) {
                    String currentWorkoutPath = String.format("$.items[*].workoutGroups[%s].workouts[%s]", i, j);
                    w += r.get(currentWorkoutPath + ".title") + (i == workouts.size() - 1 ? "" : "~");
                    String workoutName = r.get(currentWorkoutPath + ".title").replaceAll(" ","").toLowerCase().replaceAll(":","");
                    String workoutDurtion = r.get(currentWorkoutPath + ".marketingTime") + " min.";
                    buffer.append(String.format("%s.%s.%s.workoutDuration=%s", slug, groupName, workoutName, workoutDurtion));
                    buffer.append("\n");
                    buffer.append(String.format("%s.%s.%s.description=%s",slug,groupName,workoutName,r.get(currentWorkoutPath + ".description.raw")));
                    buffer.append("\n");
                }
                buffer.append(String.format("%s.%s.workouts=%s", slug , groupName , w.replaceAll("\\h", " ")));
                buffer.append("\n");
            }

        }
        writeInFile("nutrition_workout_list",buffer.toString());
    }

    public static Response sendGetRequest(boolean s, String ...path) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(BASE_URL);
        requestBuilder.addHeader("Accept","application/json");
        requestBuilder.addPathParameters(path);
        if(s) requestBuilder.addQueryParameter("category", "nutrition");
        Response response = requestBuilder.get();
        System.out.println(response.getCurl());
        return response;
    }

    public static void writeInFile(String fileName, String text) throws IOException {
        File file = new File("src/main/resources/" + (fileName.endsWith(".properties") ? fileName : (fileName + ".properties")));
        if (!file.exists()) file.createNewFile();
        BufferedWriter bf = new BufferedWriter(new FileWriter(file));
        bf.write(text);
        bf.flush();
    }



}