package de.voldechse.wintervillage.library.document;

import com.google.gson.*;
import de.voldechse.wintervillage.library.document.adapter.DocumentTypeAdapter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Document implements DocumentAbstract {

    public static Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(Document.class, new DocumentTypeAdapter())
            .create();

    protected static final JsonParser PARSER = new JsonParser();

    public String getName() {
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected String name;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private File file;

    protected JsonObject dataCatcher;

    public Document(String name) {
        this.name = name;
        this.dataCatcher = new JsonObject();
    }

    public Document(String name, JsonObject source) {
        this.name = name;
        this.dataCatcher = source;
    }

    public Document(File file, JsonObject jsonObject) {
        this.file = file;
        this.dataCatcher = jsonObject;
    }

    public Document(String key, String value) {
        this.dataCatcher = new JsonObject();
        this.append(key, value);
    }

    public Document(String key, Object value) {
        this.dataCatcher = new JsonObject();
        this.append(key, value);
    }

    public Document(String key, Number value) {
        this.dataCatcher = new JsonObject();
        this.append(key, value);
    }

    public Document(Document defaults) {
        this.dataCatcher = defaults.dataCatcher;
    }

    public Document(Document defaults, String name) {
        this.dataCatcher = defaults.dataCatcher;
        this.name = name;
    }

    public Document() {
        this.dataCatcher = new JsonObject();
    }

    public Document(JsonObject source) {
        this.dataCatcher = source;
    }

    public JsonObject obj() {
        return dataCatcher;
    }

    public boolean contains(String key) {
        return this.dataCatcher.has(key);
    }

    public Document append(String key, String value) {
        if (value == null) return this;
        this.dataCatcher.addProperty(key, value);
        return this;
    }

    public Document append(String key, Number value) {
        if (value == null) return this;
        this.dataCatcher.addProperty(key, value);
        return this;
    }

    public Document append(String key, Boolean value) {
        if (value == null) return this;
        this.dataCatcher.addProperty(key, value);
        return this;
    }

    @Override
    public Document append(String key, JsonElement value) {
        if (value == null) return this;
        this.dataCatcher.add(key, value);
        return this;
    }

    public Document appendList(String key, List<?> values) {
        if (values == null) return this;
        JsonArray jsonElements = new JsonArray();

        for (Object a : values) jsonElements.add(GSON.toJsonTree(a));

        this.dataCatcher.add(key, jsonElements);
        return this;
    }

    public Document append(String key, Document value) {
        if (value == null) return this;
        this.dataCatcher.add(key, value.dataCatcher);
        return this;
    }

    @Deprecated
    public Document append(String key, Object value) {
        if (value == null) return this;
        this.dataCatcher.add(key, GSON.toJsonTree(value));
        return this;
    }

    public Document appendValues(Map<String, Object> values) {
        for (Map.Entry<String, Object> a : values.entrySet()) append(a.getKey(), a.getValue());
        return this;
    }

    @Override
    public Document remove(String key) {
        this.dataCatcher.remove(key);
        return this;
    }

    public Set<String> keys() {
        Set<String> a = new HashSet<>();

        for (Map.Entry<String, JsonElement> b : dataCatcher.entrySet()) a.add(b.getKey());

        return a;
    }

    public JsonElement get(String key) {
        if (!dataCatcher.has(key)) return null;
        return dataCatcher.get(key);
    }

    @Override
    public String getString(String key) {
        if (!dataCatcher.has(key)) return null;
        return dataCatcher.get(key).getAsString();
    }

    @Override
    public int getInt(String key) {
        if (!dataCatcher.has(key)) return 0;
        return dataCatcher.get(key).getAsInt();
    }

    @Override
    public long getLong(String key) {
        if (!dataCatcher.has(key)) return 0L;
        return dataCatcher.get(key).getAsLong();
    }

    @Override
    public double getDouble(String key) {
        if (!dataCatcher.has(key)) return 0D;
        return dataCatcher.get(key).getAsDouble();
    }

    @Override
    public float getFloat(String key) {
        if (!dataCatcher.has(key)) return 0F;
        return dataCatcher.get(key).getAsFloat();
    }

    @Override
    public short getShort(String key) {
        if (!dataCatcher.has(key)) return 0;
        return dataCatcher.get(key).getAsShort();
    }

    @Override
    public boolean getBoolean(String key) {
        if (!dataCatcher.has(key)) return false;
        return dataCatcher.get(key).getAsBoolean();
    }

    public <T> T getObject(String key, Class<T> a) {
        if (!dataCatcher.has(key)) return null;
        JsonElement jsonElement = dataCatcher.get(key);
        return GSON.fromJson(jsonElement, a);
    }

    public Document getDocument(String key) {
        Document document = new Document(dataCatcher.get(key).getAsJsonObject());
        return document;
    }

    public Document clear() {
        for (String key : keys()) remove(key);
        return this;
    }

    public int size() {
        return this.dataCatcher.size();
    }

    public Document loadProperties(Properties properties) {
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            Object a = enumeration.nextElement();
            this.append(a.toString(), properties.getProperty(a.toString()));
        }
        return this;
    }

    public boolean isEmpty() {
        return this.dataCatcher.size() == 0;
    }

    public JsonArray getArray(String key) {
        return dataCatcher.get(key).getAsJsonArray();
    }

    public String convertToJson() {
        return GSON.toJson(dataCatcher);
    }

    public String convertToJsonString() {
        return dataCatcher.toString();
    }

    public String convertToJsonString_pretty() {
        JsonObject jsonObject = PARSER.parse(dataCatcher.toString()).getAsJsonObject();
        return GSON.toJson(jsonObject);
    }

    public boolean saveAsConfig(File backend) {
        if (backend == null) return false;
        if (backend.exists()) backend.delete();

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(backend), "UTF-8")) {
            GSON.toJson(dataCatcher, (writer));
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    /**
     * VERALTET
     *
     * @Deprecated public boolean saveAsConfig0(File backend) {
     * if (backend == null) return false;
     * if (backend.exists()) backend.delete();
     * <p>
     * try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(backend), "UTF-8")) {
     * GSON.toJson(dataCatcher, (writer));
     * return true;
     * } catch (IOException exception) {
     * exception.getStackTrace();
     * }
     * return false;
     * }
     */

    public boolean saveAsConfig(Path path) {
        try {
            if (!Files.exists(path)) Files.createFile(path);

            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(Files.newOutputStream(path), "UTF-8")) {
                GSON.toJson(dataCatcher, outputStreamWriter);
                return true;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public boolean saveAsConfig(String path) {
        return saveAsConfig(Paths.get(path));
    }

    public static Document loadDocument(File backend) {
        return loadDocument(backend.toPath());
    }

    public static Document $loadDocument(File backend) throws Exception {
        try {
            return new Document(PARSER.parse(new String(Files.readAllBytes(backend.toPath()), StandardCharsets.UTF_8)).getAsJsonObject());
        } catch (Exception exception) {
            throw new Exception(exception);
        }
    }

    public static Document loadDocument(Path backend) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(Files.newInputStream(backend), "UTF-8");
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            JsonObject jsonObject = PARSER.parse(bufferedReader).getAsJsonObject();
            return new Document(jsonObject);
        } catch (Exception exception) {
            exception.printStackTrace();
            ;
        }
        return new Document();
    }

    public Document loadToExistingDocument(File backend) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(backend), "UTF-8")) {

            this.dataCatcher = PARSER.parse(reader).getAsJsonObject();
            this.file = backend;
            return this;
        } catch (Exception exception) {
            exception.getStackTrace();
        }
        return new Document();
    }

    public Document loadToExistingDocument(Path path) {
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(path), "UTF-8")) {

            this.dataCatcher = PARSER.parse(reader).getAsJsonObject();
            return this;
        } catch (Exception exception) {
            exception.getStackTrace();
        }
        return new Document();
    }

    public static Document load(String input) {
        try (InputStreamReader reader = new InputStreamReader(new StringBufferInputStream(input), "UTF-8")) {
            return new Document(PARSER.parse(new BufferedReader(reader)).getAsJsonObject());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return new Document();
    }

    @Override
    public String toString() {
        return convertToJsonString();
    }

    public static Document load(JsonObject input) {
        return new Document(input);
    }

    public <T> T getObject(String key, Type type) {
        if (!contains(key)) return null;

        return GSON.fromJson(dataCatcher.get(key), type);
    }

    public byte[] toBytesAsUTF_8() {
        return convertToJsonString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toBytes() {
        return convertToJsonString().getBytes();
    }
}