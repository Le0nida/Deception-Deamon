package cybersec.deception.deamon.services.extrafeatures;

import cybersec.deception.deamon.utils.FileUtils;
import cybersec.deception.deamon.utils.servermanipulation.ApplPropUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FilterService {

    @Value("${folder.api}")
    private String generatedApiPath;

    @Value("${folder.static}")
    private String generatedStaticPath;

    @Value("${folder.config}")
    private String generatedConfigPath;

    @Value("${filterFiles.source.path}")
    private String filterFilesSourcePath;

    public void addFilterFeatures(String patterns){

        // Copio i file statici
        copyStaticFiles();

        // Aggiungo i pattern in application.properties
        ApplPropUtils.addApplicationPropertiesFilteredPatternsConfig(patterns);
    }

    private void copyStaticFiles() {
        // Copio i file di configurazione per il filtro
        FileUtils.copyDirectory(FileUtils.buildPath(filterFilesSourcePath, "config"), FileUtils.buildPath(generatedConfigPath, "filter"));

        // Copio il controller per la visualizzazione della pagina di errore
        FileUtils.copyFile(FileUtils.buildPath(filterFilesSourcePath, "ErrorController.java"), FileUtils.buildPath(generatedApiPath, "ErrorController.java"));

        // Copio la pagina html da visualizzare
        FileUtils.copyFile(FileUtils.buildPath(filterFilesSourcePath, "not_authorized.html"), FileUtils.buildPath(generatedStaticPath, "not_authorized.html"));
    }

}
