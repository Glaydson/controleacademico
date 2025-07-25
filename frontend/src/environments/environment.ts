import { OpenIdConfiguration } from 'angular-auth-oidc-client'; // <-- Importação CORRETA

const oidcConfig: OpenIdConfiguration = {
  authority: 'http://localhost:8080/realms/controle-academico', // URL base do Keycloak com o realm
  redirectUrl: window.location.origin,
  postLogoutRedirectUri: window.location.origin,
  clientId: 'academico-frontend', // CONFIRME QUE É 'academico-frontend'
  scope: 'openid profile email offline_access',
  responseType: 'code',
  silentRenew: true,
  silentRenewUrl: window.location.origin + '/silent-renew.html',
  logLevel: 3, // LogLevel.Debug é 3, se você quiser manter como número
  // useRefreshTokens: true, // Descomente se for usar refresh tokens
  // secureRoutes: ['/api'], // Rotas que exigem token Bearer (opcional, pode ser configurado no interceptor)
  // customParams: {
  //   kc_idp_hint: 'my-identity-provider' // Exemplo: se usar um Identity Provider específico
  // }
};

export const environment = {
  production: false,
  oidcConfig: oidcConfig // Use um nome claro como oidcConfig
};