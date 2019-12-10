package generator;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import requests.Response;

import java.io.IOException;
import java.util.List;
public class AllWorkoutsGenerator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws IOException {
        Response res = Helper.sendGetRequest("v4","programs");
        List<String> slugs = Helper.getSlugs(res);
        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (String slug : slugs) {

            Response r = Helper.sendGetRequest("v4", "programs");
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
            for(int j = 0; j < trainers.size(); j++) {
                String name = r.get(String.format("$.items[%s].trainers[%s].title", i, j));
                s+= j == trainers.size() - 1 ? name: name + ", ";
            }
            buffer.append(String.format("%s.workoutTrainers=%s", slug ,s));
            buffer.append("\n");

            System.out.println(buffer);
        i++;
        }
        Helper.writeInFile(Constants.ALL_WORKOUTS_PROPERTY_FILE_NAME,buffer.toString());
    }



}