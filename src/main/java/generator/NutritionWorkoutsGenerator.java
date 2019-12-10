package generator;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import requests.Response;

import java.io.IOException;
import java.util.List;
public class NutritionWorkoutsGenerator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws IOException {
        Response res = Helper.sendGetRequest(Constants.QUERY_PARAMS,"v4","programs");
        List<String> slugs = Helper.getSlugs(res);
        StringBuffer buffer = new StringBuffer();
        for (String slug : slugs) {

            Response r = Helper.sendGetRequest("v4", "programs", slug);
            String ws = r.get("$.items[*].workoutGroups[*].groupName")
                    .replaceAll("\\s{2}", " ")
                    .replaceAll("\\h", " ");

            buffer.append(String.format("%s.workoutGroups=%s", slug , ws));
            buffer.append("\n");
            JSONArray workoutGroups = JsonPath.read(r.body(), "$.items[*].workoutGroups[*]");

            for (int i = 0; i < workoutGroups.size(); i++) {
                String groupName = r.get(String.format("$.items[*].workoutGroups[%s].groupName", i)).replaceAll(":", "").replaceAll(" ", "").toLowerCase();
                String w = "";
                JSONArray workouts = JsonPath.read(r.body(), String.format("$.items[*].workoutGroups[%s].workouts[*]",i));
                for (int j = 0; j < workouts.size(); j++) {
                    String currentWorkoutPath = String.format("$.items[*].workoutGroups[%s].workouts[%s]", i, j);
                    w += r.get(currentWorkoutPath + ".title") + (j == workouts.size() - 1 ? "" : "~");
                    String workoutName = r.get(currentWorkoutPath + ".title").replaceAll(" ","").toLowerCase().replaceAll(":","");
                    String workoutDurtion = r.get(currentWorkoutPath + ".marketingTime") + " min.";
                    buffer.append(String.format("%s.%s.%s.workoutDuration=%s", slug, groupName, workoutName, workoutDurtion));
                    buffer.append("\n");

                    String desc = r.get(currentWorkoutPath + ".description.raw")
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
                    buffer.append(String.format("%s.%s.%s.description=%s",slug,groupName,workoutName, desc));
                    buffer.append("\n");
                    boolean haveResources = false;
                    if(!r.get(currentWorkoutPath + ".resources").equals("[]")) {
                        haveResources = true;
                        JSONArray resources = JsonPath.read(r.body(), currentWorkoutPath + ".resources[*]");
                        int count = resources.size();
                        buffer.append(String.format("%s.%s.%s.resourcesCount=%s", slug, groupName, workoutName, count));
                        buffer.append("\n");
                        for(int c = 0; c < resources.size(); c++) {
                            String title = r.get(String.format("%s.resources[%s].title", currentWorkoutPath, c));
                            buffer.append(String.format("%s.%s.%s.resources.%s.title=%s", slug, groupName, workoutName, c, title));
                            buffer.append("\n");
                            String link = r.get(String.format("%s.resources[%s].link", currentWorkoutPath, c))
                                    .replaceAll("https://d2rxohj08n82d5.cloudfront.net", "")
                                    .replaceAll("http://d2rxohj08n82d5.cloudfront.net", "");
                            buffer.append(String.format("%s.%s.%s.resources.%s.link=%s", slug, groupName, workoutName, c, link));
                            buffer.append("\n");
                        }
                    }
                    buffer.append(String.format("%s.%s.%s.haveResource=%s", slug, groupName, workoutName, haveResources));
                    buffer.append("\n");
                }
                buffer.append(String.format("%s.%s.workouts=%s", slug , groupName , w.replaceAll("\\s{2}", " ")
                        .replaceAll("\\h", " ")));
                buffer.append("\n");
            }

        }
        buffer.append("fixate-cooking-show.video.groupName=Breakfast Recipes\n" +
                "fixate-cooking-show.video.workoutName=Ricotta, Apple, and Honey Toast\n" +
                "fixate-cooking-show.video.duration=150\n" +
                "2b-mindset.video.groupName=Ask Ilana\n" +
                "2b-mindset.video.workoutName=Help! I Hit a Plateau\n" +
                "2b-mindset.video.duration=95\n" +
                "ultimate-reset.video.groupName=Phase 3: Restore\n" +
                "ultimate-reset.video.workoutName=Day 16: Diary\n" +
                "ultimate-reset.video.duration=78\n" +
                "shakeology.video.groupName=Formulators' Corner\n" +
                "shakeology.video.workoutName=How Shakeology Helps You Lose Weight\n" +
                "shakeology.video.duration=73\n" +
                "ultimate-portion-fix.video.groupName=Module 6: Easy Ways For Special Days\n" +
                "ultimate-portion-fix.video.workoutName=Video 31: Ready, Set, Fix…\n" +
                "ultimate-portion-fix.video.duration=112\n" +
                "ultimate-portion-fix-certification.video.groupName=Challenge Group Guide\n" +
                "ultimate-portion-fix-certification.video.workoutName=Introduction to Challenge Group Guide\n" +
                "ultimate-portion-fix-certification.video.duration=104\n" +
                "ultimate-portion-fix-master-series.video.groupName=April Ultimate Portion Fix Master Series\n" +
                "ultimate-portion-fix-master-series.video.workoutName=Get Started\n" +
                "ultimate-portion-fix-master-series.video.duration=206");
        Helper.writeInFile(Constants.NUTRITION_WORKOUT_LIST_PROPERTY_FILE_NAME,buffer.toString());
    }



}