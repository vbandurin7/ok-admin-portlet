package ok.admin.portlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ok.admin.rest.annotation.JsonRequestMapping;
import ok.admin.rest.annotation.RequirePermissions;
import ok.admin.rest.component.portlet.model.PlatformType;
import ok.admin.rest.component.portlet.model.PortletConfigRequest;
import ok.admin.rest.response.BaseResponse;
import ok.admin.rest.response.v1.CustomSuccessResponse;
import ok.admin.rest.response.v1.ErrorResponse;
import one.comp.admin.AdminED;

@RestController
@RequirePermissions({AdminED.Option.FEED_BANNER_PORTLET_ADMIN})
@RequestMapping(value = "/api/portlet")
public class PortletController {
    private final PortletService portletService;

    @Autowired
    public PortletController(PortletService portletService) {
        this.portletService = portletService;
    }

    @JsonRequestMapping("/resolver/configs/{user-id}")
    public BaseResponse getResolverConfigsByUser(@PathVariable("user-id") Long userId) {
        if (userId == null) {
            return ErrorResponse.of("Null user ID", "incorrect-id");
        }
        try {
            return CustomSuccessResponse.of()
                    .put("resolverConfigs", portletService.getResolverConfigsByUser(userId));
        } catch (Exception e) {
            return ErrorResponse.of(e.getMessage());
        }

    }

    @JsonRequestMapping("/inserter/config")
    public BaseResponse getInserterConfig(@RequestParam String host,
                                          @RequestParam String configName,
                                          @RequestParam PlatformType platformType) {
        try {
            return CustomSuccessResponse.of()
                    .put("inserterConfig", portletService.getFeedConfig(host, configName, platformType));
        } catch (Exception e) {
            return ErrorResponse.of(e.getMessage());
        }
    }

    @JsonRequestMapping(value = "/create/inserter/config")
    public BaseResponse createInserterConfig(@RequestParam String host,
                                             @RequestParam String configName,
                                             @RequestBody PortletConfigRequest portletConfigRequest) {
        return CustomSuccessResponse.of().put("generatedConfig", PortletConfigGenerator.generateConfig(portletConfigRequest.getPortlets()));
    }
}