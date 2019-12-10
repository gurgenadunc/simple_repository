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
public class AllWorkoutsGenerator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private static final String BASE_URL = "https://content-prod.api.beachbodyondemand.com";
    public static void main(String[] args) throws IOException {
        List<String> slugs = Arrays.asList(sendGetRequest("v4","programs").get("$.items[*].slug").split("~")).stream().map(String::trim).collect(Collectors.toList());
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (String slug : slugs) {

            Response r = sendGetRequest("v4", "programs");
            String title = r.get(String.format("$.items[%s].title", i)).toLowerCase().replaceAll(" ","")
                    .replaceAll(":","");;
            buffer.append(String.format("%s.workoutDescription=%s", slug ,r.get(String.format("$.items[%s].shortDescription.raw", i))));
            buffer.append("\n");
            String duration = r.get(String.format("$.items[%s].programDuration.id", i));
            String durationType = r.get(String.format("$.items[%s].programDurationType", i));
            System.out.println(duration);
            System.out.println(durationType);
            buffer.append(String.format("%s.workoutDuration=%s", slug ,duration + " " + durationType + "s"));
            buffer.append("\n");
            buffer.append(String.format("%s.fitnesLevel=%s", title ,r.get(String.format("$.items[%s].programIntensity.title", i))));
            buffer.append("\n");
            buffer.append(String.format("%s.workoutDurationMaximum=%s", title ,r.get(String.format("$.items[%s].workoutDurationMaximum.id", i))));
            buffer.append("\n");
            JSONArray workoutTypes = JsonPath.read(r.body(), String.format("$.items[%s].workoutType[*].title", i));

            String s = "";
            for(int j = 0; j < workoutTypes.size(); j++) {
                String name = r.get(String.format("$.items[%s].workoutType[%s].title", i, j));
                s+= j == workoutTypes.size() - 1 ? name: name + ", ";
            }
            buffer.append(String.format("%s.workoutType=%s", title ,s));
            buffer.append("\n");
            JSONArray trainers = JsonPath.read(r.body(), String.format("$.items[%s].trainers[*].title", i));
//
//            String s = "Trainer: ";
            for(int j = 0; j < trainers.size(); j++) {
                String name = r.get(String.format("$.items[%s].trainers[%s].title", i, j));
                s+= j == trainers.size() - 1 ? name: name + ", ";
            }
            buffer.append(String.format("%s.workoutTrainers=%s", slug ,s));
            buffer.append("\n");

            System.out.println(buffer);
        i++;
        }
        writeInFile("all_workouts",buffer.toString());
    }

    public static Response sendGetRequest(String ...path) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(BASE_URL);
        requestBuilder.addHeader("Accept","application/json");
        requestBuilder.addHeader("x-api-key", "2yPXMA9Tsd529LCH6WhQA13F5iO40mRW6qLTwgnh");
        requestBuilder.addPathParameters(path);
        Response response = requestBuilder.get();
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