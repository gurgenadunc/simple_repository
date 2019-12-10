package generator;

import requests.RequestBuilder;
import requests.Response;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Helper {

    public static Response sendGetRequest(String ...path) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(Constants.CONTENT_ENDPOINT);
        requestBuilder.addHeader(Constants.CONTENT_HEADERS);
        requestBuilder.addPathParameters(path);
        return requestBuilder.get();
    }

    public static void writeInFile(String fileName, String text) throws IOException {
        File file = new File("src/main/resources/" + (fileName.endsWith(".properties") ? fileName : (fileName + ".properties")));
        if (!file.exists()) file.createNewFile();
        BufferedWriter bf = new BufferedWriter(new FileWriter(file));
        bf.write(text);
        bf.flush();
    }

    public static void appendToBufferWithNewLine (StringBuilder builder, String value) {
        builder.append(value).append("\n");
    }

    public static List<String> getSlugs (Response response) {
        return Arrays.asList(response.get("$.items[*].slug", Constants.SEPARATOR).split(Constants.SEPARATOR))
                .stream()
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
