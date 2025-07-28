import { OpenIdConfiguration } from 'angular-auth-oidc-client'; // <-- Importação CORRETA

const oidcConfig: OpenIdConfiguration = {
  authority: 'https://seukeycloak.com/realms/controle-academico', // Altere para a URL do Keycloak em produção
  redirectUrl: window.location.origin,
  postLogoutRedirectUri: window.location.origin,
  clientId: 'academico-frontend', // Client ID do seu cliente em produção
  scope: 'openid profile email offline_access',
  responseType: 'code',
  silentRenew: true,
  silentRenewUrl: window.location.origin + '/silent-renew.html',
  logLevel: 0, // LogLevel.None é 0 para produção
  // useRefreshTokens: true,
};

export const environment = {
  production: true,
  oidcConfig: oidcConfig,
  apiUrl: 'http://localhost:8081' // Altere para a URL da API em produção
};