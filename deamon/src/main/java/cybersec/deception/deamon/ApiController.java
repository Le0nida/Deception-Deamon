package cybersec.deception.deamon;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import cybersec.deception.deamon.services.DockerHubBuildService;
import cybersec.deception.deamon.services.ManagePersistenceService;
import cybersec.deception.deamon.services.ServerBuildingService;
import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.ServerBuildResponse;
import cybersec.deception.deamon.utils.Utils;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static cybersec.deception.deamon.utils.FileUtils.readFileContent;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${instructions.server}")
    private String instructionTxtPath;
    private final ServerBuildingService serverBuildingService;
    private final DockerHubBuildService dockerService;
    private final ManagePersistenceService persistenceService;

    @Autowired
    public ApiController(ServerBuildingService serverBuildingService, DockerHubBuildService dockerService, ManagePersistenceService persistenceService) {
        this.serverBuildingService = serverBuildingService;
        this.dockerService = dockerService;
        this.persistenceService = persistenceService;
    }

    @GetMapping("/downloadZip")
    public ResponseEntity<byte[]> downloadZip() {
        //sbService.buildServerFromSwagger();
        return null;
    }

    @GetMapping("/")
    public String index() {

        // genero il progetto nella directory di default
        this.serverBuildingService.buildBasicServerFromSwagger("openapi: 3.0.3\n" +
                "info:\n" +
                "  title: Swagger Petstore - OpenAPI 3.0\n" +
                "  description: >-\n" +
                "    This is a sample Pet Store Server based on the OpenAPI 3.0 specification. \n" +
                "    You can find out more about\n" +
                "\n" +
                "    Swagger at [https://swagger.io](https://swagger.io). In the third iteration\n" +
                "    of the pet store, we've switched to the design first approach!\n" +
                "\n" +
                "    You can now help us improve the API whether it's by making changes to the\n" +
                "    definition itself or to the code.\n" +
                "\n" +
                "    That way, with time, we can improve the API in general, and expose some of\n" +
                "    the new features in OAS3.\n" +
                "\n" +
                "\n" +
                "    _If you're looking for the Swagger 2.0/OAS 2.0 version of Petstore, then\n" +
                "    click\n" +
                "    [here](https://editor.swagger.io/?url=https://petstore.swagger.io/v2/swagger.yaml).\n" +
                "    Alternatively, you can load via the `Edit > Load Petstore OAS 2.0` menu\n" +
                "    option!_\n" +
                "\n" +
                "\n" +
                "    Some useful links:\n" +
                "\n" +
                "    - [The Pet Store\n" +
                "    repository](https://github.com/swagger-api/swagger-petstore)\n" +
                "\n" +
                "    - [The source API definition for the Pet\n" +
                "    Store](https://github.com/swagger-api/swagger-petstore/blob/master/src/main/resources/openapi.yaml)\n" +
                "  termsOfService: http://swagger.io/terms/\n" +
                "  contact:\n" +
                "    email: apiteam@swagger.io\n" +
                "  license:\n" +
                "    name: Apache 2.0\n" +
                "    url: http://www.apache.org/licenses/LICENSE-2.0.html\n" +
                "  version: 1.0.11\n" +
                "externalDocs:\n" +
                "  description: Find out more about Swagger\n" +
                "  url: http://swagger.io\n" +
                "servers:\n" +
                "  - url: https://petstore3.swagger.io/api/v3\n" +
                "tags:\n" +
                "  - name: pet\n" +
                "    description: Everything about your Pets\n" +
                "    externalDocs:\n" +
                "      description: Find out more\n" +
                "      url: http://swagger.io\n" +
                "  - name: store\n" +
                "    description: Access to Petstore orders\n" +
                "    externalDocs:\n" +
                "      description: Find out more about our store\n" +
                "      url: http://swagger.io\n" +
                "  - name: user\n" +
                "    description: Operations about user\n" +
                "paths:\n" +
                "  /pet:\n" +
                "    put:\n" +
                "      tags:\n" +
                "        - pet\n" +
                "      summary: Update an existing pet\n" +
                "      description: Update an existing pet by Id\n" +
                "      operationId: updatePet\n" +
                "      requestBody:\n" +
                "        description: Update an existent pet in the store\n" +
                "        content:\n" +
                "          application/json:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Pet'\n" +
                "          application/xml:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Pet'\n" +
                "          application/x-www-form-urlencoded:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Pet'\n" +
                "        required: true\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: Successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Pet'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Pet'\n" +
                "        '400':\n" +
                "          description: Invalid ID supplied\n" +
                "        '404':\n" +
                "          description: Pet not found\n" +
                "        '405':\n" +
                "          description: Validation exception\n" +
                "      security:\n" +
                "        - petstore_auth:\n" +
                "            - write:pets\n" +
                "            - read:pets\n" +
                "    post:\n" +
                "      tags:\n" +
                "        - pet\n" +
                "      summary: Add a new pet to the store\n" +
                "      description: Add a new pet to the store\n" +
                "      operationId: addPet\n" +
                "      requestBody:\n" +
                "        description: Create a new pet in the store\n" +
                "        content:\n" +
                "          application/json:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Pet'\n" +
                "          application/xml:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Pet'\n" +
                "          application/x-www-form-urlencoded:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Pet'\n" +
                "        required: true\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: Successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Pet'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Pet'\n" +
                "        '405':\n" +
                "          description: Invalid input\n" +
                "      security:\n" +
                "        - petstore_auth:\n" +
                "            - write:pets\n" +
                "            - read:pets\n" +
                "  /pet/findByStatus:\n" +
                "    get:\n" +
                "      tags:\n" +
                "        - pet\n" +
                "      summary: Finds Pets by status\n" +
                "      description: Multiple status values can be provided with comma separated strings\n" +
                "      operationId: findPetsByStatus\n" +
                "      parameters:\n" +
                "        - name: status\n" +
                "          in: query\n" +
                "          description: Status values that need to be considered for filter\n" +
                "          required: false\n" +
                "          explode: true\n" +
                "          schema:\n" +
                "            type: string\n" +
                "            default: available\n" +
                "            enum:\n" +
                "              - available\n" +
                "              - pending\n" +
                "              - sold\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                type: array\n" +
                "                items:\n" +
                "                  $ref: '#/components/schemas/Pet'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                type: array\n" +
                "                items:\n" +
                "                  $ref: '#/components/schemas/Pet'\n" +
                "        '400':\n" +
                "          description: Invalid status value\n" +
                "      security:\n" +
                "        - petstore_auth:\n" +
                "            - write:pets\n" +
                "            - read:pets\n" +
                "  /pet/findByTags:\n" +
                "    get:\n" +
                "      tags:\n" +
                "        - pet\n" +
                "      summary: Finds Pets by tags\n" +
                "      description: >-\n" +
                "        Multiple tags can be provided with comma separated strings. Use tag1,\n" +
                "        tag2, tag3 for testing.\n" +
                "      operationId: findPetsByTags\n" +
                "      parameters:\n" +
                "        - name: tags\n" +
                "          in: query\n" +
                "          description: Tags to filter by\n" +
                "          required: false\n" +
                "          explode: true\n" +
                "          schema:\n" +
                "            type: array\n" +
                "            items:\n" +
                "              type: string\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                type: array\n" +
                "                items:\n" +
                "                  $ref: '#/components/schemas/Pet'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                type: array\n" +
                "                items:\n" +
                "                  $ref: '#/components/schemas/Pet'\n" +
                "        '400':\n" +
                "          description: Invalid tag value\n" +
                "      security:\n" +
                "        - petstore_auth:\n" +
                "            - write:pets\n" +
                "            - read:pets\n" +
                "  /pet/{petId}:\n" +
                "    get:\n" +
                "      tags:\n" +
                "        - pet\n" +
                "      summary: Find pet by ID\n" +
                "      description: Returns a single pet\n" +
                "      operationId: getPetById\n" +
                "      parameters:\n" +
                "        - name: petId\n" +
                "          in: path\n" +
                "          description: ID of pet to return\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: integer\n" +
                "            format: int64\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Pet'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Pet'\n" +
                "        '400':\n" +
                "          description: Invalid ID supplied\n" +
                "        '404':\n" +
                "          description: Pet not found\n" +
                "      security:\n" +
                "        - api_key: []\n" +
                "        - petstore_auth:\n" +
                "            - write:pets\n" +
                "            - read:pets\n" +
                "    post:\n" +
                "      tags:\n" +
                "        - pet\n" +
                "      summary: Updates a pet in the store with form data\n" +
                "      description: ''\n" +
                "      operationId: updatePetWithForm\n" +
                "      parameters:\n" +
                "        - name: petId\n" +
                "          in: path\n" +
                "          description: ID of pet that needs to be updated\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: integer\n" +
                "            format: int64\n" +
                "        - name: name\n" +
                "          in: query\n" +
                "          description: Name of pet that needs to be updated\n" +
                "          schema:\n" +
                "            type: string\n" +
                "        - name: status\n" +
                "          in: query\n" +
                "          description: Status of pet that needs to be updated\n" +
                "          schema:\n" +
                "            type: string\n" +
                "      responses:\n" +
                "        '405':\n" +
                "          description: Invalid input\n" +
                "      security:\n" +
                "        - petstore_auth:\n" +
                "            - write:pets\n" +
                "            - read:pets\n" +
                "    delete:\n" +
                "      tags:\n" +
                "        - pet\n" +
                "      summary: Deletes a pet\n" +
                "      description: delete a pet\n" +
                "      operationId: deletePet\n" +
                "      parameters:\n" +
                "        - name: api_key\n" +
                "          in: header\n" +
                "          description: ''\n" +
                "          required: false\n" +
                "          schema:\n" +
                "            type: string\n" +
                "        - name: petId\n" +
                "          in: path\n" +
                "          description: Pet id to delete\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: integer\n" +
                "            format: int64\n" +
                "      responses:\n" +
                "        '400':\n" +
                "          description: Invalid pet value\n" +
                "      security:\n" +
                "        - petstore_auth:\n" +
                "            - write:pets\n" +
                "            - read:pets\n" +
                "  /pet/{petId}/uploadImage:\n" +
                "    post:\n" +
                "      tags:\n" +
                "        - pet\n" +
                "      summary: uploads an image\n" +
                "      description: ''\n" +
                "      operationId: uploadFile\n" +
                "      parameters:\n" +
                "        - name: petId\n" +
                "          in: path\n" +
                "          description: ID of pet to update\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: integer\n" +
                "            format: int64\n" +
                "        - name: additionalMetadata\n" +
                "          in: query\n" +
                "          description: Additional Metadata\n" +
                "          required: false\n" +
                "          schema:\n" +
                "            type: string\n" +
                "      requestBody:\n" +
                "        content:\n" +
                "          application/octet-stream:\n" +
                "            schema:\n" +
                "              type: string\n" +
                "              format: binary\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/ApiResponse'\n" +
                "      security:\n" +
                "        - petstore_auth:\n" +
                "            - write:pets\n" +
                "            - read:pets\n" +
                "  /store/inventory:\n" +
                "    get:\n" +
                "      tags:\n" +
                "        - store\n" +
                "      summary: Returns pet inventories by status\n" +
                "      description: Returns a map of status codes to quantities\n" +
                "      operationId: getInventory\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                type: object\n" +
                "                additionalProperties:\n" +
                "                  type: integer\n" +
                "                  format: int32\n" +
                "      security:\n" +
                "        - api_key: []\n" +
                "  /store/order:\n" +
                "    post:\n" +
                "      tags:\n" +
                "        - store\n" +
                "      summary: Place an order for a pet\n" +
                "      description: Place a new order in the store\n" +
                "      operationId: placeOrder\n" +
                "      requestBody:\n" +
                "        content:\n" +
                "          application/json:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Order'\n" +
                "          application/xml:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Order'\n" +
                "          application/x-www-form-urlencoded:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/Order'\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Order'\n" +
                "        '405':\n" +
                "          description: Invalid input\n" +
                "  /store/order/{orderId}:\n" +
                "    get:\n" +
                "      tags:\n" +
                "        - store\n" +
                "      summary: Find purchase order by ID\n" +
                "      description: >-\n" +
                "        For valid response try integer IDs with value <= 5 or > 10. Other values\n" +
                "        will generate exceptions.\n" +
                "      operationId: getOrderById\n" +
                "      parameters:\n" +
                "        - name: orderId\n" +
                "          in: path\n" +
                "          description: ID of order that needs to be fetched\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: integer\n" +
                "            format: int64\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Order'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/Order'\n" +
                "        '400':\n" +
                "          description: Invalid ID supplied\n" +
                "        '404':\n" +
                "          description: Order not found\n" +
                "    delete:\n" +
                "      tags:\n" +
                "        - store\n" +
                "      summary: Delete purchase order by ID\n" +
                "      description: >-\n" +
                "        For valid response try integer IDs with value < 1000. Anything above\n" +
                "        1000 or nonintegers will generate API errors\n" +
                "      operationId: deleteOrder\n" +
                "      parameters:\n" +
                "        - name: orderId\n" +
                "          in: path\n" +
                "          description: ID of the order that needs to be deleted\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: integer\n" +
                "            format: int64\n" +
                "      responses:\n" +
                "        '400':\n" +
                "          description: Invalid ID supplied\n" +
                "        '404':\n" +
                "          description: Order not found\n" +
                "  /user:\n" +
                "    post:\n" +
                "      tags:\n" +
                "        - user\n" +
                "      summary: Create user\n" +
                "      description: This can only be done by the logged in user.\n" +
                "      operationId: createUser\n" +
                "      requestBody:\n" +
                "        description: Created user object\n" +
                "        content:\n" +
                "          application/json:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/User'\n" +
                "          application/xml:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/User'\n" +
                "          application/x-www-form-urlencoded:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/User'\n" +
                "      responses:\n" +
                "        default:\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/User'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/User'\n" +
                "  /user/createWithList:\n" +
                "    post:\n" +
                "      tags:\n" +
                "        - user\n" +
                "      summary: Creates list of users with given input array\n" +
                "      description: Creates list of users with given input array\n" +
                "      operationId: createUsersWithListInput\n" +
                "      requestBody:\n" +
                "        content:\n" +
                "          application/json:\n" +
                "            schema:\n" +
                "              type: array\n" +
                "              items:\n" +
                "                $ref: '#/components/schemas/User'\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: Successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/User'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/User'\n" +
                "        default:\n" +
                "          description: successful operation\n" +
                "  /user/login:\n" +
                "    get:\n" +
                "      tags:\n" +
                "        - user\n" +
                "      summary: Logs user into the system\n" +
                "      description: ''\n" +
                "      operationId: loginUser\n" +
                "      parameters:\n" +
                "        - name: username\n" +
                "          in: query\n" +
                "          description: The user name for login\n" +
                "          required: false\n" +
                "          schema:\n" +
                "            type: string\n" +
                "        - name: password\n" +
                "          in: query\n" +
                "          description: The password for login in clear text\n" +
                "          required: false\n" +
                "          schema:\n" +
                "            type: string\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          headers:\n" +
                "            X-Rate-Limit:\n" +
                "              description: calls per hour allowed by the user\n" +
                "              schema:\n" +
                "                type: integer\n" +
                "                format: int32\n" +
                "            X-Expires-After:\n" +
                "              description: date in UTC when token expires\n" +
                "              schema:\n" +
                "                type: string\n" +
                "                format: date-time\n" +
                "          content:\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                type: string\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                type: string\n" +
                "        '400':\n" +
                "          description: Invalid username/password supplied\n" +
                "  /user/logout:\n" +
                "    get:\n" +
                "      tags:\n" +
                "        - user\n" +
                "      summary: Logs out current logged in user session\n" +
                "      description: ''\n" +
                "      operationId: logoutUser\n" +
                "      parameters: []\n" +
                "      responses:\n" +
                "        default:\n" +
                "          description: successful operation\n" +
                "  /user/{username}:\n" +
                "    get:\n" +
                "      tags:\n" +
                "        - user\n" +
                "      summary: Get user by user name\n" +
                "      description: ''\n" +
                "      operationId: getUserByName\n" +
                "      parameters:\n" +
                "        - name: username\n" +
                "          in: path\n" +
                "          description: 'The name that needs to be fetched. Use user1 for testing. '\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: string\n" +
                "      responses:\n" +
                "        '200':\n" +
                "          description: successful operation\n" +
                "          content:\n" +
                "            application/json:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/User'\n" +
                "            application/xml:\n" +
                "              schema:\n" +
                "                $ref: '#/components/schemas/User'\n" +
                "        '400':\n" +
                "          description: Invalid username supplied\n" +
                "        '404':\n" +
                "          description: User not found\n" +
                "    put:\n" +
                "      tags:\n" +
                "        - user\n" +
                "      summary: Update user\n" +
                "      description: This can only be done by the logged in user.\n" +
                "      operationId: updateUser\n" +
                "      parameters:\n" +
                "        - name: username\n" +
                "          in: path\n" +
                "          description: name that need to be deleted\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: string\n" +
                "      requestBody:\n" +
                "        description: Update an existent user in the store\n" +
                "        content:\n" +
                "          application/json:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/User'\n" +
                "          application/xml:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/User'\n" +
                "          application/x-www-form-urlencoded:\n" +
                "            schema:\n" +
                "              $ref: '#/components/schemas/User'\n" +
                "      responses:\n" +
                "        default:\n" +
                "          description: successful operation\n" +
                "    delete:\n" +
                "      tags:\n" +
                "        - user\n" +
                "      summary: Delete user\n" +
                "      description: This can only be done by the logged in user.\n" +
                "      operationId: deleteUser\n" +
                "      parameters:\n" +
                "        - name: username\n" +
                "          in: path\n" +
                "          description: The name that needs to be deleted\n" +
                "          required: true\n" +
                "          schema:\n" +
                "            type: string\n" +
                "      responses:\n" +
                "        '400':\n" +
                "          description: Invalid username supplied\n" +
                "        '404':\n" +
                "          description: User not found\n" +
                "components:\n" +
                "  schemas:\n" +
                "    Order:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        id:\n" +
                "          type: integer\n" +
                "          format: int64\n" +
                "          example: 10\n" +
                "        petId:\n" +
                "          type: integer\n" +
                "          format: int64\n" +
                "          example: 198772\n" +
                "        quantity:\n" +
                "          type: integer\n" +
                "          format: int32\n" +
                "          example: 7\n" +
                "        shipDate:\n" +
                "          type: string\n" +
                "          format: date-time\n" +
                "        status:\n" +
                "          type: string\n" +
                "          description: Order Status\n" +
                "          example: approved\n" +
                "          enum:\n" +
                "            - placed\n" +
                "            - approved\n" +
                "            - delivered\n" +
                "        complete:\n" +
                "          type: boolean\n" +
                "      xml:\n" +
                "        name: order\n" +
                "    Customer:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        id:\n" +
                "          type: integer\n" +
                "          format: int64\n" +
                "          example: 100000\n" +
                "        username:\n" +
                "          type: string\n" +
                "          example: fehguy\n" +
                "        address:\n" +
                "          type: array\n" +
                "          xml:\n" +
                "            name: addresses\n" +
                "            wrapped: true\n" +
                "          items:\n" +
                "            $ref: '#/components/schemas/Address'\n" +
                "      xml:\n" +
                "        name: customer\n" +
                "    Address:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        street:\n" +
                "          type: string\n" +
                "          example: 437 Lytton\n" +
                "        city:\n" +
                "          type: string\n" +
                "          example: Palo Alto\n" +
                "        state:\n" +
                "          type: string\n" +
                "          example: CA\n" +
                "        zip:\n" +
                "          type: string\n" +
                "          example: '94301'\n" +
                "      xml:\n" +
                "        name: address\n" +
                "    Category:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        id:\n" +
                "          type: integer\n" +
                "          format: int64\n" +
                "          example: 1\n" +
                "        name:\n" +
                "          type: string\n" +
                "          example: Dogs\n" +
                "      xml:\n" +
                "        name: category\n" +
                "    User:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        id:\n" +
                "          type: integer\n" +
                "          format: int64\n" +
                "          example: 10\n" +
                "        username:\n" +
                "          type: string\n" +
                "          example: theUser\n" +
                "        firstName:\n" +
                "          type: string\n" +
                "          example: John\n" +
                "        lastName:\n" +
                "          type: string\n" +
                "          example: James\n" +
                "        email:\n" +
                "          type: string\n" +
                "          example: john@email.com\n" +
                "        password:\n" +
                "          type: string\n" +
                "          example: '12345'\n" +
                "        phone:\n" +
                "          type: string\n" +
                "          example: '12345'\n" +
                "        userStatus:\n" +
                "          type: integer\n" +
                "          description: User Status\n" +
                "          format: int32\n" +
                "          example: 1\n" +
                "      xml:\n" +
                "        name: user\n" +
                "    Tag:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        id:\n" +
                "          type: integer\n" +
                "          format: int64\n" +
                "        name:\n" +
                "          type: string\n" +
                "      xml:\n" +
                "        name: tag\n" +
                "    Pet:\n" +
                "      required:\n" +
                "        - name\n" +
                "        - photoUrls\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        id:\n" +
                "          type: integer\n" +
                "          format: int64\n" +
                "          example: 10\n" +
                "        name:\n" +
                "          type: string\n" +
                "          example: doggie\n" +
                "        category:\n" +
                "          $ref: '#/components/schemas/Category'\n" +
                "        photoUrls:\n" +
                "          type: array\n" +
                "          xml:\n" +
                "            wrapped: true\n" +
                "          items:\n" +
                "            type: string\n" +
                "            xml:\n" +
                "              name: photoUrl\n" +
                "        tags:\n" +
                "          type: array\n" +
                "          xml:\n" +
                "            wrapped: true\n" +
                "          items:\n" +
                "            $ref: '#/components/schemas/Tag'\n" +
                "        status:\n" +
                "          type: string\n" +
                "          description: pet status in the store\n" +
                "          enum:\n" +
                "            - available\n" +
                "            - pending\n" +
                "            - sold\n" +
                "      xml:\n" +
                "        name: pet\n" +
                "    ApiResponse:\n" +
                "      type: object\n" +
                "      properties:\n" +
                "        code:\n" +
                "          type: integer\n" +
                "          format: int32\n" +
                "        type:\n" +
                "          type: string\n" +
                "        message:\n" +
                "          type: string\n" +
                "      xml:\n" +
                "        name: '##default'\n" +
                "  requestBodies:\n" +
                "    Pet:\n" +
                "      description: Pet object that needs to be added to the store\n" +
                "      content:\n" +
                "        application/json:\n" +
                "          schema:\n" +
                "            $ref: '#/components/schemas/Pet'\n" +
                "        application/xml:\n" +
                "          schema:\n" +
                "            $ref: '#/components/schemas/Pet'\n" +
                "    UserArray:\n" +
                "      description: List of user object\n" +
                "      content:\n" +
                "        application/json:\n" +
                "          schema:\n" +
                "            type: array\n" +
                "            items:\n" +
                "              $ref: '#/components/schemas/User'\n" +
                "  securitySchemes:\n" +
                "    petstore_auth:\n" +
                "      type: oauth2\n" +
                "      flows:\n" +
                "        implicit:\n" +
                "          authorizationUrl: https://petstore3.swagger.io/oauth/authorize\n" +
                "          scopes:\n" +
                "            write:pets: modify pets in your account\n" +
                "            read:pets: read your pets\n" +
                "    api_key:\n" +
                "      type: apiKey\n" +
                "      name: api_key\n" +
                "      in: header\n");

        // manipolo il server generato per aggiungere la gestione della persistenza
        this.persistenceService.managePersistence();

        return "Hello from your deamon";
    }

    @PostMapping("/buildSpringServer")
    public ResponseEntity<?> buildSpringServer(@RequestBody String yamlSpecString, boolean persistence) {
        ServerBuildResponse response = new ServerBuildResponse();

        // controllo la validità del file .yaml
        if (validateOpenAPI(yamlSpecString).getStatusCode().equals(HttpStatusCode.valueOf(200))) {

            // genero il progetto nella directory di default
            this.serverBuildingService.buildBasicServerFromSwagger(yamlSpecString);

            if (persistence) {

                byte[] databaseDockerImg = new byte[0];

                // manipolo il server generato per aggiungere la gestione della persistenza
                this.persistenceService.managePersistence();

                // recupero la lista di nomi di operazioni non implementate
                List<String> notImplMethods = this.persistenceService.getNotImplementedMethods();

                response.setNotImplMethods(notImplMethods);


                // genero il db nel container

                // popolo il db

                // TODO restituisco l'immagine docker "db"

                // restituisco una lista di stringhe

            }

            // Restituisco:
            //      - file "deception_deamon.zip"
            //      - istruzioni sotto forma di stringa
            //      - immagine docker del server

            byte[] zipFileContent = new byte[0];
            byte[] serverDockerImg = new byte[0];
            String instructionsContent = "";
            try {
                zipFileContent = this.serverBuildingService.getZip();
                instructionsContent = FileUtils.readFileContent(instructionTxtPath);
                serverDockerImg = this.dockerService.buildServerDockerImg();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            response.setServerZipFile(zipFileContent);
            response.setInstructions(instructionsContent);
            response.setServerDockerImg(serverDockerImg);


            // TODO restituisco l'immagine docker "deamon"


            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=deception_deamon.zip")
                    .body(response);

        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore durante la validazione OpenAPI");
        }
    }


    @PostMapping("/buildDockerImagine")
    public ResponseEntity<String> buildDockerImage(@RequestParam("zipFile") MultipartFile zipFile) {

        return null;
    }


    @PostMapping("/validateOpenAPISpec")
    public ResponseEntity<String> validateOpenAPI(@RequestBody String yamlString) {
        try {
            SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(yamlString);

            if (parseResult.getMessages() == null || parseResult.getMessages().isEmpty()) {
                return ResponseEntity.ok("La specifica OpenAPI è valida.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Errore nella specifica OpenAPI:\n" + parseResult.getMessages());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore durante la validazione OpenAPI: " + e.getMessage());
        }
    }


}
