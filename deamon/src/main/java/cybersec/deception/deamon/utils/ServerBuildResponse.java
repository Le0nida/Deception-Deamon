package cybersec.deception.deamon.utils;

import java.util.List;
import java.util.Map;

public class ServerBuildResponse {

    private byte[] serverDockerImg;
    private byte[] serverZipFile;
    private Map<String, List<String>> notImplMethods;
    private String instructions;

    public ServerBuildResponse(byte[] serverDockerImg, byte[] serverZipFile, Map<String, List<String>> notImplMethods, String instructions) {
        this.serverDockerImg = serverDockerImg;
        this.serverZipFile = serverZipFile;
        this.notImplMethods = notImplMethods;
        this.instructions = instructions;
    }

    public ServerBuildResponse() {
    }

    public byte[] getServerDockerImg() {
        return serverDockerImg;
    }

    public void setServerDockerImg(byte[] serverDockerImg) {
        this.serverDockerImg = serverDockerImg;
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
