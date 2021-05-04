package template.auth;

import template.Application.Feat;
import template.Client;
import template.Support.IntegrationTest;
import template.feature.auth.Auth;

@IntegrationTest({Feat.USER, Feat.AUTH})
final class AuthTest {

  private static final Client<Auth> CLIENT = Client.create(Auth.class);
}
