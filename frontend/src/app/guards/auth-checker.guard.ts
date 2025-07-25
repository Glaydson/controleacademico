// src/app/guards/auth-checker.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthCheckerGuard implements CanActivate {

  constructor(private oidcSecurityService: OidcSecurityService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    return this.oidcSecurityService.checkAuth().pipe(
      map(({ isAuthenticated }) => {
        if (isAuthenticated) {
          return true; // Usuário autenticado, permite acesso à rota
        } else {
          // Usuário não autenticado, inicia o fluxo de login
          // ou redireciona para uma página pública, se houver
          this.oidcSecurityService.authorize(); // Inicia o login via Keycloak
          return false; // Não permite o acesso imediato à rota
        }
      })
    );
  }
}