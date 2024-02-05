package cybersec.deception.deamon.utils;

import java.util.List;

public class ServerBuildResponse {

    private byte[] databaseDockerImg;
    private byte[] serverDockerImg;
    private byte[] serverZipFile;
    private List<String> notImplMethods;
    private String instructions;

    public ServerBuildResponse(byte[] databaseDockerImg, byte[] serverDockerImg, byte[] serverZipFile, List<String> notImplMethods, String instructions) {
        this.databaseDockerImg = databaseDockerImg;
        this.serverDockerImg = serverDockerImg;
        this.serverZipFile = serverZipFile;
        this.notImplMethods = notImplMethods;
        this.instructions = instructions;
    }

    public ServerBuildResponse() {
    }

    public byte[] getDatabaseDockerImg() {
        return databaseDockerImg;
    }

    public void setDatabaseDockerImg(byte[] databaseDockerImg) {
        this.databaseDockerImg = databaseDockerImg;
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

    public List<String> getNotImplMethods() {
        return notImplMethods;
    }

    public void setNotImplMethods(List<String> notImplMethods) {
        this.notImplMethods = notImplMethods;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
