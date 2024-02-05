package cybersec.deception.deamon.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DockerHubBuildService {

    private static final String DOCKER_HUB_API_URL = "https://cloud.docker.com/api/build/v1/source/{sourceId}/trigger/{triggerId}/";

    private final RestTemplate restTemplate;

    public DockerHubBuildService() {
        this.restTemplate = new RestTemplate();
    }

    public void triggerDockerHubBuild(String sourceId, String triggerId, String dockerHubToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(dockerHubToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                DOCKER_HUB_API_URL, HttpMethod.POST, requestEntity, String.class,
                sourceId, triggerId
        );

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            System.out.println("Build avviata con successo su Docker Hub");
        } else {
            System.err.println("Errore durante l'avvio della build su Docker Hub");
        }
    }

    public byte[] buildServerDockerImg() {
        return new byte[0];
    }
}
