// src/app/interceptors/auth.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { switchMap } from 'rxjs/operators';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private oidcSecurityService: OidcSecurityService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
     // Adicione uma verificação para não interceptar requisições para o Keycloak
  if (req.url.includes('/realms/controle-academico')) {
    return next.handle(req);
  }
    console.log('🚀 [INTERCEPTOR] === INTERCEPTOR EXECUTADO ===');
    console.log('🚀 [INTERCEPTOR] URL:', req.url);
    console.log('🚀 [INTERCEPTOR] Method:', req.method);
    console.log('🚀 [INTERCEPTOR] Headers atuais:', req.headers.keys());
    
    // TEMPORÁRIO: INTERCEPTA TODAS AS REQUISIÇÕES PARA DEBUG
    console.log('📡 [INTERCEPTOR] Processando TODAS as requisições para debug');
    
    return this.oidcSecurityService.getAccessToken().pipe(
      switchMap(token => {
        console.log('🔑 [INTERCEPTOR] Token obtido:', token ? 'PRESENTE' : 'AUSENTE');
        
        if (token && token.length > 0) {
          console.log('🔑 [INTERCEPTOR] Token (primeiros 50 chars):', token.substring(0, 50) + '...');
          
          // Clone a requisição e adiciona o header Authorization
          const authReq = req.clone({
            headers: req.headers.set('Authorization', `Bearer ${token}`)
          });
          
          console.log('✅ [INTERCEPTOR] Header Authorization adicionado');
          console.log('✅ [INTERCEPTOR] Headers finais:', authReq.headers.keys());
          console.log('✅ [INTERCEPTOR] Authorization header value:', authReq.headers.get('Authorization')?.substring(0, 30) + '...');
          
          return next.handle(authReq);
        } else {
          console.log('⚠️ [INTERCEPTOR] Nenhum token válido encontrado');
          console.log('⚠️ [INTERCEPTOR] Enviando requisição SEM autenticação');
          return next.handle(req);
        }
      })
    );
  }
}