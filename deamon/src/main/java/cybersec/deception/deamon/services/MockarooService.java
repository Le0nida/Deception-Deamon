package cybersec.deception.deamon.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MockarooService {

    @Value("${mockaroo.data.types}")
    public String mockarooDataTypes;

    public HttpRequest buildSQLRequest(String schema) {
        return buildRequest("https://api.mockaroo.com/api/generate.sql?key=b324d530&count=1000", schema);
    }

    private HttpRequest buildRequest(String url, String schema) {
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(schema))
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpRequest buildJSONRequest(String schema) {
        schema = schema.replace("[{\"", "[{\"name\":\"id\",\"type\":\"Row Number\"},{\"");
        return buildRequest("https://api.mockaroo.com/api/generate.json?key=b324d530&count=1000", schema);
    }

    public String generateData(HttpRequest request) {

        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            return null;
        }
    }

    public String createTable(String fileName, String requestBody) {
        Map<String, String> map = convertJsonStringToMap(requestBody);
        StringBuilder builder = new StringBuilder();
        builder.append("create table ").append(fileName).append(" (\n").append("\tid INT AUTO_INCREMENT,\n");
        for (Map.Entry<String, String> entry: map.entrySet()) {
            String type = getTypeForAttribute(entry.getValue());
            builder.append("\t").append(entry.getKey()).append(" ").append(type).append(",\n");
        }
        builder.append("\tPRIMARY KEY (id)\n);");
        return builder.toString();
    }

    private Map<String, String> convertJsonStringToMap(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Deserializzazione della stringa JSON in una List<Map<String, String>>
        List<Map<String, String>> list = null;
        try {
            list = objectMapper.readValue(jsonString, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Estrazione dei valori name e type in una mappa
        Map<String, String> resultMap = new java.util.HashMap<>();
        for (Map<String, String> map : list) {
            resultMap.put(map.get("name"), map.get("type"));
        }

        return resultMap;
    }

    private String getTypeForAttribute(String attributeName){
        // Lettura del file JSON
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(mockarooDataTypes);
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Estrazione della lista di tipi
        JsonNode typesNode = rootNode.get("types");
        List<LinkedHashMap<String, Object>> typesList = objectMapper.convertValue(typesNode, List.class);

        // Ricerca dell'attributo specificato
        for (LinkedHashMap<String, Object> typeNode : typesList) {
            String typeName = typeNode.get("name").toString();
            if (attributeName.equals(typeName)) {
                // Trovato l'attributo, determina il tipo
                String typeString = typeNode.get("type").toString();
                if (typeString != null) {
                    if (typeString.contains("|")) {
                        // Più tipi separati da |
                        String[] possibleTypes = typeString.split("\\|");
                        for (String possibleType : possibleTypes) {
                            if (possibleType.trim().equals("string")) {
                                return "VARCHAR(200)";
                            } else if (possibleType.trim().equals("float")) {
                                return "DECIMAL(9,2)";
                            } else if (possibleType.trim().equals("integer")) {
                                return "INT";
                            }
                        }
                    } else {
                        // Tipo singolo
                        if (typeString.trim().equals("string")) {
                            return "VARCHAR(200)";
                        } else if (typeString.trim().equals("float")) {
                            return "DECIMAL(9,2)";
                        } else if (typeString.trim().equals("integer")) {
                            return "INT";
                        }
                    }

                }
                return "VARCHAR(200)";
            }
        }

        return "VARCHAR(200)";
    }
}
