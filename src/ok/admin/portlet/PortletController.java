package ok.admin.portlet;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ok.admin.rest.annotation.JsonRequestMapping;
import ok.admin.rest.annotation.RequirePermissions;
import ok.admin.rest.component.portlet.druid.DruidDataLoader;
import ok.admin.rest.component.portlet.exception.ConfigurationNotFoundException;
import ok.admin.rest.component.portlet.model.PlatformType;
import ok.admin.rest.component.portlet.model.PortletConfigRequest;
import ok.admin.rest.response.BaseResponse;
import ok.admin.rest.response.Result;
import ok.admin.rest.response.v1.CustomSuccessResponse;
import ok.admin.rest.response.v1.ErrorResponse;
import ok.admin.utils.MapBuilder;
import one.comp.admin.AdminED;
import one.util.StringUtil;

@RestController
@RequirePermissions({AdminED.Option.FEED_BANNER_PORTLET_ADMIN})
@RequestMapping(value = "/api/portlet")
public class PortletController {
    private final PortletService portletService;
    private final DruidDataLoader dataLoader;

    @Autowired
    public PortletController(PortletService portletService, DruidDataLoader dataLoader) {
        this.portletService = portletService;
        this.dataLoader = dataLoader;
    }

    @JsonRequestMapping("/resolver/configs/{user-id}")
    public BaseResponse getResolverConfigsByUser(@PathVariable("user-id") Long userId) {
        dataLoader.makeQuery();
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

    @JsonRequestMapping("/getInserterConfig")
    public BaseResponse getInserterConfig(@RequestParam String host,
                                          @RequestParam String configName,
                                          @RequestParam PlatformType platformType) {
        if (StringUtil.isEmpty(host) || StringUtil.isEmpty(configName) || platformType == null) {
            return ErrorResponse.of("Empty parameters", "incorrect-parameters");
        }
        try {
            return CustomSuccessResponse.of()
                    .put("inserterConfig", portletService.getInserterConfig(host, configName, platformType));
        } catch (Exception e) {
            return ErrorResponse.of(e.getMessage());
        }
    }

    @JsonRequestMapping(value = "/createInserterConfig",  method = RequestMethod.POST)
    public BaseResponse createInserterConfig(@RequestParam String host,
                                             @RequestParam String configName,
                                             @RequestBody PortletConfigRequest portletConfigRequest) {
        if (StringUtil.isEmpty(host) || StringUtil.isEmpty(configName) || portletConfigRequest == null) {
            return ErrorResponse.of("Empty parameters", "incorrect-parameters");
        }
        return CustomSuccessResponse.of()
                .put("generatedConfig", portletService.generateFeedPortletConfig(host, configName, portletConfigRequest));
    }

    @JsonRequestMapping(value = "/enableInserterConfig",  method = RequestMethod.POST)
    public Map<String, Object> enableInserterConfig(@RequestParam long userId,
                                                    @RequestParam String host,
                                                    @RequestParam String configName) {
        if (StringUtil.isEmpty(host) || StringUtil.isEmpty(configName)) {
            return errorMapResponse("incorrect parameters");
        }

        try {
            portletService.enableInserterConfig(userId, host, configName);
        } catch (ConfigurationNotFoundException e) {
            return errorMapResponse(e.getMessage());
        }
        return new MapBuilder<String, Object>()
                .put("result", Result.Ok)
                .toMap();
    }

    @JsonRequestMapping(value = "/disableInserterConfig",  method = RequestMethod.POST)
    public Map<String, Object> disableInserterConfig(@RequestParam long userId,
                                                    @RequestParam String host,
                                                    @RequestParam String configName) {
        if (StringUtil.isEmpty(host) || StringUtil.isEmpty(configName)) {
            return errorMapResponse("incorrect parameters");
        }

        try {
            portletService.disableInserterConfig(userId, host, configName);
        } catch (ConfigurationNotFoundException e) {
            return errorMapResponse(e.getMessage());
        }
        return new MapBuilder<String, Object>()
                .put("result", Result.Ok)
                .toMap();
    }

    private static Map<String, Object> errorMapResponse(String errorMessage) {
        return new MapBuilder<String, Object>()
                .put("result", Result.Fail)
                .put("error", errorMessage)
                .toMap();
    }
}