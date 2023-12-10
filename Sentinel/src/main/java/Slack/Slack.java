
package Slack;

import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Slack {

    private static HttpClient client = HttpClient.newHttpClient();
    private static final String URL = "aHR0cHM6Ly9ob29rcy5zbGFjay5jb20vc2VydmljZXMvVDA2NUNDRjlNVU4vQjA2ODVDRkpSTDcvYmxndjZOT3VNbFlPYjE1Q0dmOFlNeVpz";

    public static void sendMessage(JSONObject content) throws IOException, InterruptedException {

        byte[] encodedBytes = Base64.getDecoder().decode(URL);

        String urlDecoded = new String(encodedBytes, StandardCharsets.UTF_8);


        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(urlDecoded))
                .header("accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(content.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(String.format("Status: %s", response.statusCode()));
        System.out.println(String.format("Response: %s", response.body()));

    }
}
