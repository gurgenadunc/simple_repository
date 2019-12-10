package generator;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import requests.Response;

import java.io.IOException;
import java.util.List;
public class MaterialsTabGenerator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws IOException {
        Constants.QUERY_PARAMS.put("category","nutrition,fitness");
        Response res = Helper.sendGetRequest(Constants.QUERY_PARAMS, "v4","programs");
        List<String> slugs = Helper.getSlugs(res);
        StringBuffer buffer = new StringBuffer();

        for (String slug : slugs) {

            Response rr = Helper.sendGetRequest("v4", "programs", slug);

            String ent = rr.get("$.items[0].entitlementGroup");
            if(slug.equals("2b-mindset")) {
                System.out.println(ent);

            }
            String s = "";
            if (ent.contains("freeuser")) {
                s = "free";
            } else if (ent.contains("allaccess")) {
                s = "club";
            } else {
                s = "entitlement";
            }
            buffer.append(String.format("%s.programType=%s", slug , s));
            buffer.append("\n");

            Response r = Helper.sendGetRequest("programMaterials", slug);
            buffer.append(String.format("%s.materialGroups=%s", slug ,r.get("$.items[*].title")));
            buffer.append("\n");
            JSONArray materials = JsonPath.read(r.body(), "$.items[*]");

            for (int i = 0; i < materials.size(); i++) {
                String groupName = r.get(String.format("$.items[%s].title", i)).replaceAll(":", "").replaceAll(" ", "").toLowerCase();
                buffer.append(String.format("%s.%s.materials=%s", slug, groupName, r.get(String.format("$.items[%s].data[*].title", i))).replaceAll("\\h", " ").replaceAll(" ,", ","));
                buffer.append("\n");

                JSONArray workouts = JsonPath.read(r.body(), String.format("$.items[%s].data[*]", i));

                for (int j = 0; j < workouts.size(); j++) {
                    String currentWorkoutPath = String.format("$.items[%s].data[%s]", i, j);

                    String workoutName = r.get(currentWorkoutPath + ".title").
                            replaceAll(" ", "").
                            replaceAll("\\s+", "").
                            replaceAll("\\h", "").
                            replaceAll("\u2028", "").
                            toLowerCase().replaceAll(":", "");
                    String materialTyp = r.get(currentWorkoutPath + ".type");
                    String description = r.get(currentWorkoutPath + ".description").replaceAll("’", "’");
                    //description = cleanTextContent(description);
                    String link = r.get(currentWorkoutPath + ".link")
                            .replaceAll("https://.*\\.cloudfront\\.net", "")
                            .replaceAll("user/beachbodyondemand/", "")
                            .replaceAll("/user/\\d+", "")
                            .replaceAll("http://.*\\.cloudfront\\.net", "").toLowerCase();

                    buffer.append(String.format("%s.%s.%s.description=%s", slug, groupName, workoutName, description));
                    buffer.append("\n");
                    buffer.append(String.format("%s.%s.%s.link=%s", slug, groupName, workoutName, link));
                    buffer.append("\n");
                    buffer.append(String.format("%s.%s.%s.type=%s", slug, groupName, workoutName, materialTyp));
                    buffer.append("\n");
                }
            }

        }
        Helper.writeInFile(Constants.PROGRAM_MATERIALS_PROPERTY_FILE_NAME,buffer.toString());
    }

    private static String cleanTextContent(String text)
    {
        // strips off all non-ASCII characters
//        text = text.replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

//        // removes non-printable characters from Unicode
//        text = text.replaceAll("\\p{C}", "");

        return text.trim();
    }



}