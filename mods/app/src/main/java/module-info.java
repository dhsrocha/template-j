/**
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
module template.app {
  // System
  requires lombok;
  requires org.slf4j;
  // CDI
  requires java.compiler;
  requires javax.inject;
  requires dagger;
  // Application
  requires java.net.http;
  requires io.javalin;
  requires com.google.gson;
  requires ehcache;

  // Open for testing
  opens template;
  opens template.feature.info;
  opens template.feature.user;
  opens template.feature.address;
}
