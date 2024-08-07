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
    private final JWTAuthenticationService jwtAuthenticationService;

    @Autowired
    public ExtraFeatureService(AdminPagesService adminPagesService, FilterService filterService, SessionService sessionService, VulnerabilitiesService vulnerabilitiesService, JWTAuthenticationService jwtAuthenticationService) {
        this.adminPagesService = adminPagesService;
        this.filterService = filterService;
        this.sessionService = sessionService;
        this.vulnerabilitiesService = vulnerabilitiesService;
        this.jwtAuthenticationService = jwtAuthenticationService;
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

    public void addVulnerableEndpoints(boolean vulnBool, boolean persistence, String tableCode) {
        if (vulnBool & persistence) {
            vulnerabilitiesService.addVulnerabilitiesFeatures(tableCode);
        }
    }

    public void addJWTAuthentication(String jwtUser, String jwtPass, String jwtPaths) {
        if (!Utils.isNullOrEmpty(jwtUser) && !Utils.isNullOrEmpty(jwtPass) && !Utils.isNullOrEmpty(jwtPaths)) {
            jwtAuthenticationService.addJWTAuthenticationFeatures(jwtUser, jwtPass, jwtPaths);
        }
    }
}
