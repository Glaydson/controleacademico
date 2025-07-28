import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { OidcSecurityService, UserDataResult } from 'angular-auth-oidc-client';
import { Observable, of } from 'rxjs'; 
import { map, switchMap } from 'rxjs/operators'; 

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private oidcSecurityService: OidcSecurityService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    const requiredRoles = route.data['roles'] as string[]; // Roles esperadas da rota (ADMIN, COORDENADOR, etc.)

    if (!requiredRoles || requiredRoles.length === 0) {
      return of(true); // Nenhuma role necessária, acesso permitido
    }

    return this.oidcSecurityService.getAccessToken().pipe( 
      switchMap(token => {
        if (!token) {
          this.router.navigate(['/home']);
          return of(false);
        }

        let userRoles: string[] = [];
        try {
          const decodedToken: any = JSON.parse(atob(token.split('.')[1]));
          if (decodedToken && decodedToken.realm_access && Array.isArray(decodedToken.realm_access.roles)) {
            // Filtrar e padronizar as roles para maiúsculas
            userRoles = decodedToken.realm_access.roles.filter((role: string) =>
                ['ADMIN', 'COORDENADOR', 'PROFESSOR', 'ALUNO'].includes(role.toUpperCase())
            );
          }
        } catch (e) {
          console.error('Erro ao decodificar access token no guard', e);
          this.router.navigate(['/home']); // Tratar erro de decodificação
          return of(false);
        }
        
        // Verifica se o usuário possui QUALQUER uma das roles requeridas
        const hasRequiredRole = requiredRoles.some(role => userRoles.includes(role.toUpperCase()));

        if (hasRequiredRole) {
          return of(true); // Acesso permitido
        } else {
          // Redireciona para uma página de "acesso negado" ou para a home
          alert('Você não tem permissão para acessar esta página.');
          this.router.navigate(['/home']);
          return of(false);
        }
      })
    );
  }
}