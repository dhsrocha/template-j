module template.app {
  // System
  requires lombok;
  requires org.slf4j;
  // CDI
  requires java.compiler;
  requires javax.inject;
  requires dagger;
  // Application
  requires io.javalin;

  // Open for testing
  opens template;
  opens template.feature.user;
  opens template.infra;
}
