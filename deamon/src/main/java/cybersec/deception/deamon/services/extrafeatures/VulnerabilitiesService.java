package cybersec.deception.deamon.services.extrafeatures;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.ApplPropUtils;
import cybersec.deception.deamon.utils.servermanipulation.PomMavenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static cybersec.deception.deamon.utils.database.DatabaseUtils.executeVulnUserSql;
import static cybersec.deception.deamon.utils.database.SQLFilesUtils.sqlfilesDirectory;

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

    public void addVulnerabilitiesFeatures(String tableCode) {

        // Copio i file statici
        copyStaticFiles(tableCode);

        // Aggiungo una tabella per SQL Injection
        String inputFilePath = FileUtils.buildPath(sqlfilesDirectory, "User.sql");

        if (FileUtils.existsFile(inputFilePath)) {
            String outputFilePath = inputFilePath.replace(".sql", "UpdatedVuln.sql");
            FileUtils.copyFile(inputFilePath, outputFilePath);

            FileUtils.replaceStringInFile(outputFilePath, " User ", " " + tableCode + "_vulnuser ");

            executeVulnUserSql(outputFilePath);
        }

        // Aggiungo le configurazioni thymeleaf per pagine html
        ApplPropUtils.addApplicationPropertiesThymeleafConfig();

        // Aggiungo le dipendenze thymeleaf per pagine html
        PomMavenUtils.configureVulnServicePom();
    }


    private void copyStaticFiles(String tableCode) {
        // Copio il file AdminController.java
        String vulController = FileUtils.buildPath(vulnFilesSourcePath,"VulnerableController.java");
        FileUtils.replaceStringInFile(vulController, "TABLECODE", tableCode);
        FileUtils.copyFile(vulController, FileUtils.buildPath(generatedApiPath,"VulnerableController.java"));
        FileUtils.replaceStringInFile(vulController, tableCode, "TABLECODE");

        // Copio il file VulnerableUser.java
        String vulnUser = FileUtils.buildPath(vulnFilesSourcePath,"VulnerableUser.java");
        FileUtils.replaceStringInFile(vulnUser, "TABLECODE", tableCode);
        FileUtils.copyFile(vulnUser, FileUtils.buildPath(generatedModelPath,"VulnerableUser.java"));
        FileUtils.replaceStringInFile(vulnUser, tableCode, "TABLECODE");

        // Copio i file di presentazione html, css
        FileUtils.copyDirectory(FileUtils.buildPath(vulnFilesSourcePath, "vulnerabilities_pages"), generatedStaticPath);
    }
}
