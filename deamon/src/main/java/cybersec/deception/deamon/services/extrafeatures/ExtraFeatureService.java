package cybersec.deception.deamon.services.extrafeatures;

import cybersec.deception.deamon.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExtraFeatureService {

    private final AdminPagesService adminPagesService;

    private final FilterService filterService;

    @Autowired
    public ExtraFeatureService(AdminPagesService adminPagesService, FilterService filterService) {
        this.adminPagesService = adminPagesService;
        this.filterService = filterService;
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
}
