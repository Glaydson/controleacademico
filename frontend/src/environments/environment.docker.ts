import { OpenIdConfiguration, PassedInitialConfig } from 'angular-auth-oidc-client';

const oidcConfig: OpenIdConfiguration = {
  configId: 'controle-academico',
  authority: 'http://localhost:8080/realms/controle-academico', // Use direct Keycloak URL
  clientId: 'academico-frontend',
  redirectUrl: window.location.origin,
  postLogoutRedirectUri: window.location.origin,
  responseType: 'code',
  scope: 'openid profile email roles backend-audience',
  triggerAuthorizationResultEvent: true,
  postLoginRoute: '/home',
  forbiddenRoute: '/home',
  unauthorizedRoute: '/home',
  logLevel: 0,
  historyCleanupOff: false, // Enable cleanup to fix state issues
  silentRenew: false, // Disable silent renewal temporarily
  silentRenewUrl: window.location.origin + '/silentRenew.html',
  silentRenewTimeoutInSeconds: 60,
  renewTimeBeforeTokenExpiresInSeconds: 10,
  useRefreshToken: true,
  autoUserInfo: false, // Disable automatic user info fetching
  startCheckSession: false,
  maxIdTokenIatOffsetAllowedInSeconds: 1000,
  disableRefreshIdTokenAuthTimeValidation: true,
  ignoreNonceAfterRefresh: true, // Add this to help with state issues
  disableIatOffsetValidation: true, // Disable time validation
  usePushedAuthorisationRequests: false, // Disable PAR
  disablePkce: false, // Ensure PKCE is enabled (default)
  // Explicitly set to public client mode
  secureRoutes: [],
  customParamsAuthRequest: {},
  customParamsCodeRequest: {},
  customParamsRefreshTokenRequest: {},
  customParamsEndSessionRequest: {}
};

export const environment = {
  production: false,
  development: true,
  apiUrl: '/api', // Use relative URL that goes through nginx proxy
  oidcConfig: oidcConfig
};
