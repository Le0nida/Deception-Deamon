package cybersec.deception.deamon.services.extrafeatures;

import cybersec.deception.deamon.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtraFeatureService {

    private final AdminPagesService adminPagesService;
    private final FilterService filterService;
    private final SessionService sessionService;
    private final VulnerabilitiesService vulnerabilitiesService;

    @Autowired
    public ExtraFeatureService(AdminPagesService adminPagesService, FilterService filterService, SessionService sessionService, VulnerabilitiesService vulnerabilitiesService) {
        this.adminPagesService = adminPagesService;
        this.filterService = filterService;
        this.sessionService = sessionService;
        this.vulnerabilitiesService = vulnerabilitiesService;
    }

    public void addAdminPages(String adminUsername, String adminPass) {
        if (!Utils.isNullOrEmpty(adminUsername) && !Utils.isNullOrEmpty(adminPass)) {
            adminPagesService.addAdminFeatures(adminUsername,adminPass);
        }
    }

    public void addNotAuthorizedFilter(String patterns) {
        if (!Utils.isNullOrEmpty(patterns)) {
            filterService.addFilterFeatures(patterns);
        }
    }

    public void addSessionFilter(boolean sessionBool) {
        if (sessionBool) {
            sessionService.addSessionFeatures();
        }
    }

    public void addVulnerableEndpoints(boolean vulnBool, boolean persistence) {
        if (vulnBool & persistence) {
            vulnerabilitiesService.addVulnerabilitiesFeatures();
        }
    }
}
