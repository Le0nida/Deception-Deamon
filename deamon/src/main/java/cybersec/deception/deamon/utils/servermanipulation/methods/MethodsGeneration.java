package cybersec.deception.deamon.utils.servermanipulation.methods;

import cybersec.deception.deamon.utils.Utils;
import cybersec.deception.deamon.utils.servermanipulation.ControllerFilesUtils;

import java.util.List;

public class MethodsGeneration {

    private static String createMethodSignature;
    private static String deleteMethodSignature;
    private static String updateMethodSignature;
    private static String retrieveMethodSignature;

    private static String retrieveLoginSignature = "public ResponseEntity<String> login";
    private static String retrieveLogoutSignature = "public ResponseEntity<String> logout";

    private static void buildCRUDSignatures(String entityName){
        createMethodSignature = "public ResponseEntity<" + entityName + "> create" + entityName + "(";//@Parameter(in = ParameterIn.DEFAULT, description = \"Created " + entityName.toLowerCase() + " object\", schema=@Schema()) @Valid @RequestBody " + entityName + " body)";
        deleteMethodSignature = "public ResponseEntity<Void> delete" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"The name that needs to be deleted\", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name)";
        updateMethodSignature = "public ResponseEntity<" + entityName + "> update" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"name that need to be deleted\", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name, @Parameter(in = ParameterIn.DEFAULT, description = \"Update an existent " + entityName.toLowerCase() + " in the store\", schema=@Schema()) @Valid @RequestBody " + entityName + " body)";
        retrieveMethodSignature = "public ResponseEntity<" + entityName + "> retrieve" + entityName + "(";//@Parameter(in = ParameterIn.PATH, description = \"The name that needs to be fetched. Use " + entityName.toLowerCase() + "1 for testing. \", required=true, schema=@Schema()) @PathVariable(\"" + entityName.toLowerCase() + "name\") String " + entityName.toLowerCase() + "name)";
    }

    public static List<String> generateMethods(List<String> controllerContent, String entityName) {
        buildCRUDSignatures(entityName);

        generateJPACRUD(controllerContent, entityName);

        if (entityName.equals("User")) {
            generateJPAUserMethods(controllerContent);
        }

        // Rimuovo le stringhe che corrispondevano ai vecchi contenuti dei metodi
        Utils.removeEmptyStrings(controllerContent, "null");

        return controllerContent;
    }

    private static void generateJPACRUD(List<String> controllerContent, String entityName) {
        ControllerFilesUtils.substituteMethod(controllerContent, createMethodSignature, CRUDMethodsUtils.getJPACreateMethod(entityName));
        ControllerFilesUtils.substituteMethod(controllerContent, updateMethodSignature, CRUDMethodsUtils.getJPAUpdateMethod(entityName));
        ControllerFilesUtils.substituteMethod(controllerContent, retrieveMethodSignature, CRUDMethodsUtils.getJPARetrieveMethod(entityName));
        ControllerFilesUtils.substituteMethod(controllerContent, deleteMethodSignature, CRUDMethodsUtils.getJPADeleteMethod(entityName));
    }

    private static void generateJPAUserMethods(List<String> controllerContent) {
        ControllerFilesUtils.substituteMethod(controllerContent, retrieveLoginSignature, UserMethodsUtils.getJPALoginUserMethod());
        ControllerFilesUtils.substituteMethod(controllerContent, retrieveLogoutSignature, UserMethodsUtils.getJPALogoutUserMethod());
    }

}
