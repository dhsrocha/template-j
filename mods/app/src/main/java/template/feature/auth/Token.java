package template.feature.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.val;
import template.base.Exceptions;

/**
 * Value object which represents.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@lombok.Value
class Token {

  @AllArgsConstructor
  private enum Key {
    EXPIRATION("role"),
    ;
    String val;
  }

  // TODO:
  //  * Generate key when creating for a user registration
  //  * Parse incoming key for authorization / authentication

  private static final Duration EXPIRATION = Duration.ofMinutes(5);
  Claims claims;

  static Token parse(final @lombok.NonNull String key,
                     final @lombok.NonNull String compact) {
    return Exceptions.ILLEGAL_ARGUMENT.trapIn(() -> {
      val jwt = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(compact);
      return new Token(jwt.getBody());
    });
  }

  String get(final Key key) {
    return claims.get(key.name(), String.class);
  }

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
}
