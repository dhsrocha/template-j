package template.feature.sec.token;

import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import template.base.contract.Controller;
import template.base.contract.Router;
import template.base.stereotype.Domain;

/**
 * Value object which represents.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Token implements Domain<Token> {

  // private static final Duration EXPIRATION = Duration.ofMinutes(5);
  private static final String RSA = "RSA";
  String compact;

  @Override
  public int compareTo(final @lombok.NonNull Token t) {
    return Comparator.comparing(Token::getCompact).compare(this, t);
  }

  @Override
  public Set<Invariant<Token>> invariants() {
    return Collections.emptySet();
  }

  static Token generate(final @NonNull UUID id) {
    val compact = Jwts.builder().setAudience(
        id.toString()).signWith(keyPair().getPublic()).compact();
    return new Token(compact);
  }

  @SneakyThrows
  private static KeyPair keyPair() {
    val seed = KeyPairGenerator.getInstance(RSA);
    val spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
    seed.initialize(spec);
    return seed.generateKeyPair();
  }

//  @AllArgsConstructor
//  private enum Key {
//    EXPIRATION("role"),
//    ;
//    String val;
//  }

//  static Token parse(final @lombok.NonNull String key) {
//    return Exceptions.ILLEGAL_ARGUMENT.trapIn(() -> {
//      val jwt = Jwts.parserBuilder().setSigningKey(key).build()
//                    .parseClaimsJws(compact);
//      return new Token(jwt.getBody());
//    });
//  }
//
//  String get(final Key key) {
//    return claims.get(key.name(), String.class);
//  }
//  static Token fromUser(final @lombok.NonNull User user) {
//    val claims = Jwts.claims()
//                     // .setSubject(user.getId())
//                     // .setAudience("")
//                     // .setIssuer("")
//                     .setExpiration(
//                         Date.from(Instant.now().plus(TOKEN_EXPIRATION)))
//                     .setIssuedAt(Date.from(Instant.now()))
//                     .setNotBefore(Date.from(Instant.now()));
//    claims.put(Props.NAME.name(), user.getName());
//    return new Token(claims);
//  }
//
//  String compactFrom(final @lombok.NonNull Map.Entry<UUID, User> u) {
//    val now = Instant.now();
//    return Jwts.builder()
//               .claim("name", u.getValue().getName())
//               .setSubject(Application.class.getPackageName())
//               .setId(u.getKey().toString())
//               .setIssuedAt(Date.from(now))
//               .setExpiration(Date.from(now.plus(5L, ChronoUnit.MINUTES)))
//               .signWith(key)
//               .compact();
//  }
//
//  User toUser() {
//    return User.of(
//        Boolean.parseBoolean(Props.ACTIVE.fromClaim(claims)),
//        Arrays.stream(COLLECTION_REGEX.split(Props.ROLE.fromClaim(claims)))
//              .map(Auth.Role::valueOf)
//              .collect(Collectors.toSet()),
//        Props.ID.fromClaim(claims),
//        Props.NAME.fromClaim(claims),
//        Props.EMAIL.fromClaim(claims),
//        Props.PASSWORD.fromClaim(claims));
//  }
//
//  final boolean isExpired() {
//    return claims.getExpiration().toInstant().isBefore(Instant.now());
//  }
//
//  final Token refresh() {
//    return new Token(claims.setExpiration(Date.from(
//        Instant.now().plus(TOKEN_EXPIRATION))));
//  }
//
//  private static Key salt(final @lombok.NonNull String key) {
//    return new SecretKeySpec(Base64.getDecoder().decode(key),
//                             SignatureAlgorithm.HS256.getJcaName());
//  }

  /**
   * Type for binding package-private implementations to public interfaces.
   * It is meant to be included into a {@link Router} managed module.
   *
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   * @see <a href="https://dagger.dev/dev-guide/">Technical reference</a>
   */
  @SuppressWarnings("unused")
  @dagger.Module
  public interface Mod {

    @dagger.Binds
    Controller.Single<Token> controller(final TokenController t);
  }
}
