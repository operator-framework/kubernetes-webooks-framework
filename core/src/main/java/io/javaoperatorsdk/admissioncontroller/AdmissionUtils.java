package io.javaoperatorsdk.admissioncontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.admission.v1.AdmissionRequest;
import io.fabric8.kubernetes.api.model.admission.v1.AdmissionResponse;
import io.fabric8.zjsonpatch.JsonDiff;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AdmissionUtils {

  public static final String JSON_PATCH = "JSONPatch";
  private final static ObjectMapper mapper = new ObjectMapper();

  public static AdmissionResponse notAllowedExceptionToAdmissionResponse(
      NotAllowedException notAllowedException) {
    AdmissionResponse admissionResponse = new AdmissionResponse();
    admissionResponse.setAllowed(false);
    Status status = new Status();
    status.setCode(notAllowedException.getCode());
    status.setMessage(notAllowedException.getMessage());
    admissionResponse.setStatus(status);
    return admissionResponse;
  }

  public static KubernetesResource getTargetResource(AdmissionRequest admissionRequest,
      Operation operation) {
    return operation == Operation.DELETE ? admissionRequest.getOldObject()
        : admissionRequest.getObject();
  }

  public static AdmissionResponse admissionResponseFromMutation(KubernetesResource originalResource,
                                                                KubernetesResource mutatedResource) {
    AdmissionResponse admissionResponse = new AdmissionResponse();
    admissionResponse.setAllowed(true);
    admissionResponse.setPatchType(JSON_PATCH);
    var originalResNode = mapper.valueToTree(originalResource);
    var mutatedResNode = mapper.valueToTree(mutatedResource);

    var diff = JsonDiff.asJson(originalResNode, mutatedResNode);
    String base64Diff = Base64.getEncoder().encodeToString(diff.toString().getBytes(StandardCharsets.UTF_8));
    admissionResponse.setPatch(base64Diff);
    return admissionResponse;
  }

}
