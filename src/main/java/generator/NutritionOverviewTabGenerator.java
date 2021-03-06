package generator;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import requests.Response;

import java.io.IOException;
import java.util.List;
public class NutritionOverviewTabGenerator {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void main(String[] args) throws IOException {
        Response res = Helper.sendGetRequest(Constants.QUERY_PARAMS,"v4","programs");
        List<String> slugs = Helper.getSlugs(res);
        StringBuffer buffer = new StringBuffer();
        Helper.writeInFile("program_materials",buffer.toString());


        for (String s : slugs) {
            Response response = Helper.sendGetRequest("v4","programs", s);
            System.out.println(response.getCurl());
            String d = response.get("$.items[0].brandCode");
            System.out.println(d);
            String slug = response.get("$.items[0].slug").toLowerCase().replaceAll(" ","").replaceAll(":","");
            String title = response.get("$.items[0].title").replaceAll(" ", "").toLowerCase();
            String titleText = response.get("$.items[0].title");
            String description = response.get("$.items[0].longDescription.raw");
            String shortdescription = response.get("$.items[0].shortDescription.raw");
            buffer.append(String.format("%s.description=%s", title, description));
            buffer.append("\n");
            String heroDescriptionCopy = response.get("$.items[0].heroDescriptionCopy");
            buffer.append(String.format("%s.heroDescriptionCopy=%s", title, heroDescriptionCopy));
            buffer.append("\n");
            String heroLogo = response.get("$.items[0].images.heroLogo.web.desktop.file");
            buffer.append(String.format("%s.heroLogo=%s", title, heroLogo));
            buffer.append("\n");
            String heroImage = response.get("$.items[0].images.heroImageDesktop.web.desktop.file");
            buffer.append(String.format("%s.heroImage=%s", title, heroImage));
            buffer.append("\n");
            buffer.append(String.format("%s.title=%s", title, titleText));
            buffer.append("\n");
//            String videoCount = response.get("$.items[0].videoCount");
//            buffer.append(String.format("%s.videoCount=%s", title, videoCount));
//            buffer.append("\n");
            buffer.append(String.format("%s.shortDescription=%s", title, shortdescription));
            buffer.append("\n");
            buffer.append(String.format("%s.url=%s", title, slug));
            buffer.append("\n");
            String html =  response.get("$.items[0].nutritionDescription.rendered");
            Document doc = Jsoup.parse(html);
            String detailTitle = doc.getElementsByTag("p").first().text();
            buffer.append(String.format("%s.detailDescription.title=%s", title, detailTitle));
            buffer.append("\n");

            Elements list = doc.getElementsByTag("li");
            for (int i =0; i < list.size(); i++) {
                String text = list.get(i).text();
                buffer.append(String.format("%s.listItems.%s=%s", title, i, text));
                buffer.append("\n");
            }

            Elements paragraphs = doc.getElementsByTag("p");
            for (int i = 1; i< paragraphs.size(); i++) {
                String text = paragraphs.get(i).text();
                buffer.append(String.format("%s.paragraphs.%s=%s", title, i - 1, text));
                buffer.append("\n");
            }

            JSONArray trainers = JsonPath.read(response.body(), "$.items[0].trainers[*]");
            buffer.append(String.format("%s.trainers.title=%s", title, trainers.size() > 1 ? "About The Experts": "About The Expert"));
            buffer.append("\n");
            for(int i = 0; i< trainers.size(); i++) {
                String trainername = response.get(String.format("$.items[0].trainers[%s].title", i));
                String trainerDescription = response.get(String.format("$.items[0].trainers[%s].description.raw", i)).replaceAll("\\h", " ");
                buffer.append(String.format("%s.trainers.%s.title=%s", title, i, trainername));
                buffer.append("\n");
                buffer.append(String.format("%s.trainers.%s.description=%s", title, i, trainerDescription));
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
                        .replaceAll("http://d2rxohj08n82d5.cloudfront.net", "");
                String headline = successStoryesponse.get("$.items[0].headline.raw");
                String testimonial = successStoryesponse.get("$.items[0].testimonial.raw");
                String name = successStoryesponse.get("$.items[0].name");
                String weightLost = successStoryesponse.get("$.items[0].weightLost");
                String weightType = successStoryesponse.get("$.items[0].weightType");
                String disclaimer = successStoryesponse.get("$.items[0].disclaimer");
                buffer.append(String.format("%s.successStory.beforeImage=%s", title, beforeImage));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.afterImage=%s", title, afterImage));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.headline=%s", title, headline));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.testimonial=%s", title, testimonial));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.name=%s", title, name));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.weightLost=%s", title, weightLost));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.disclaimer=%s", title, disclaimer));
                buffer.append("\n");
                buffer.append(String.format("%s.successStory.weightType=%s", title, weightType));
                buffer.append("\n");
            }

            String ss = response.get("items[0].social[0].slug");
            Response relatedesponse = Helper.sendGetRequest("socialFeeds", ss);
            String relatedTitle = relatedesponse.get("$.items[0].title");
            buffer.append(String.format("%s.relatedContent.title=%s", title, relatedTitle));
            buffer.append("\n");
            JSONArray links = JsonPath.read(relatedesponse.body(), "$.items[0].socialLinks[*]");
            for(int i = 0; i < links.size(); i++) {
                String trainerDescription = relatedesponse.get(String.format("$.items[0].socialLinks[%s].title", i));
                String linkImage = relatedesponse.get(String.format("$.items[0].socialLinks[%s].images.main.web.desktop.sourceUrl", i))

                        .replaceAll("https://d2rxohj08n82d5.cloudfront.net", "")
                        .replaceAll("http://d2rxohj08n82d5.cloudfront.net", "");
                String link = relatedesponse.get(String.format("$.items[0].socialLinks[%s].linkUrl", i)).toLowerCase();
                buffer.append(String.format("%s.relatedContent.%s.description=%s", title, i, trainerDescription));
                buffer.append("\n");
                buffer.append(String.format("%s.relatedContent.%s.image=%s", title, i, linkImage));
                buffer.append("\n");
                buffer.append(String.format("%s.relatedContent.%s.link=%s", title, i, link));
                buffer.append("\n");
            }
        }
        Helper.writeInFile(Constants.NUTRITION_PROPERTY_FILE_NAME,buffer.toString());
    }


}