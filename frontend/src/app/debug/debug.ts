import { Component, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-debug',
  standalone: false,
  template: `
    <div class="debug-container">
      <h2>🔍 Debug: Estado de Autenticação</h2>
      
      <div class="auth-info">
        <h3>Estado da Autenticação:</h3>
        <p><strong>Autenticado:</strong> {{ isAuthenticated ? 'Sim' : 'Não' }}</p>
        <p><strong>Token Presente:</strong> {{ hasToken ? 'Sim' : 'Não' }}</p>
        
        <h3>Informações do Token:</h3>
        <pre *ngIf="tokenInfo">{{ getTokenInfoJson() }}</pre>
        <p *ngIf="!tokenInfo">Nenhum token disponível</p>
        
        <h3>Roles do Usuário:</h3>
        <ul *ngIf="userRoles && userRoles.length > 0">
          <li *ngFor="let role of userRoles">{{ role }}</li>
        </ul>
        <p *ngIf="!userRoles || userRoles.length === 0">Nenhuma role encontrada</p>
        
        <h3>Dados do Usuário:</h3>
        <pre *ngIf="userData">{{ getUserDataJson() }}</pre>
        <p *ngIf="!userData">Nenhum dado de usuário disponível</p>
      </div>
      
      <button (click)="refreshInfo()">🔄 Atualizar Informações</button>
      <button (click)="testAdminAccess()">🔑 Testar Acesso Admin</button>
    </div>
  `,
  styles: [`
    .debug-container {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }
    
    .auth-info {
      background: #f5f5f5;
      padding: 15px;
      border-radius: 5px;
      margin: 10px 0;
    }
    
    pre {
      background: #fff;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 3px;
      overflow-x: auto;
      max-height: 300px;
    }
    
    button {
      margin: 5px;
      padding: 10px 15px;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 3px;
      cursor: pointer;
    }
    
    button:hover {
      background: #0056b3;
    }
    
    h2 { color: #333; }
    h3 { color: #666; margin-top: 20px; }
  `]
})
export class DebugComponent implements OnInit {
  isAuthenticated = false;
  hasToken = false;
  tokenInfo: any = null;
  userRoles: string[] = [];
  userData: any = null;

  constructor(private oidcSecurityService: OidcSecurityService) {}

  ngOnInit(): void {
    this.refreshInfo();
  }

  getTokenInfoJson(): string {
    return JSON.stringify(this.tokenInfo, null, 2);
  }

  getUserDataJson(): string {
    return JSON.stringify(this.userData, null, 2);
  }

  refreshInfo(): void {
    console.log('🔍 [DEBUG] Atualizando informações de debug...');
    
    // Check authentication state
    this.oidcSecurityService.isAuthenticated$.subscribe({
      next: (authResult) => {
        console.log('🔍 [DEBUG] Estado de autenticação:', authResult);
        this.isAuthenticated = authResult.isAuthenticated;
      }
    });

    // Get user data separately
    this.oidcSecurityService.getUserData().subscribe({
      next: (userData) => {
        console.log('🔍 [DEBUG] Dados do usuário:', userData);
        this.userData = userData;
      },
      error: (error) => {
        console.log('🔍 [DEBUG] Erro ao obter dados do usuário:', error);
      }
    });

    // Get access token
    this.oidcSecurityService.getAccessToken().subscribe({
      next: (token) => {
        console.log('🔍 [DEBUG] Token obtido:', token ? 'Presente' : 'Ausente');
        this.hasToken = !!token;
        
        if (token) {
          try {
            // Decode token
            const tokenParts = token.split('.');
            const decodedToken = JSON.parse(atob(tokenParts[1]));
            console.log('🔍 [DEBUG] Token decodificado:', decodedToken);
            
            this.tokenInfo = {
              sub: decodedToken.sub,
              preferred_username: decodedToken.preferred_username,
              email: decodedToken.email,
              realm_access: decodedToken.realm_access,
              resource_access: decodedToken.resource_access,
              scope: decodedToken.scope,
              iat: new Date(decodedToken.iat * 1000),
              exp: new Date(decodedToken.exp * 1000)
            };
            
            // Extract roles
            if (decodedToken.realm_access && decodedToken.realm_access.roles) {
              this.userRoles = decodedToken.realm_access.roles.filter((role: string) =>
                ['ADMIN', 'COORDENADOR', 'PROFESSOR', 'ALUNO'].includes(role.toUpperCase())
              );
            }
            
            console.log('🔍 [DEBUG] Roles do usuário:', this.userRoles);
            
          } catch (error) {
            console.error('🔍 [DEBUG] Erro ao decodificar token:', error);
            this.tokenInfo = { error: 'Erro ao decodificar token' };
          }
        }
      },
      error: (error) => {
        console.error('🔍 [DEBUG] Erro ao obter token:', error);
        this.hasToken = false;
      }
    });
  }

  testAdminAccess(): void {
    console.log('🔑 [DEBUG] Testando acesso de admin...');
    
    const hasAdminRole = this.userRoles.includes('ADMIN');
    const message = hasAdminRole 
      ? '✅ Usuário possui role ADMIN' 
      : '❌ Usuário NÃO possui role ADMIN';
    
    console.log('🔑 [DEBUG]', message);
    alert(message);
  }
}
