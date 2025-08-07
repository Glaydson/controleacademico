import { OpenIdConfiguration, LogLevel } from 'angular-auth-oidc-client';

const oidcConfig: OpenIdConfiguration = {
  configId: 'controle-academico', // Add explicit config ID
  authority: window.location.origin + '/realms/controle-academico',
  clientId: 'academico-frontend',
  redirectUrl: window.location.origin,
  postLogoutRedirectUri: window.location.origin,
  responseType: 'code',
  scope: 'openid profile email roles backend-audience',
  
  // Configurações simplificadas para depuração
  silentRenew: false,
  useRefreshToken: false, // Temporarily disable to simplify
  autoUserInfo: false, // Temporarily disable to simplify
  
  // Configurações de segurança
  historyCleanupOff: true, // Enable cleanup to avoid state conflicts
  
  // Configurações de fluxo
  triggerAuthorizationResultEvent: true,
  postLoginRoute: '/home',
  forbiddenRoute: '/forbidden',
  unauthorizedRoute: '/unauthorized',
  
  // Debug apenas em desenvolvimento
  logLevel: LogLevel.Debug,
  
  // Configurações adicionais para estabilidade
  ignoreNonceAfterRefresh: false, // Enable nonce validation
  disableIdTokenValidation: false,
  
  // Configuração para PKCE (mais seguro)
  usePushedAuthorisationRequests: false,
  
  // Silent renew configuration
  silentRenewUrl: window.location.origin + '/silentRenew.html'
};

export const environment = {
  production: false,
  oidcConfig,
  apiUrl: 'http://localhost:4200/api'
};