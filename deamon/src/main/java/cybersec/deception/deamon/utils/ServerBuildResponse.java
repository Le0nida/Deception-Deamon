package cybersec.deception.deamon.utils;

import java.util.List;
import java.util.Map;

public class ServerBuildResponse {

    private String serverDockerFile;
    private byte[] serverZipFile;
    private Map<String, List<String>> notImplMethods;
    private String instructions;

    public ServerBuildResponse(String serverDockerFile, byte[] serverZipFile, Map<String, List<String>> notImplMethods, String instructions) {
        this.serverDockerFile = serverDockerFile;
        this.serverZipFile = serverZipFile;
        this.notImplMethods = notImplMethods;
        this.instructions = instructions;
    }

    public ServerBuildResponse() {
    }

    public String getServerDockerFile() {
        return serverDockerFile;
    }

    public void setServerDockerFile(String serverDockerFile) {
        this.serverDockerFile = serverDockerFile;
    }

    public byte[] getServerZipFile() {
        return serverZipFile;
    }

    public void setServerZipFile(byte[] serverZipFile) {
        this.serverZipFile = serverZipFile;
    }

    public Map<String, List<String>> getNotImplMethods() {
        return notImplMethods;
    }

    public void setNotImplMethods(Map<String, List<String>> notImplMethods) {
        this.notImplMethods = notImplMethods;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
