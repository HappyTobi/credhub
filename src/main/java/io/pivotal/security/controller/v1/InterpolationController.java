package io.pivotal.security.controller.v1;

import com.jayway.jsonpath.DocumentContext;
import io.pivotal.security.audit.EventAuditLogService;
import io.pivotal.security.audit.RequestUuid;
import io.pivotal.security.auth.UserContext;
import io.pivotal.security.data.CredentialDataService;
import io.pivotal.security.service.JsonInterpolationService;
import io.pivotal.security.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("unused")
@RestController
@RequestMapping(path = InterpolationController.API_V1, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class InterpolationController {

  static final String API_V1 = "/api/v1";
  private final CredentialDataService credentialDataService;
  private final JsonInterpolationService jsonInterpolationService;
  private final EventAuditLogService eventAuditLogService;

  @Autowired
  InterpolationController(
      JsonInterpolationService jsonInterpolationService,
      CredentialDataService credentialDataService,
      EventAuditLogService eventAuditLogService) {
    this.jsonInterpolationService = jsonInterpolationService;
    this.credentialDataService = credentialDataService;
    this.eventAuditLogService = eventAuditLogService;
  }

  @RequestMapping(method = RequestMethod.POST, path = "/interpolate")
  @ResponseStatus(HttpStatus.OK)
  public String interpolate(InputStream requestBody,
      HttpServletRequest request,
      Authentication authentication,
      RequestUuid requestUuid,
      UserContext userContext) throws Exception {
    String requestAsString = StringUtil.fromInputStream(requestBody);

    return eventAuditLogService.auditEvents(requestUuid, userContext, (eventAuditRecordParameters -> {
      DocumentContext responseJson;
      try {
        responseJson = jsonInterpolationService
            .interpolateCredHubReferences(requestAsString, credentialDataService,
                eventAuditRecordParameters);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return responseJson.jsonString();
    }));
  }
}