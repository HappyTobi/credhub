package io.pivotal.security.mapper;

import com.jayway.jsonpath.DocumentContext;
import io.pivotal.security.controller.v1.StringSecretParameters;
import io.pivotal.security.entity.NamedPasswordSecret;
import io.pivotal.security.generator.SecretGenerator;
import io.pivotal.security.view.StringSecret;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.pivotal.security.view.ParameterizedValidationException;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;

@Component
public class PasswordGeneratorRequestTranslator implements RequestTranslator<NamedPasswordSecret>, SecretGeneratorRequestTranslator<StringSecretParameters> {

  @Autowired
  SecretGenerator<StringSecretParameters, StringSecret> stringSecretGenerator;

  @Override
  public StringSecretParameters validRequestParameters(DocumentContext parsed) {
    StringSecretParameters secretParameters = new StringSecretParameters();
    secretParameters.setType(parsed.read("$.type", String.class));
    Optional.ofNullable(parsed.read("$.parameters.length", Integer.class))
        .ifPresent(secretParameters::setLength);
    Optional.ofNullable(parsed.read("$.parameters.exclude_lower", Boolean.class))
        .ifPresent(secretParameters::setExcludeLower);
    Optional.ofNullable(parsed.read("$.parameters.exclude_upper", Boolean.class))
        .ifPresent(secretParameters::setExcludeUpper);
    Optional.ofNullable(parsed.read("$.parameters.exclude_number", Boolean.class))
        .ifPresent(secretParameters::setExcludeNumber);
    Optional.ofNullable(parsed.read("$.parameters.exclude_special", Boolean.class))
        .ifPresent(secretParameters::setExcludeSpecial);

    if (!secretParameters.isValid()) {
      throw new ParameterizedValidationException("error.excludes_all_charsets");
    }
    return secretParameters;
  }

  @Override
  public void populateEntityFromJson(NamedPasswordSecret entity, DocumentContext documentContext) {
    StringSecretParameters requestParameters = validRequestParameters(documentContext);
    StringSecret secret = stringSecretGenerator.generateSecret(requestParameters);
    entity.setValue(secret.getValue());
  }

  @Override
  public Set<String> getValidKeys() {
    return of("$['type']",
        "$['overwrite']",
        "$['parameters']",
        "$['parameters']['length']",
        "$['parameters']['exclude_lower']",
        "$['parameters']['exclude_upper']",
        "$['parameters']['exclude_number']",
        "$['parameters']['exclude_special']");
  }
}