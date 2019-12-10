package generator;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import requests.Response;

import java.io.IOException;
import java.util.*;
public class WorkoutTabGenerator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws IOException {
        Response response = Helper.sendGetRequest("v4","programs");
        List<String> slugs = Helper.getSlugs(response);

        StringBuffer buffer = new StringBuffer();
        for (String slug : slugs) {

            Response programResponse = Helper.sendGetRequest("v4", "programs", slug);
            buffer.append(String.format("%s.workoutGroups=%s", slug , programResponse.get("$.items[*].workoutGroups[*].groupName")).replaceAll("\\s+", " "));
            buffer.append("\n");
            JSONArray workoutGroups = JsonPath.read(programResponse.body(), "$.items[*].workoutGroups[*]");

            for (int i = 0; i < workoutGroups.size(); i++) {
                String groupName = programResponse.get(String.format("$.items[*].workoutGroups[%s].groupName", i)).replaceAll(":", "").replaceAll(" ", "").toLowerCase();
                String ws = programResponse.get((String.format("$.items[*].workoutGroups[%s].workouts[*].title", i)))
                        .replaceAll("\\h", " ")
                        .replaceAll("\\s+- ", " - ")
                        .replaceAll("\\s+", " ")
                        .replaceAll(" ,", ",");
                ws = ws.replaceAll(" -\\s+", " - ");
                buffer.append(String.format("%s.%s.workouts=%s", slug , groupName , ws));

                buffer.append("\n");
                JSONArray workouts = JsonPath.read(programResponse.body(), String.format("$.items[*].workoutGroups[%s].workouts[*]",i));
                for (int j = 0; j < workouts.size(); j++) {
                    String currentWorkoutPath = String.format("$.items[*].workoutGroups[%s].workouts[%s]", i, j);
                    String workoutName = programResponse.get(currentWorkoutPath + ".title").replaceAll(" ","").toLowerCase().replaceAll(":","");
                    String requiredEquips = programResponse.get(currentWorkoutPath + ".requiredEquips");
                    String recommendEquips = programResponse.get(currentWorkoutPath + ".recommendEquips");
                    String workoutDurtion = programResponse.get(currentWorkoutPath + ".marketingTime") + " min.";
                    if (recommendEquips.equals("[]") && requiredEquips.equals("[]")) {
                        buffer.append(String.format("%s.%s.%s.isWorkoutEquipmentsExist=false",slug,groupName,workoutName));
                        buffer.append("\n");
                    } else {
                        buffer.append(String.format("%s.%s.%s.isWorkoutEquipmentsExist=true",slug,groupName,workoutName));
                        buffer.append("\n");
                    }

                    if(recommendEquips.equals("[]") || recommendEquips.equals("-")) {
                        recommendEquips = "None";
                    } else {
                        recommendEquips = recommendEquips
                                .replaceAll("\\[","")
                                .replaceAll("\",\"", ",")
                                .replaceAll("\",\" ", ",")
                                .replaceAll("\"","")
                                .replaceAll("]","")
                                .replaceAll(",",",")
                                .replaceAll("\\s+", " ");
                        recommendEquips+=".";
                        recommendEquips = recommendEquips.replaceAll("\\.\\.", ".");
                    }

                    if(requiredEquips.equals("[]")) {
                        requiredEquips = "None";
                    } else {
                        requiredEquips = requiredEquips
                                .replaceAll("\\[","")
                                .replaceAll("\",\"", "~~~")
                                .replaceAll("\",\" ", "~~~")
                                .replaceAll("\"","")
                                .replaceAll("]","")
                                .replaceAll(",",", ")
                                .replaceAll("~~~", ",")
                                .replaceAll("\\s+", " ");
                                requiredEquips+=".";
                                requiredEquips = requiredEquips.replaceAll("\\.\\.", ".");

                    }
                    String desc = programResponse.get(currentWorkoutPath + ".description.raw")
                            .replaceAll("\\h", " ")
                            .replaceAll("--", "–")
                            .replaceAll("'", "’");
                    StringBuilder description = new StringBuilder(desc);
                    for(int z = 0; z < desc.length(); z++) {
                        if((int)description.charAt(z) == 8203 ){
                            description.deleteCharAt(z++);
                        }
                    }

                    desc = description.toString().trim();
                    desc = desc
                            .replaceAll("\u200B", "")
                            .replaceAll(" - ", " – ")
                            .replaceAll("\\s+", " ")
                            .replaceAll("\\.\\.\\.", "…");

                    while (desc.contains("\"")) {
                        desc = desc.replaceFirst("\"", "“");
                        desc = desc.replaceFirst("\"", "”");
                    }

                    buffer.append(String.format("%s.%s.%s.workoutDuration=%s", slug, groupName, workoutName, workoutDurtion));
                    buffer.append("\n");
                    buffer.append(String.format("%s.%s.%s.requiredEquips=%s",slug,groupName,workoutName, requiredEquips));
                    buffer.append("\n");
                    buffer.append(String.format("%s.%s.%s.recommendEquips=%s",slug,groupName,workoutName, recommendEquips));
                    buffer.append("\n");
                    buffer.append(String.format("%s.%s.%s.description=%s",slug,groupName,workoutName,desc));
                    buffer.append("\n");
                }
            }

        }
        Helper.writeInFile(Constants.WORKOUTS_PROPERTY_FILE_NAME, buffer.toString());
    }

}
