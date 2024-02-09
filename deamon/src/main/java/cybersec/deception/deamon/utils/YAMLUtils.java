package cybersec.deception.deamon.utils;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.Reader;
import java.util.*;

@Component
public class YAMLUtils {

    public static Map<String, List<String>> getComponentsProperties(String yamlString) {
        List<String> componentNames = getComponentsFromSchema(yamlString);
        Map<String, List<String>> result = new LinkedHashMap<>();

        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(yamlString);

        // Extract components
        Map<String, Object> components = (Map<String, Object>) yamlData.get("components");

        if (components != null) {
            for (String componentName : componentNames) {
                if (components.containsKey("schemas")) {
                    Map<String, Object> schemas = (Map<String, Object>) components.get("schemas");
                    if (schemas.containsKey(componentName)) {
                        Map<String, Object> schemaProperties = (Map<String, Object>) schemas.get(componentName);
                        List<String> properties = extractProperties(schemaProperties);
                        result.put(componentName, properties);
                    }
                }
            }
        }

        return result;
    }

    private static List<String> getComponentsFromSchema(String yamlString) {
        List<String> schemaComponents = new ArrayList<>();

        // Parsing della stringa YAML
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(yamlString);

        // Estrazione dei componenti dello schema
        Map<String, Object> components = (Map<String, Object>) yamlData.get("components");
        if (components != null) {
            Map<String, Object> schemas = (Map<String, Object>) components.get("schemas");
            if (schemas != null) {
                schemaComponents.addAll(schemas.keySet());
            }
        }

        return schemaComponents;
    }

    private static List<String> extractProperties(Map<String, Object> schemaProperties) {
        List<String> properties = new ArrayList<>();


        Map<String, List<String>> propMap = (Map<String, List<String>>) schemaProperties.get("properties");

        Set<Map.Entry<String, List<String>>> s = propMap.entrySet();
        for (Map.Entry<String, List<String>> entry : s) {
            properties.add(entry.getKey());
        }

        return properties;
    }

}
