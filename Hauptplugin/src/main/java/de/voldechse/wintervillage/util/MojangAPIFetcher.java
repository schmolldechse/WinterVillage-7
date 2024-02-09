package de.voldechse.wintervillage.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Logger;

public class MojangAPIFetcher {

    public UUID uuidFrom(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null)
                    response.append(line);
                reader.close();

                String jsonResponse = response.toString();
                String uuidString = jsonResponse.split("\"id\" : \"")[1].split("\",")[0];

                String hyphenated = String.format(
                        "%s-%s-%s-%s-%s",
                        uuidString.substring(0, 8),
                        uuidString.substring(8, 12),
                        uuidString.substring(12, 16),
                        uuidString.substring(16, 20),
                        uuidString.substring(20)
                );

                return UUID.fromString(hyphenated);
            } else {
                InjectionLayer.ext().instance(WinterVillage.class).getInstance().getLogger().severe("Could not find uuid from " + name);
                return null;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public String stringFrom(UUID uuid) {
        String url = "https://sessionserver.mojang.com/session/minecraft/profile" + uuid;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null)
                    response.append(line);
                reader.close();

                String jsonResponse = response.toString();

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();

                return jsonObject.getAsJsonPrimitive("name").getAsString();
            } else {
                InjectionLayer.ext().instance(WinterVillage.class).getInstance().getLogger().severe("Could not find name from " + uuid);
                return null;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}