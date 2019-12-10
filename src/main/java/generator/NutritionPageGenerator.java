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
public class NutritionPageGenerator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private static final String BASE_URL = "https://content-prod.api.beachbodyondemand.com";
    public static void main(String[] args) throws IOException {
        List<String> slugs = Arrays.asList(sendGetRequest(true,"v4","programs").get("$.items[*].slug").split("~")).stream().map(String::trim).collect(Collectors.toList());
        StringBuffer buffer = new StringBuffer();
        int channels = 0;
        int programs = 0;
        int certifications = 0;

        for (String slug : slugs) {

            Response response = sendGetRequest(false,"v4","programs", slug);
            System.out.println(response.getCurl());
            String slug1 = response.get("$.items[0].title").toLowerCase().replaceAll(" ","").replaceAll(":","");
            String nutritionRail = response.get("$.items[0].nutritionRail");
            switch (nutritionRail) {
                case "channels":
                    channels++;
                    break;
                case "programs":
                    programs++;
                    break;
                default:
                    certifications++;
            }
            String releaseStatus = response.get("$.items[0].releaseStatus").equals("New") ? "true" : "false";
            buffer.append(String.format("%s.isNew=%s", slug1, releaseStatus));
            buffer.append("\n");
            String videoCount = response.get("$.items[0].videoCount");
            buffer.append(String.format("%s.videosCount=%s", slug1, videoCount + " videos"));
            buffer.append("\n");
            String image = response.get("$.items[0].images.main.web.desktop.file");
            buffer.append(String.format("%s.imageUrl=%s", slug1, image));
            buffer.append("\n");
            String description = response.get("$.items[0].shortDescription.raw");
            buffer.append(String.format("%s.shortDescription=%s", slug1, description));
            buffer.append("\n");


            String s1 = "";
            JSONArray trainers = JsonPath.read(response.body(), String.format("$.items[0].trainers[*].title"));

            for(int j = 0; j < trainers.size(); j++) {
                String name = response.get(String.format("$.items[0].trainers[%s].title", j));
                s1+= j == trainers.size() - 1 ? name: name + " and ";
            }
            buffer.append(String.format("%s.trainer=%s", slug1, s1));
            buffer.append("\n");
        }

        buffer.append(String.format("channelsCount=%s", channels));
        buffer.append("\n");
        buffer.append(String.format("programsCount=%s", programs));
        buffer.append("\n");
        buffer.append(String.format("certificationsCount=%s", certifications));
        buffer.append("\n");

        writeInFile("nutrition_page",buffer.toString());

    }

    public static Response sendGetRequest(boolean falg, String ...path) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(BASE_URL);
        requestBuilder.addHeader("Accept","application/json");
        requestBuilder.addHeader("x-api-key", "2yPXMA9Tsd529LCH6WhQA13F5iO40mRW6qLTwgnh");
        if(falg)requestBuilder.addQueryParameter("category", "nutrition");
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
