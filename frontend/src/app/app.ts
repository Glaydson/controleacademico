import { Component, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-root',
  templateUrl: './app.html', 
  styleUrls: ['./app.css'],
  standalone: false 
})
export class AppComponent implements OnInit {
  title = 'Controle Acadêmico Web'; 
  isAuthenticated = false;
  userData: any;
  private authCallbackProcessed = false; // Flag to prevent multiple callback processing

  constructor(public oidcSecurityService: OidcSecurityService) {}

  ngOnInit() {
    console.log('🏁 [APP] Iniciando verificação de autenticação...');
    console.log('🏁 [APP] Current URL:', window.location.href);
    
    const currentUrl = window.location.href;
    const hasAuthParams = currentUrl.includes('?code=') || currentUrl.includes('&code=') || currentUrl.includes('?state=');
    
    if (hasAuthParams) {
      console.log('🔄 [APP] Callback de autenticação detectado, usando estratégia robusta...');
      this.handleAuthCallbackRobust();
    } else {
      console.log('🔍 [APP] Verificação normal de autenticação...');
      this.checkAuthentication();
    }

    // Subscribe para mudanças no status de autenticação
    this.oidcSecurityService.isAuthenticated$.subscribe({
      next: (result) => {
        console.log('📊 [APP] Status de autenticação mudou:', result.isAuthenticated);
        this.isAuthenticated = result.isAuthenticated;
        
        if (result.isAuthenticated && !this.userData) {
          this.loadUserData();
        }
      }
    });
  }

  private handleAuthCallbackRobust(): void {
    console.log('🔄 [APP] Iniciando processamento robusto do callback...');
    
    // Wait a moment to let authentication state settle
    setTimeout(() => {
      // Check if authentication already succeeded (from the status subscription)
      if (this.isAuthenticated) {
        console.log('✅ [APP] Autenticação já bem-sucedida, limpando URL...');
        this.cleanUrlAndRedirect();
        return;
      }

      // If not authenticated yet, try standard checkAuth
      this.oidcSecurityService.checkAuth().subscribe({
        next: (result) => {
          console.log('✅ [APP] CheckAuth bem-sucedido:', result);
          this.processAuthResult(result);
        },
        error: (error) => {
          console.log('⚠️ [APP] CheckAuth falhou, mas verificando se auth já funcionou:', error);
          // Before giving up, check if authentication actually worked despite the error
          this.verifyActualAuthState();
        }
      });
    }, 1000);
  }

  private verifyActualAuthState(): void {
    console.log('🔍 [APP] Verificando estado real de autenticação...');
    
    // Check current authentication status
    this.oidcSecurityService.isAuthenticated$.subscribe({
      next: (authResult) => {
        console.log('🔍 [APP] Estado de autenticação verificado:', authResult.isAuthenticated);
        if (authResult.isAuthenticated) {
          console.log('✅ [APP] Autenticação bem-sucedida apesar do erro de callback');
          this.isAuthenticated = true;
          this.loadUserDataFromToken();
          this.cleanUrlAndRedirect();
        } else {
          // Try token-based verification as final fallback
          this.fallbackAuthCheck();
        }
      }
    });
  }

  private fallbackAuthCheck(): void {
    console.log('🔄 [APP] Executando verificação alternativa de autenticação...');
    
    // Check if we have a valid token in storage
    this.oidcSecurityService.getAccessToken().subscribe({
      next: (token) => {
        if (token && token.length > 0) {
          console.log('✅ [APP] Token encontrado na verificação alternativa');
          // Token exists, check if it's valid by verifying auth status
          this.oidcSecurityService.isAuthenticated$.subscribe({
            next: (authResult) => {
              console.log('✅ [APP] Verificação alternativa - autenticado:', authResult.isAuthenticated);
              if (authResult.isAuthenticated) {
                this.isAuthenticated = true;
                this.loadUserDataFromToken();
                this.cleanUrlAndRedirect();
              } else {
                this.handleAuthFailure();
              }
            }
          });
        } else {
          console.log('❌ [APP] Nenhum token encontrado na verificação alternativa');
          this.handleAuthFailure();
        }
      },
      error: (error) => {
        console.log('❌ [APP] Erro na verificação alternativa:', error);
        this.handleAuthFailure();
      }
    });
  }

  private processAuthResult(result: any): void {
    if (result.isAuthenticated) {
      console.log('✅ [APP] Usuário autenticado com sucesso');
      this.isAuthenticated = true;
      this.loadUserDataFromToken();
      this.cleanUrlAndRedirect();
    } else {
      console.log('❌ [APP] Falha na autenticação');
      this.handleAuthFailure();
    }
  }

  private cleanUrlAndRedirect(): void {
    console.log('🧹 [APP] Limpando URL e redirecionando para home...');
    // Clean URL
    window.history.replaceState({}, document.title, '/home');
    // Gentle redirect without forcing page reload
    setTimeout(() => {
      if (window.location.pathname !== '/home') {
        window.location.href = '/home';
      }
    }, 500);
  }

  private handleAuthFailure(): void {
    console.log('❌ [APP] Verificando se realmente há falha de autenticação...');
    
    // Double-check before cleaning up - don't logout authenticated users
    setTimeout(() => {
      this.oidcSecurityService.isAuthenticated$.subscribe({
        next: (authResult) => {
          if (authResult.isAuthenticated) {
            console.log('✅ [APP] Usuário ainda autenticado, não fazendo logout');
            this.isAuthenticated = true;
            // Just clean URL, don't logout
            window.history.replaceState({}, document.title, '/home');
          } else {
            console.log('❌ [APP] Usuário realmente não autenticado, limpando estado');
            // Clean URL but don't force redirect - let user try again
            window.history.replaceState({}, document.title, '/');
            this.isAuthenticated = false;
            this.userData = null;
          }
        }
      });
    }, 500);
  }

  private checkAuthentication(): void {
    // For normal authentication check, just verify current state
    this.oidcSecurityService.isAuthenticated$.pipe().subscribe({
      next: (result) => {
        console.log('🔍 [APP] Estado atual de autenticação:', result.isAuthenticated);
        this.isAuthenticated = result.isAuthenticated;
        if (result.isAuthenticated) {
          this.loadUserDataFromToken();
        }
      }
    });
  }

  private checkAuthSafely() {
    // Check authentication without triggering callback processing
    this.oidcSecurityService.isAuthenticated$.subscribe({
      next: (result) => {
        console.log('� [APP] Safe auth check:', result.isAuthenticated);
        this.isAuthenticated = result.isAuthenticated;
        
        if (result.isAuthenticated) {
          this.loadUserDataFromToken();
        }
      }
    });
  }

  private processAuthCheck() {
    // Método simplificado para Angular 20
    this.oidcSecurityService.checkAuth().subscribe({
      next: (result) => {
        console.log('🔍 [APP] CheckAuth resultado completo:', result);
        console.log('🔍 [APP] Result configId:', result.configId);
        console.log('🔍 [APP] Result errorMessage:', result.errorMessage);
        console.log('🔍 [APP] Result idToken:', result.idToken ? 'Present' : 'Not present');
        console.log('🔍 [APP] Result accessToken:', result.accessToken ? 'Present' : 'Not present');
        
        this.isAuthenticated = result.isAuthenticated;
        
        if (result.isAuthenticated) {
          console.log('✅ [APP] Usuário autenticado com sucesso');
          
          // Load user data from token claims instead of separate endpoint
          this.loadUserDataFromToken();
          
        } else {
          console.log('🚪 [APP] Usuário não autenticado - aguardando interação do usuário');
          if (result.errorMessage) {
            console.error('🔍 [APP] Error message:', result.errorMessage);
          }
        }
      },
      error: (err) => {
        console.error('❌ [APP] Erro na verificação de autenticação:', err);
        console.error('❌ [APP] Error details:', {
          message: err.message,
          stack: err.stack,
          name: err.name
        });
        
        // If this is a state error, try to clear the URL and check auth again
        if (err.message && err.message.includes('could not find matching config for state')) {
          console.log('🧹 [APP] Limpando URL devido a erro de state');
          
          // Clear any stored state and URL
          this.clearAuthStorage();
          window.history.replaceState({}, document.title, window.location.pathname);
          
          // Try a safer auth check after clearing URL
          setTimeout(() => {
            this.checkAuthSafely();
          }, 100);
        }
      }
    });
  }

  private loadUserData() {
    this.oidcSecurityService.getUserData().subscribe({
      next: (userData) => {
        this.userData = userData;
        console.log('👤 [APP] Dados do usuário carregados:', userData);
      },
      error: (err) => {
        console.error('❌ [APP] Erro ao carregar dados do usuário:', err);
        // Fallback to loading from token
        this.loadUserDataFromToken();
      }
    });
  }

  private loadUserDataFromToken() {
    // Get user data from ID token claims instead of userinfo endpoint
    this.oidcSecurityService.getPayloadFromIdToken().subscribe({
      next: (tokenData) => {
        if (tokenData) {
          // Extract user info from token claims
          this.userData = {
            sub: tokenData.sub,
            name: tokenData.name || tokenData.preferred_username,
            email: tokenData.email,
            given_name: tokenData.given_name,
            family_name: tokenData.family_name,
            preferred_username: tokenData.preferred_username,
            realm_access: tokenData.realm_access,
            resource_access: tokenData.resource_access
          };
          console.log('👤 [APP] Dados do usuário extraídos do token:', this.userData);
        } else {
          console.log('⚠️ [APP] Nenhum dado encontrado no token');
        }
      },
      error: (err) => {
        console.error('❌ [APP] Erro ao extrair dados do token:', err);
      }
    });
  }

  private processAuthCallbackOnce(callbackUrl?: string) {
    // Store the current URL parameters before processing
    const currentUrl = callbackUrl || window.location.href;
    const urlParams = new URLSearchParams(new URL(currentUrl).search);
    const code = urlParams.get('code');
    const state = urlParams.get('state');
    
    console.log('🔍 [APP] Processing callback with code:', code?.substring(0, 8) + '...', 'state:', state?.substring(0, 8) + '...');
    
    // Process the callback only once and then clear the URL
    this.oidcSecurityService.checkAuth(currentUrl).subscribe({
      next: (result) => {
        console.log('🔍 [APP] CheckAuth resultado completo:', result);
        this.isAuthenticated = result.isAuthenticated;
        
        if (result.isAuthenticated) {
          console.log('✅ [APP] Usuário autenticado com sucesso via callback');
          this.loadUserDataFromToken();
        } else {
          console.log('❌ [APP] Falha na autenticação via callback:', result.errorMessage);
        }
      },
      error: (err) => {
        console.error('❌ [APP] Erro no callback de autenticação:', err);
        // Clear URL and storage on error
        this.clearAuthStorage();
      }
    });
  }

  private clearAuthStorage() {
    try {
      localStorage.removeItem('angular-auth-oidc-client-code-flow-state');
      localStorage.removeItem('angular-auth-oidc-client-code-flow-nonce');
      sessionStorage.removeItem('angular-auth-oidc-client-code-flow-state');
      sessionStorage.removeItem('angular-auth-oidc-client-code-flow-nonce');
    } catch (storageErr) {
      console.log('🧹 [APP] Erro limpando storage:', storageErr);
    }
  }

  login() {
    console.log('🚪 [APP] Login manual iniciado');
    this.oidcSecurityService.authorize();
  }

  logout() {
    console.log('🚪 [APP] Logout iniciado');
    this.oidcSecurityService.logoffAndRevokeTokens();
  }
}