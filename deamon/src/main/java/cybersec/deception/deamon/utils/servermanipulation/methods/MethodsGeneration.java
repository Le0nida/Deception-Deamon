package cybersec.deception.deamon.utils.servermanipulation.methods;

import cybersec.deception.deamon.services.EntitiesManipulationService;
import cybersec.deception.deamon.utils.Utils;
import cybersec.deception.deamon.utils.servermanipulation.ControllerFilesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MethodsGeneration {

    private static String createMethodSignature;
    private static String deleteMethodSignature;
    private static String updateMethodSignature;
    private static String retrieveMethodSignature;

    private static final String loginSignature = "public ResponseEntity<String> loginUser(";
    private static final String adminSignature = "public ResponseEntity<String> loginAdmin(";
    private static final String logoutSignature = "public ResponseEntity<Void> logoutUser(";
    private static final String accessWorkstationSignature = "public ResponseEntity<String> accessWorkstation(";
    private static final String transferCryptoSignature = "public ResponseEntity<String> transfer(";
    private static final String requestMoneySignature = "public ResponseEntity<String> requestMoney(";
    private static final String transferMoneySignature = "public ResponseEntity<String> transferMoney(";

    private static void buildCRUDSignatures(String entityName){
        createMethodSignature = "public ResponseEntity<" + entityName + "> create" + entityName + "(";//@Parameter(in = ParameterIn.DEFAULT, description = \"Created " + entityName.toLowerCase() + " object\", schema=@Schema()) @Valid @RequestBody " + entityName + " body)";
        deleteMethodSignature = "public ResponseEntity<Void> delete" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"The name that needs to be deleted\", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name)";
        updateMethodSignature = "public ResponseEntity<" + entityName + "> update" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"name that need to be deleted\", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name, @Parameter(in = ParameterIn.DEFAULT, description = \"Update an existent " + entityName.toLowerCase() + " in the store\", schema=@Schema()) @Valid @RequestBody " + entityName + " body)";
        retrieveMethodSignature = "public ResponseEntity<" + entityName + "> retrieve" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"The name that needs to be fetched. Use " + entityName.toLowerCase() + "1 for testing. \", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name)";
    }

    private static EntitiesManipulationService entManipulationService;

    @Autowired
    public MethodsGeneration(EntitiesManipulationService entManipulationService) {
        MethodsGeneration.entManipulationService = entManipulationService;
    }

    public static List<String> getNotImplementedMethods(List<String> fileContent, String entityName, boolean persistence){
        // Creo le signature
        buildCRUDSignatures(entityName);

        List<String> result = new ArrayList<>();
        if (persistence) {
            for (String s: extractMethodSignatures(fileContent)){
                result.add(s.substring(0, s.indexOf("(")));
            }
        }
        else {
            for (String line : fileContent) {
                if (line.trim().startsWith("public ResponseEntity<")) {
                    result.add(line.trim().substring(0, line.indexOf("(")));
                }
            }
        }
        return result;
    }


    private static List<String> extractMethodSignatures(List<String> fileContent) {
        List<String> methodSignatures = new ArrayList<>();
        for (String line : fileContent) {
            if (line.trim().startsWith("public ResponseEntity<") &&
                    !line.trim().startsWith(createMethodSignature) &&
                    !line.trim().startsWith(deleteMethodSignature) &&
                    !line.trim().startsWith(updateMethodSignature) &&
                    !line.trim().startsWith(retrieveMethodSignature) &&
                    !line.trim().startsWith(loginSignature) &&
                    !line.trim().startsWith(logoutSignature) &&
                    !line.trim().startsWith(adminSignature) &&
                    !line.trim().startsWith(accessWorkstationSignature) &&
                    !line.trim().startsWith(transferCryptoSignature) &&
                    !line.trim().startsWith(transferMoneySignature) &&
                    !line.trim().startsWith(requestMoneySignature)) {
                methodSignatures.add(line.trim());
            }
        }
        return methodSignatures;
    }
    public static List<String> generateMethods(List<String> controllerContent, String entityName) {

        // Creo le signature
        buildCRUDSignatures(entityName);

        // Creo i metodi CRUD
        generateJPACRUD(controllerContent, entityName);

        // Genero login e logout
        if (entityName.equals("User")) {
            generateJPAUserMethods(controllerContent);
        } else if (entityName.equals("Crypto")) {
            generateJPACryptoMethods(controllerContent);
        } else if (entityName.equals("Workstation")) {
            generateJPAWorkstationMethods(controllerContent);
        }

        // Rimuovo le stringhe che corrispondevano ai vecchi contenuti dei metodi
        Utils.removeEmptyStrings(controllerContent, "null");

        // Modifico tutti i metodi non implementati
        modifyNotImpl(controllerContent);

        return controllerContent;
    }

    private static void generateJPACRUD(List<String> controllerContent, String entityName) {
        ControllerFilesUtils.substituteMethod(controllerContent, createMethodSignature, CRUDMethodsUtils.getJPACreateMethod(entityName));
        ControllerFilesUtils.substituteMethod(controllerContent, updateMethodSignature, CRUDMethodsUtils.getJPAUpdateMethod(entityName));
        ControllerFilesUtils.substituteMethod(controllerContent, retrieveMethodSignature, CRUDMethodsUtils.getJPARetrieveMethod(entityName));
        ControllerFilesUtils.substituteMethod(controllerContent, deleteMethodSignature, CRUDMethodsUtils.getJPADeleteMethod(entityName));
    }

    private static void generateJPAUserMethods(List<String> controllerContent) {
        ControllerFilesUtils.substituteMethod(controllerContent, adminSignature, UserMethodsUtils.getJPALoginUserMethod());
        ControllerFilesUtils.substituteMethod(controllerContent, loginSignature, UserMethodsUtils.getJPALoginUserMethod());
        ControllerFilesUtils.substituteMethod(controllerContent, logoutSignature, UserMethodsUtils.getJPALogoutUserMethod());
    }

    private static void generateJPAWorkstationMethods(List<String> controllerContent) {
        ControllerFilesUtils.substituteMethod(controllerContent, accessWorkstationSignature, WorkstationMethodUtils.getJPAAccessWorkstationMethod());
    }

    private static void generateJPACryptoMethods(List<String> controllerContent) {
        ControllerFilesUtils.substituteMethod(controllerContent, transferCryptoSignature, CryptoMethodUtils.getJPATransferCryptoMethod());
    }

    public static List<String> modifyNotImpl(List<String> controllerContent) {
        List<String> methodSignatures = extractMethodSignatures(controllerContent);
        boolean found = false;
        Random random = new Random();
        for (int i = 0; i < controllerContent.size(); i++) {
            String line = controllerContent.get(i);
            String dateText =
                    "                      Date d = new Date(request.getSession().getCreationTime());\n" +
                            "                      String minutes = \"\"+d.getMinutes();\n" +
                            "                      if (minutes.contains(\""+random.nextInt(6)+"\") || minutes.contains(\""+random.nextInt(6)+"\") || minutes.contains(\""+random.nextInt(6)+"\")) {\n" +
                            "                           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);\n" +
                            "                      }";
            String voidText =
                    "                       else { if (minutes.contains(\""+random.nextInt(6)+"\") || minutes.contains(\""+random.nextInt(6)+"\") || minutes.contains(\""+random.nextInt(6)+"\")) {\n" +
                            "                           return new ResponseEntity<>(HttpStatus.OK);\n" +
                            "                       }";

            // metodo non implementato
            if (!found && methodSignatures.contains(line.trim())) {
                found = true;
            }

            if (found) {

                if (line.contains("String accept = request.getHeader(\"Accept\");") &&
                        controllerContent.get(i+1).contains("return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);")) {
                    controllerContent.set(i, line + "\n"+dateText + voidText);
                }

                if (line.contains("objectMapper.readValue")) {

                    // Recupero il JSON
                    String init = line.substring(0, line.indexOf("objectMapper.readValue"));
                    String entity = "", json = "";
                    if (line.contains("<List<")) {
                        entity = line.substring(line.indexOf("<List<") + 1, line.indexOf(">>"));
                        json = entManipulationService.getMultiRandomEntityByName(entity);
                    } else {
                        entity = line.substring(line.indexOf("<") + 1, line.indexOf(">"));
                        json = entManipulationService.getRandomEntityByName(entity);
                    }
                    if (!Utils.isNullOrEmpty(json)) {
                        controllerContent.set(i, line.substring(0, line.indexOf("(\"") + 2) + json.trim().replace("\"", "\\\"").replace("\n","\\r\\n") + line.substring(line.lastIndexOf("\", ")));
                    }
                }

                if (line.contains("try {")) {
                    controllerContent.set(i, "                      try {\n" + dateText);
                    continue;
                }

                // ritorno entità
                if (line.contains(", HttpStatus.NOT_IMPLEMENTED);")) {
                    controllerContent.set(i, controllerContent.get(i).replace(", HttpStatus.NOT_IMPLEMENTED);",", HttpStatus.OK);"));
                    continue;
                }

                // fine metodo
                if (line.contains(">(HttpStatus.NOT_IMPLEMENTED);")) {
                    controllerContent.set(i, line.replace(">(HttpStatus.NOT_IMPLEMENTED);", ">(HttpStatus.BAD_REQUEST);"));
                    found = false;
                }
            }


        }
        return controllerContent;
    }

}
