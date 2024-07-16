package cybersec.deception.deamon.services.extrafeatures;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.ApplPropUtils;
import cybersec.deception.deamon.utils.servermanipulation.PomMavenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VulnerabilitiesService {

    @Value("${folder.api}")
    private String generatedApiPath;

    @Value("${folder.model}")
    private String generatedModelPath;

    @Value("${folder.static}")
    private String generatedStaticPath;

    @Value("${vulnFiles.source.path}")
    private String vulnFilesSourcePath;

    public void addVulnerabilitiesFeatures() {

        // Copio i file statici
        copyStaticFiles();

        // Aggiungo una tabella per SQL Injection
        // TODO implementare nuova tabella per vulnuser
    }


    private void copyStaticFiles() {
        // Copio il file AdminController.java
        FileUtils.copyFile(FileUtils.buildPath(vulnFilesSourcePath,"VulnerableController.java"), FileUtils.buildPath(generatedApiPath,"VulnerableController.java"));

        // Copio il file VulnerableUser.java
        FileUtils.copyFile(FileUtils.buildPath(vulnFilesSourcePath,"VulnerableUser.java"), FileUtils.buildPath(generatedModelPath,"VulnerableUser.java"));

        // Copio i file di presentazione html, css
        FileUtils.copyDirectory(FileUtils.buildPath(vulnFilesSourcePath, "vulnerabilities_pages"), FileUtils.buildPath(generatedStaticPath, "vulnerabilities"));
    }
}
