package cybersec.deception.deamon;

import cybersec.deception.deamon.model.SecurityConfig;
import cybersec.deception.deamon.model.ServerBuildResponse;
import cybersec.deception.deamon.services.*;
import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.Utils;
import cybersec.deception.deamon.utils.servermanipulation.ControllerFilesUtils;
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
    private final LoggingService logService;
    private final ApiUtilsService apiUtilsService;
    private final SecurityService securityService;


    @Autowired
    public ApiController(ServerBuildingService serverBuildingService, ManagePersistenceService persistenceService, LoggingService logService, ApiUtilsService apiUtilsService, SecurityService securityService) {
        this.serverBuildingService = serverBuildingService;
        this.persistenceService = persistenceService;
        this.logService = logService;
        this.apiUtilsService = apiUtilsService;
        this.securityService = securityService;
    }


    @PostMapping("/buildSpringServer")
    public ResponseEntity<?> buildSpringServer(@RequestBody Map<String, Object> requestBody) {
        ServerBuildResponse response = new ServerBuildResponse();

        String yamlSpecString = (String) requestBody.get("yamlSpecString");
        boolean persistence = (boolean) requestBody.get("persistence");
        String basepath = requestBody.get("basePath") != null && !Utils.isNullOrEmpty((String) requestBody.get("basePath")) ? (String) requestBody.get("basePath") : "api";
        boolean docs = (boolean) requestBody.get("docs");
        SecurityConfig securityConfig = null;
        if (requestBody.get("securityConfig") != null) {
            securityConfig = (SecurityConfig) requestBody.get("securityConfig");
        }

        // controllo la validità del file .yaml
        if (validateOpenAPI(yamlSpecString).getStatusCode().equals(HttpStatusCode.valueOf(200))) {

            String tableCode = Utils.generateRandomString(7);

            // genero il progetto nella directory di default
            this.serverBuildingService.buildBasicServerFromSwagger(yamlSpecString, basepath);

            if (!docs) {
                this.serverBuildingService.removeDocs();
            }

            if (persistence) {
                // manipolo il server generato per aggiungere la gestione della persistenza
                this.persistenceService.managePersistence(tableCode);

                // genero e popolo le tabelle
                this.persistenceService.setupDatabase(yamlSpecString, tableCode);
            }

            String notImplMethods = ControllerFilesUtils.getNotImplementedMethods();

            if (!persistence) {
                this.serverBuildingService.buildNotImplementedMethods();
            }

            if (securityConfig != null) {
                this.securityService.manageSecurity(securityConfig);
            }


            // aggiungo la logica per inserire delay ed errori nei metodi
            this.apiUtilsService.addApiUtils();

            // aggiungo tutta la gestione del logging privato
            this.logService.manageLogging(tableCode, persistence);


            // Costruzione della risposta
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
            response.setNotImplMethods(notImplMethods);


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
