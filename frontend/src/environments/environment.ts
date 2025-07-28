import { OpenIdConfiguration, LogLevel } from 'angular-auth-oidc-client';

const oidcConfig: OpenIdConfiguration = {
  authority: 'http://localhost:8080/realms/controle-academico',
  clientId: 'academico-frontend',
  redirectUrl: window.location.origin,
  postLogoutRedirectUri: window.location.origin,
  responseType: 'code',
  scope: 'openid profile email roles',
  
  // Configurações otimizadas:
  silentRenew: false,
  useRefreshToken: false,
  renewTimeBeforeTokenExpiresInSeconds: 30,
  autoUserInfo: true,
  
  triggerAuthorizationResultEvent: true,
  postLoginRoute: '/',
  
  // Debug
  logLevel: LogLevel.Debug,
  customParamsAuthRequest: {
    prompt: 'login'
  }
};

export const environment = {
  production: false,
  oidcConfig: oidcConfig,
  apiUrl: 'http://localhost:8081'
};