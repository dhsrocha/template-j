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
  requires jjwt.api;
  requires transitive java.net.http;
  // Persistence
  requires org.jooq;
  requires com.zaxxer.hikari;
  requires org.postgresql.jdbc;
  requires transitive java.sql;

  // Open for testing
  opens template;
  opens template.core;
  opens template.feature.info;
  opens template.feature.auth;
  opens template.feature.user;
  opens template.feature.address;
}
