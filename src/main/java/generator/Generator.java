package generator;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import requests.Response;

import java.io.IOException;
import java.util.List;
public class Generator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws IOException {
        Response res = Helper.sendGetRequest("v4","programs");
        List<String> slugs = Helper.getSlugs(res);

        StringBuffer buffer = new StringBuffer();
        for (String s : slugs) {
            Response response = Helper.sendGetRequest("v4","programs",s);
            String slug = response.get("$.items[0].slug").toLowerCase().replaceAll(" ","").replaceAll(":","");
            String title = response.get("$.items[0].title");
            String description = response.get("$.items[0].longDescription.raw");
            String s1 = "Trainer:";
            JSONArray trainers = JsonPath.read(response.body(), String.format("$.items[0].trainers[*].title"));

            for(int j = 0; j < trainers.size(); j++) {
                String name = response.get(String.format("$.items[0].trainers[%s].title", j));
                s1+= name;
            }
            String programDurationType = response.get("$.items[0].programDurationType");
            if(programDurationType.endsWith("day")) programDurationType = "Days";
            if(programDurationType.endsWith("week")) programDurationType = "Weeks";
            String dur = response.get("$.items[0].programDuration.title");
            String programDuration = dur.isEmpty() ? "" : dur + " " + programDurationType;
            String programDurationIcon = response.get("$.items[0].programDuration.images.web.desktop.file");
            int durMin = Integer.parseInt(response.get("$.items[0].workoutDurationMinimum.title"));
            int durMax = Integer.parseInt(response.get("$.items[0].workoutDurationMaximum.title"));
            String duration = durMin + "-" + durMax + " mins";
            if (durMin == durMax) duration = durMax + " mins";
            String programDurationIconMinimum = response.get("$.items[0].workoutDurationMinimum.images.web.desktop.file");
            String programType = response.get("$.items[0].workoutType[0].title");
            String programTypeImage = response.get("$.items[0].workoutType[0].images.web.desktop.file");
            String programIntensity = response.get("$.items[0].programIntensity.title");
            String programIntensityIcon = response.get("$.items[0].programIntensity.images.web.desktop.file");

            String workoutTotalNum = String.valueOf(response.get("$.items[0].workoutTotalNum"));
            String a = " WORKOUTS";
            if(workoutTotalNum.equals("1")) a = " WORKOUT";
            String programPlanWorkouts = workoutTotalNum + a + (programDuration.isEmpty() ? "" : " , " + programDuration.toUpperCase());

            buffer.append(String.format("%s.%s=%s",slug,"title",title));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"description",description));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"trainers", s1));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"programDuration",programDuration));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"programDurationIcon",programDurationIcon));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"programDurationMins",duration));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"programDurationMinsIcon",programDurationIconMinimum));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"programType",programType));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"programTypeIcon",programTypeImage));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"programLevel",programIntensity));
            buffer.append("\n");
            buffer.append(String.format("%s.%s=%s",slug,"programLevelIcon",programIntensityIcon));
            buffer.append("\n");
            buffer.append(String.format("%s.fitnesLevel=%s", slug ,response.get(String.format("$.items[0].programIntensity.title"))));
            buffer.append("\n");
            buffer.append(String.format("%s.workoutDurationMaximum=%s", slug ,response.get(String.format("$.items[0].workoutDurationMaximum.id"))));
            buffer.append("\n");
            buffer.append(String.format("%s.workoutDurationMinimum=%s", slug ,response.get(String.format("$.items[0].workoutDurationMinimum.id"))));
            buffer.append("\n");
            buffer.append(String.format("%s.programPlanWorkouts=%s", slug ,programPlanWorkouts ));
            buffer.append("\n");

            String ss = response.get("items[0].social[0].slug");
            Response relatedesponse = Helper.sendGetRequest("socialFeeds", ss);
            String relatedTitle = relatedesponse.get("$.items[0].title");
            buffer.append(String.format("%s.relatedContent.title=%s", slug, relatedTitle));
            buffer.append("\n");
            JSONArray links = JsonPath.read(relatedesponse.body(), "$.items[0].socialLinks[*]");
            for(int i = 0; i < links.size(); i++) {
                String trainerDescription = relatedesponse.get(String.format("$.items[0].socialLinks[%s].title", i)).replaceAll("  ", " ");
                String linkImage = relatedesponse.get(String.format("$.items[0].socialLinks[%s].images.main.web.desktop.sourceUrl", i))
                        .replaceAll("https://d2rxohj08n82d5.cloudfront.net", "")
                        .replaceAll("http://d2rxohj08n82d5.cloudfront.net", "");
                String link = relatedesponse.get(String.format("$.items[0].socialLinks[%s].linkUrl", i));
                try {
                    String newLink;
                    Document doc = Jsoup.connect(link).get();
                    Element el = doc.selectFirst("link[rel='canonical']");
                    if(el != null) {
                        newLink = el.attr("href").toLowerCase();
                        if(!link.startsWith(newLink)) {
                            String sss = null;
                            if(link.contains("#")) {
                                sss = "#" + link.split("#")[1];
                                newLink += sss;
                            }
                            link = newLink;

                        }
                    }

                    buffer.append(String.format("%s.relatedContent.%s.link=%s", slug, i, link));
                    buffer.append("\n");
                } catch (HttpStatusException ex) {
                    System.out.println(slug);
                    System.out.println(i + 1 +  " relatedlink");
                    System.out.println(ex.getStatusCode());
                    buffer.append(String.format("%s.relatedContent.%s.link=%s", slug, i, link));
                    buffer.append("\n");
                }

                buffer.append(String.format("%s.relatedContent.%s.description=%s", slug, i, trainerDescription));
                buffer.append("\n");
                buffer.append(String.format("%s.relatedContent.%s.image=%s", slug, i, linkImage));
                buffer.append("\n");


            }

            if (!response.get("$.items[0].successStory").equals("[]") && !response.get("$.items[0].successStory").isEmpty()) {
                String successStorySlug = response.get("$.items[0].successStory[0].slug");
                Response successStoryesponse = Helper.sendGetRequest("v1","successStories", successStorySlug);
                String beforeImage = successStoryesponse.get("$.items[0].images.beforeImage.web.desktop.sourceUrl")
                        .replaceAll("https://d2rxohj08n82d5.cloudfront.net", "")
                        .replaceAll("http://d2rxohj08n82d5.cloudfront.net", "");
                String afterImage = successStoryesponse.get("$.items[0].images.afterImage.web.desktop.sourceUrl")
                        .replaceAll("https://d2rxohj08n82d5.cloudfront.net", "")
                        .replaceAll("http://d2rxohj08n82d5.cloudfront.net", "");;
                String headline = successStoryesponse.get("$.items[0].description");
                String testimonial = successStoryesponse.get("$.items[0].testimonial.raw");
                String name = successStoryesponse.get("$.items[0].name");
                String weightLost = successStoryesponse.get("$.items[0].weightLost");
                String weightType = successStoryesponse.get("$.items[0].weightType");
                String disclaimer = successStoryesponse.get("$.items[0].disclaimer").replaceAll("\\h", " ");
                buffer.append(String.format("%s.successStory.beforeImage=%s", slug, beforeImage));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.afterImage=%s", slug, afterImage));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.headline=%s", slug, headline));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.testimonial=%s", slug, testimonial));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.name=%s", slug, name));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.weightLost=%s", slug, weightLost));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.disclaimer=%s", slug, disclaimer));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.weightType=%s", slug, weightType));
                buffer.append("\n");
            }

            buffer.append(String.format("%s.heroDescriptionCopy=%s", slug, response.get("$.items[0].heroDescriptionCopy")));
            buffer.append("\n");

            boolean haveSizzleVideo = false;
            if(!response.get("$.items[0].digitalUnlock.sizzle_guid").isEmpty() || !response.get("$.items[0].digitalUnlock.sneak_peak_guid").isEmpty()) {
                haveSizzleVideo = true;
            }
            buffer.append(String.format("%s.haveHeroVideo=%s", slug, haveSizzleVideo));
            buffer.append("\n");
            if(haveSizzleVideo) {
                buffer.append(String.format("%s.playButtonText=%s", slug, response.get("$.items[0].digitalUnlock.teaserText")));
                buffer.append("\n");
            }
            String heroLogo = response.get("$.items[0].images.heroLogo.web.desktop.sourceUrl")
                    .replaceAll("https://d2rxohj08n82d5.cloudfront.net", "")
                    .replaceAll("http://d2rxohj08n82d5.cloudfront.net", "");
            buffer.append(String.format("%s.heroLogo=%s", slug ,heroLogo));
            buffer.append("\n");
            String heroImage = response.get("$.items[0].images.heroImageDesktop.web.desktop.sourceUrl")
                    .replaceAll("https://d2rxohj08n82d5.cloudfront.net", "")
                    .replaceAll("http://d2rxohj08n82d5.cloudfront.net", "");
            buffer.append(String.format("%s.heroImage=%s", slug ,heroImage));
            buffer.append("\n");

            }


        buffer.append("autumncalabrese.url=autumn-calabrese\n" +
                "joelfreeman.url=joel-freeman\n" +
                "idalisvelazquez.url=idalis-velazquez\n" +
                "shaunt.url=shaun-t\n" +
                "tonyhorton.url=tony-horton\n" +
                "chalenejohnson.url=chalene-johnson\n" +
                "sagikalev.url=sagi-kalev\n" +
                "jerichomcmatthews.url=jericho-mcmatthews\n" +
                "chrisdowning.url=chris-downing\n" +
                "megandavies.url=megan-davies\n" +
                "vytasbaskauskas.url=vytas\n" +
                "elisejoan.url=elise\n" +
                "tedmcdonald.url=ted\n" +
                "faithhunter.url=faith\n" +
                "beebosnak.url=bee\n" +
                "desibartlett.url=desi\n" +
                "caleyalyssa.url=caley-alyssa\n" +
                "leandrocarvalho.url=leandro-carvalho\n" +
                "debbiesiebers.url=debbie-siebers\n" +
                "bretthoebel.url=brett-hoebel\n" +
                "drcheng.url=dr-cheng\n" +
                "gillianteigh.url=gillianteigh\n" +
                "kathysmith.url=kathy-smith");
        Helper.writeInFile(Constants.DETAILS_PROPERTY_FILE_NAME,buffer.toString());
    }

}
