package io.pivotal.security.controller.v1;

import com.greghaskins.spectrum.Spectrum;
import io.pivotal.security.CredentialManagerApp;
import io.pivotal.security.CredentialManagerTestContextBootstrapper;
import io.pivotal.security.view.ParameterizedValidationException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;

import static com.greghaskins.spectrum.Spectrum.*;
import static io.pivotal.security.helper.SpectrumHelper.itThrowsWithMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(Spectrum.class)
@SpringApplicationConfiguration(classes = CredentialManagerApp.class)
@BootstrapWith(CredentialManagerTestContextBootstrapper.class)
@ActiveProfiles("unit-test")
public class RsaSecretParametersTest {
  private RsaSecretParameters subject;

  {
    beforeEach(() -> {
      subject = new RsaSecretParameters();
    });

    it("should default to a reasonable key length", () -> {
      assertThat(subject.getKeyLength(), equalTo(2048));
    });

    describe("validate", () -> {
      it("should accept correct key lengths", () -> {
        subject.setKeyLength(2048);
        subject.validate();

        subject.setKeyLength(3072);
        subject.validate();

        subject.setKeyLength(4096);
        subject.validate();
        //pass
      });

      itThrowsWithMessage("should throw if given an invalid length", ParameterizedValidationException.class, "error.invalid_key_length", () -> {
        subject.setKeyLength(1024);
        subject.validate();
      });
    });
  }
}