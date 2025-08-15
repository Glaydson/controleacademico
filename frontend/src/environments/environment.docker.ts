import { OpenIdConfiguration, PassedInitialConfig } from 'angular-auth-oidc-client';

const oidcConfig: OpenIdConfiguration = {
  configId: 'controle-academico',
  authority: 'http://localhost:8080/realms/controle-academico',
  clientId: 'academico-frontend',
  redirectUrl: window.location.origin,
  postLogoutRedirectUri: window.location.origin,
  responseType: 'code',
  scope: 'openid profile email roles',
  triggerAuthorizationResultEvent: true,
  postLoginRoute: '/home',
  forbiddenRoute: '/home',
  unauthorizedRoute: '/home',
  logLevel: 0,
  
  // Simplified configuration to minimize state issues
  historyCleanupOff: true, // Disable automatic cleanup to prevent interference
  silentRenew: false,
  useRefreshToken: false,
  autoUserInfo: false,
  startCheckSession: false,
  
  // Disable problematic validations that might cause state mismatches
  maxIdTokenIatOffsetAllowedInSeconds: 3600,
  disableRefreshIdTokenAuthTimeValidation: true,
  ignoreNonceAfterRefresh: true,
  disableIatOffsetValidation: true,
  usePushedAuthorisationRequests: false,
  disablePkce: false,
  
  // Minimal secure routes
  secureRoutes: ['/api'],
  
  // Empty custom params to avoid any parameter conflicts
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
