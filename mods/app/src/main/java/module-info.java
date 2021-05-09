/**
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
module template.app {
  // System
  requires lombok;
  requires org.slf4j;
  requires com.google.gson;
  // CDI
  requires dagger;
  requires javax.inject;
  requires transitive java.compiler;
  // Application
  requires ehcache;
  requires io.javalin;
  requires transitive java.net.http;
  // Persistence
  requires org.jooq;
  requires com.zaxxer.hikari;
  requires org.postgresql.jdbc;
  requires transitive java.sql;
  // Open API
  requires io.swagger.v3.oas.annotations;
  requires transitive java.ws.rs;

  // Open for testing
  opens template;
  opens template.core;
  opens template.feature.info;
  opens template.feature.user;
  opens template.feature.address;
}
