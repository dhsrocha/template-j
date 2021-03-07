module template.app {
  // System
  requires lombok;
  requires org.slf4j;
  // Application
  requires io.javalin;

  // Open for testing
  opens template;
  opens template.feature.user;
}
