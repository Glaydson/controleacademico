import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { switchMap, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private oidcSecurityService: OidcSecurityService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Lista de URLs que NÃO devem ser interceptadas
    const skipUrls = [
      '/realms/controle-academico',
      '/realms/',
      '/.well-known/',
      '/oauth2/',
      '/token',
      '/swagger-ui',
      '/openapi',
      '/q/health',
      '/q/metrics'
    ];
    
    // Verifica se a URL é o callback do OIDC para evitar loops
    const isOidcCallback = req.url.includes('?code=') && req.url.includes('&state=');

    // Verifica se a URL deve ser ignorada
    const shouldSkip = isOidcCallback || skipUrls.some(skipUrl => req.url.includes(skipUrl));
    
    if (shouldSkip) {
      console.log('⏭️ [INTERCEPTOR] Pulando interceptação para:', req.url);
      return next.handle(req);
    }

    // Só intercepta chamadas para a API do backend
    if (!req.url.includes(environment.apiUrl)) {
      console.log('⏭️ [INTERCEPTOR] URL não é da API, pulando:', req.url);
      return next.handle(req);
    }

    console.log('🚀 [INTERCEPTOR] Interceptando requisição para API:', req.url);
    
    return this.oidcSecurityService.getAccessToken().pipe(
      switchMap(token => {
        if (token && token.length > 0) {
          console.log('🔑 [INTERCEPTOR] Token válido encontrado');
          console.log('🔑 [INTERCEPTOR] Token preview:', token.substring(0, 50) + '...');
          
          const authReq = req.clone({
            headers: req.headers.set('Authorization', `Bearer ${token}`)
          });
          
          console.log('✅ [INTERCEPTOR] Header Authorization adicionado para:', req.url);
          console.log('✅ [INTERCEPTOR] Request method:', req.method);
          console.log('✅ [INTERCEPTOR] Request headers:', authReq.headers.keys());
          return next.handle(authReq);
          
        } else {
          console.log('⚠️ [INTERCEPTOR] Token não encontrado ou inválido');
          
          // Se não há token, verifica se o usuário está autenticado
          return this.oidcSecurityService.isAuthenticated$.pipe(
            switchMap((authResult) => {
              if (!authResult.isAuthenticated) {
                console.log('🚪 [INTERCEPTOR] Usuário não autenticado, mas não forçando reautenticação');
                // Don't call authorize() as it triggers new auth flow
                // Just return an error and let the app handle it
                return throwError(() => new Error('Usuário não autenticado'));
              }
              
              // Se autenticado mas sem token, envia sem autenticação
              console.log('⚠️ [INTERCEPTOR] Enviando sem token (usuário autenticado)');
              return next.handle(req);
            })
          );
        }
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('❌ [INTERCEPTOR] Erro na requisição:', error);
        
        // Don't automatically trigger reauth on errors
        // Let the application handle authentication failures
        if (error.status === 401 || error.status === 403) {
          console.log('🔄 [INTERCEPTOR] Erro de autorização detectado, mas não forçando reautenticação automática');
        }
        
        return throwError(() => error);
      })
    );
  }
}