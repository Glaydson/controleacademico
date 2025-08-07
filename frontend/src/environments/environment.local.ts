import { OpenIdConfiguration, LogLevel } from 'angular-auth-oidc-client';

const oidcConfig: OpenIdConfiguration = {
  authority: 'http://localhost:8080/realms/controle-academico',
  clientId: 'academico-frontend',
  redirectUrl: window.location.origin,
  postLogoutRedirectUri: window.location.origin,
  responseType: 'code',
  scope: 'openid profile email roles backend-audience',
  
  silentRenew: true,
  useRefreshToken: true,
  renewTimeBeforeTokenExpiresInSeconds: 60,
  autoUserInfo: true,
  
  historyCleanupOff: false,
  triggerAuthorizationResultEvent: true,
  postLoginRoute: '/',
  forbiddenRoute: '/forbidden',
  unauthorizedRoute: '/unauthorized',
  
  logLevel: LogLevel.Debug,
  ignoreNonceAfterRefresh: true,
  disableIdTokenValidation: false,
  usePushedAuthorisationRequests: false
};

export const environment = {
  production: false,
  oidcConfig: {
    ...oidcConfig,
    authority: 'http://localhost:8080/realms/controle-academico',
  },
  apiUrl: 'http://localhost:8081'
};
