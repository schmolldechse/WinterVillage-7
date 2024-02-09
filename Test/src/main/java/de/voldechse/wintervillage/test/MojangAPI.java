package de.voldechse.wintervillage.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MojangAPI {

    private final HttpClient httpClient;

    public MojangAPI(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    public MojangAPI() {
        this(HttpClient.newHttpClient());
    }

    public CompletableFuture<Optional<UUID>> lookupUniqueId(String username) {
        CompletableFuture<HttpResponse<InputStream>> future = this.httpClient.sendAsync(HttpRequest
                .newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                .header("Accept", "application/json")
                .header("User-Agent", "WinterVillage")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        return future.thenApply((HttpResponse<InputStream> response) -> {
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                JsonObject jsonObject = readJsonObject(response);

                String id = jsonObject.get("id").getAsString();
                UUID uuid = UUIDHelper.dashed(id);
                return Optional.of(uuid);
            } else return handleNotOkay(response);
        });
    }

    public CompletableFuture<Optional<String>> lookupUsername(UUID uniqueId) {
        CompletableFuture<HttpResponse<InputStream>> future = this.httpClient.sendAsync(HttpRequest
                .newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile" + UUIDHelper.undashed(uniqueId)))
                .header("Accept", "application/json")
                .header("User-Agent", "WinterVillage")
                .timeout(Duration.ofSeconds(5))
                .build(), HttpResponse.BodyHandlers.ofInputStream());

        return future.thenApply((HttpResponse<InputStream> response) -> {
            int statusCode = response.statusCode();

            if (statusCode == 200) {
                JsonObject jsonObject = readJsonObject(response);
                String username = jsonObject.get("name").getAsString();
                return Optional.of(username);
            } else return handleNotOkay(response);
        });
    }

    private <T> Optional<T> handleNotOkay(HttpResponse<InputStream> response) {
        int statusCode = response.statusCode();

        switch (statusCode) {
            case 204 -> {
                return handleNoContent(response);
            }

            case 400 -> {
                return handleBadRequest(response);
            }

            default -> {
                return handleUnknownStatusCode(response);
            }
        }
    }

    private <T> Optional<T> handleNoContent(HttpResponse<InputStream> response) {
        return Optional.empty();
    }

    private <T> Optional<T> handleBadRequest(HttpResponse<InputStream> response) {
        JsonObject jsonObject = readJsonObject(response);

        String error = jsonObject.get("error").getAsString();
        String errorMessage = jsonObject.get("errorMessage").getAsString();

        throw new RuntimeException("We sent a bad request to Mojang. We got a(n) " + error + " with the following message: " + errorMessage);
    }

    private <T> Optional<T> handleUnknownStatusCode(HttpResponse<InputStream> response) {
        throw new RuntimeException("Unexpected status code from Mojang API: " + response.statusCode());
    }

    private JsonObject readJsonObject(HttpResponse<InputStream> response) {
        Charset charset = charsetFromHeaders(response.headers());

        try (InputStream inputStream = response.body();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset)) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(inputStreamReader, JsonObject.class);

            if (jsonObject != null) return jsonObject;
            else throw new RuntimeException("Expected response to be represented as a Json Object, but it was null");
        } catch (IOException exception) {
            throw new RuntimeException("Could not read http response body", exception);
        } catch (JsonParseException exception) {
            throw new RuntimeException("Invalid Json from API", exception);
        }
    }

    /**
    private JsonObject readJsonObject(HttpResponse<InputStream> response) {
        Charset charset = charsetFromHeaders(response.headers());

        try (InputStream inputStream = response.body();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset)) {
            Object json = new JsonParser().parse(inputStreamReader);
            if (json instanceof JsonObject) {
                return (JsonObject) json;
            } else {
                throw new RuntimeException("Expected response to be represented as a Json Object, instead we got: " + json);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not read http response body", exception);
        } catch (ParseException exception) {
            throw new RuntimeException("Invalid Json from api", exception);
        }
    }
     **/

    private Charset charsetFromHeaders(HttpHeaders headers) {
        Optional<String> optionalContentType = headers.firstValue("Content-Type");
        if (optionalContentType.isPresent()) {
            String contentType = optionalContentType.get();
            int indexOfSemi = contentType.indexOf(";");

            if (indexOfSemi != -1) {
                String charsetPart = contentType.substring(indexOfSemi + 1).trim();
                String[] charSetKeyAndValue = charsetPart.split("=", 2);
                if (charSetKeyAndValue.length == 2 && "charset".equalsIgnoreCase(charSetKeyAndValue[0])) {
                    String charsetName = charSetKeyAndValue[1];
                    return Charset.forName(charsetName);
                }
            }
        }

        return StandardCharsets.UTF_8;
    }
}
