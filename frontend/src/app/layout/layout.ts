import { Component, OnInit } from '@angular/core';
import { OidcSecurityService, UserDataResult } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.html',
  styleUrls: ['./layout.css'],
  standalone: false 
})
export class LayoutComponent  {
  userName: string | undefined;
  userRoles: string[] = [];

  constructor(private oidcSecurityService: OidcSecurityService) { }

  ngOnInit(): void {
    this.oidcSecurityService.userData$.subscribe((userDataResult: UserDataResult) => {
      if (userDataResult.userData) {
        this.userName = userDataResult.userData.preferred_username || userDataResult.userData.name;
      }
    });

    // É melhor usar o .subscribe dentro do ngOnInit ou de um método, não direto no pipe
    this.oidcSecurityService.getAccessToken().subscribe(token => {
        if (token) {
            try {
                const decodedToken: any = JSON.parse(atob(token.split('.')[1]));
                if (decodedToken && decodedToken.realm_access && Array.isArray(decodedToken.realm_access.roles)) {
                    // Filtrar apenas as roles relevantes para sua aplicação (ADMIN, COORDENADOR, etc.)
                    // E convertê-las para maiúsculas para corresponder ao token, se necessário
                    this.userRoles = decodedToken.realm_access.roles.filter((role: string) =>
                        ['ADMIN', 'COORDENADOR', 'PROFESSOR', 'ALUNO'].includes(role.toUpperCase())
                    );
                    console.log('Roles do usuário (Decodificadas):', this.userRoles); // Para depuração
                }
            } catch (e) {
                console.error('Erro ao decodificar access token ou extrair roles', e);
            }
        }
    });
  }

  hasRole(role: string): boolean {
    // Compare a role passada com as roles do usuário (em maiúsculas para consistência)
    return this.userRoles.includes(role.toUpperCase());
  }

  hasAnyRole(roles: string[]): boolean {
    // Compare as roles passadas com as roles do usuário (em maiúsculas para consistência)
    return roles.some(role => this.userRoles.includes(role.toUpperCase()));
  }

  logout(): void {
  this.oidcSecurityService.logoffAndRevokeTokens().subscribe((result) => {
    console.log('Logout completo:', result);
    // Opcional: Navegar programaticamente para a home ou uma página de logout
    // this.router.navigate(['/']);
  });

}
}