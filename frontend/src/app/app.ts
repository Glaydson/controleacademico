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

  constructor(public oidcSecurityService: OidcSecurityService) {}

  ngOnInit() {
    console.log('🏁 [APP] Iniciando verificação de autenticação...');
    console.log('🏁 [APP] Current URL:', window.location.href);
    console.log('🏁 [APP] URL params:', window.location.search);
    console.log('🏁 [APP] URL hash:', window.location.hash);
    
    // Check if this is a callback from Keycloak
    const isCallback = window.location.search.includes('code=') || 
                      window.location.search.includes('state=') ||
                      window.location.hash.includes('code=') || 
                      window.location.hash.includes('state=');
    
    if (isCallback) {
      console.log('🔄 [APP] Detectado callback do Keycloak - processando...');
      // For callbacks, still try to process
      this.processAuthCheck();
    } else {
      console.log('🔄 [APP] Não é um callback - verificando estado sem erro');
      // For normal page loads, use a safer approach
      this.checkAuthSafely();
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
          
          // Clear any stored state
          try {
            localStorage.removeItem('angular-auth-oidc-client-code-flow-state');
            localStorage.removeItem('angular-auth-oidc-client-code-flow-nonce');
            sessionStorage.removeItem('angular-auth-oidc-client-code-flow-state');
            sessionStorage.removeItem('angular-auth-oidc-client-code-flow-nonce');
          } catch (storageErr) {
            console.log('🧹 [APP] Erro limpando storage:', storageErr);
          }
          
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

  login() {
    console.log('🚪 [APP] Login manual iniciado');
    this.oidcSecurityService.authorize();
  }

  logout() {
    console.log('🚪 [APP] Logout iniciado');
    this.oidcSecurityService.logoffAndRevokeTokens();
  }
}