package io.pivotal.security.entity;

import io.pivotal.security.view.SecretKind;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static io.pivotal.security.entity.NamedCertificateSecret.NEW_LINE;

@Entity
@Table(name = "RsaSecret")
@DiscriminatorValue("rsa")
public class NamedRsaSecret extends NamedRsaSshSecret {
  static private final String RSA_START = "-----BEGIN PUBLIC KEY-----\n";
  static private final String RSA_END = "\n-----END PUBLIC KEY-----";

  public NamedRsaSecret() {
    this(null);
  }

  public NamedRsaSecret(String name) {
    super(name);
  }

  public SecretKind getKind() {
    return SecretKind.RSA;
  }

  public int getKeyLength() {
    String publicKey = this.getPublicKey();

    if (StringUtils.isEmpty(publicKey)) {
      return 0;
    }

    try {
      String key = publicKey
            .replaceFirst(RSA_START, "")
            .replaceFirst(RSA_END, "")
            .replaceAll(NEW_LINE, "");
      byte[] byteKey = Base64.decodeBase64(key.getBytes());
      X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return ((RSAPublicKey) kf.generatePublic(X509publicKey)).getModulus().bitLength();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }
}