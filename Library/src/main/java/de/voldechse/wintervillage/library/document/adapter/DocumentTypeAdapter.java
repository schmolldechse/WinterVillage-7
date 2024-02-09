package de.voldechse.wintervillage.library.document.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.voldechse.wintervillage.library.document.Document;

import java.io.IOException;

public class DocumentTypeAdapter extends TypeAdapter<Document> {

    protected final JsonParser PARSER = new JsonParser();

    @Override
    public void write(JsonWriter jsonWriter, Document document) throws IOException {
        jsonWriter.jsonValue(document.convertToJsonString());
    }

    @Override
    public Document read(JsonReader jsonReader) throws IOException {
        JsonElement jsonElement = PARSER.parse(jsonReader);
        return new Document(jsonElement.getAsJsonObject());
    }
}
