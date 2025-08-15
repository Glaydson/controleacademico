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
    
    console.log('🔒 [ROLE-GUARD] Verificando acesso para rota:', state.url);
    console.log('🔒 [ROLE-GUARD] Roles necessárias:', requiredRoles);

    if (!requiredRoles || requiredRoles.length === 0) {
      console.log('🔒 [ROLE-GUARD] Nenhuma role necessária, acesso permitido');
      return of(true); // Nenhuma role necessária, acesso permitido
    }

    // First, check if user is authenticated without triggering any auth flows
    return this.oidcSecurityService.isAuthenticated$.pipe( 
      switchMap(authResult => {
        console.log('🔒 [ROLE-GUARD] Estado de autenticação:', authResult.isAuthenticated);
        
        if (!authResult.isAuthenticated) {
          console.log('🔒 [ROLE-GUARD] Usuário não autenticado, redirecionando para home');
          this.router.navigate(['/home']);
          return of(false);
        }

        // User is authenticated, now check token and roles
        return this.oidcSecurityService.getAccessToken().pipe(
          switchMap(token => {
            console.log('🔒 [ROLE-GUARD] Token obtido:', token ? 'Presente' : 'Ausente');
            
            if (!token) {
              console.log('🔒 [ROLE-GUARD] Token não disponível mas usuário autenticado, permitindo acesso temporariamente');
              // Don't redirect to auth, just allow access since user is authenticated
              return of(true);
            }

            let userRoles: string[] = [];
            try {
              const decodedToken: any = JSON.parse(atob(token.split('.')[1]));
              console.log('🔒 [ROLE-GUARD] Token decodificado com sucesso');
              
              if (decodedToken && decodedToken.realm_access && Array.isArray(decodedToken.realm_access.roles)) {
                // Filtrar e padronizar as roles para maiúsculas
                userRoles = decodedToken.realm_access.roles.filter((role: string) =>
                    ['ADMIN', 'COORDENADOR', 'PROFESSOR', 'ALUNO'].includes(role.toUpperCase())
                );
                console.log('🔒 [ROLE-GUARD] Roles do usuário:', userRoles);
              }
            } catch (e) {
              console.error('🔒 [ROLE-GUARD] Erro ao decodificar access token no guard', e);
              this.router.navigate(['/home']); // Tratar erro de decodificação
              return of(false);
            }
            
            // Verifica se o usuário possui QUALQUER uma das roles requeridas
            const hasRequiredRole = requiredRoles.some(role => userRoles.includes(role.toUpperCase()));
            console.log('🔒 [ROLE-GUARD] Usuário possui role necessária:', hasRequiredRole);

            if (hasRequiredRole) {
              console.log('✅ [ROLE-GUARD] Acesso permitido para:', state.url);
              return of(true); // Acesso permitido
            } else {
              // Redireciona para uma página de "acesso negado" ou para a home
              console.log('❌ [ROLE-GUARD] Acesso negado para:', state.url, 'roles necessárias:', requiredRoles, 'roles do usuário:', userRoles);
              alert('Você não tem permissão para acessar esta página.');
              this.router.navigate(['/home']);
              return of(false);
            }
          })
        );
      })
    );
  }
}