package generator;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import requests.Response;

import java.io.IOException;
import java.util.List;
public class NutritionPageGenerator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws IOException {
        Response res = Helper.sendGetRequest(Constants.QUERY_PARAMS,"v4","programs");
        List<String> slugs = Helper.getSlugs(res);
        StringBuffer buffer = new StringBuffer();
        int channels = 0;
        int programs = 0;
        int certifications = 0;

        for (String slug : slugs) {

            Response response = Helper.sendGetRequest("v4","programs", slug);
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

        Helper.writeInFile(Constants.NUTRITION_PAGE_PROPERTY_FILE_NAME,buffer.toString());

    }

}
