package cybersec.deception.deamon;

import cybersec.deception.deamon.services.ManagePersistenceService;
import cybersec.deception.deamon.services.ServerBuildingService;
import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.ServerBuildResponse;
import cybersec.deception.deamon.utils.Utils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${instructions.server}")
    private String instructionTxtPath;

    @Value("${dockerfile.path}")
    private String dockerFilePath;
    private final ServerBuildingService serverBuildingService;
    private final ManagePersistenceService persistenceService;

    @Autowired
    public ApiController(ServerBuildingService serverBuildingService, ManagePersistenceService persistenceService) {
        this.serverBuildingService = serverBuildingService;
        this.persistenceService = persistenceService;
    }

    @PostMapping("/buildSpringServer")
    public ResponseEntity<?> buildSpringServer(@RequestBody Map<String, Object> requestBody) {
        ServerBuildResponse response = new ServerBuildResponse();

        String yamlSpecString = (String) requestBody.get("yamlSpecString");
        boolean persistence = (boolean) requestBody.get("persistence");
        String basepath = requestBody.get("basePath") != null && !Utils.isNullOrEmpty((String) requestBody.get("basePath")) ? (String) requestBody.get("basePath") : "api";

        // controllo la validità del file .yaml
        if (validateOpenAPI(yamlSpecString).getStatusCode().equals(HttpStatusCode.valueOf(200))) {

            // genero il progetto nella directory di default
            this.serverBuildingService.buildBasicServerFromSwagger(yamlSpecString, basepath);

            if (persistence) {

                // manipolo il server generato per aggiungere la gestione della persistenza
                this.persistenceService.managePersistence();

                // recupero la lista di nomi di operazioni non implementate
                Map<String, List<String>> notImplMethods = this.persistenceService.getNotImplementedMethods();
                response.setNotImplMethods(notImplMethods);

                // genero e popolo il database
                this.persistenceService.setupDatabase(yamlSpecString);
            }

            byte[] zipFileContent;
            String serverDockerFile;
            String instructionsContent;
            try {
                zipFileContent = this.serverBuildingService.getZip();
                instructionsContent = FileUtils.readFileContent(instructionTxtPath);
                serverDockerFile = FileUtils.readFileContent(dockerFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            response.setServerZipFile(zipFileContent);
            response.setInstructions(instructionsContent);
            response.setServerDockerFile(serverDockerFile);

            // svuoto la directory con il server generato
            this.serverBuildingService.cleanDirectory();

            return ResponseEntity.ok().body(response);
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore durante la validazione OpenAPI");
        }
    }

    @PostMapping("/validateOpenAPISpec")
    public ResponseEntity<String> validateOpenAPI(@RequestBody String yamlString) {
        try {
            ParseOptions options = new ParseOptions();
            options.setResolve(true);
            SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(yamlString, null, options);

            if (parseResult.getMessages() == null || parseResult.getMessages().isEmpty()) {

                // Verifica la conformità rispetto alle specifiche OpenAPI
                OpenAPI openAPI = parseResult.getOpenAPI();
                if (openAPI == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore di conformità OpenAPI");
                }
                return ResponseEntity.ok("La specifica OpenAPI è valida.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore nella specifica OpenAPI:\n" + parseResult.getMessages());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante la validazione OpenAPI: " + e.getMessage());
        }
    }
}
