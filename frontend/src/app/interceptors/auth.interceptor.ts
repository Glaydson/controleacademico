import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { switchMap, catchError, filter, take } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

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
        
        if (error.status === 401) {
          console.log('🔄 [INTERCEPTOR] Token expirado (401), tentando renovar...');
          return this.handle401Error(req, next);
        }
        
        if (error.status === 403) {
          console.log('🚫 [INTERCEPTOR] Acesso negado (403) - usuário não autorizado');
        }
        
        return throwError(() => error);
      })
    );
  }

  private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      console.log('🔄 [INTERCEPTOR] Iniciando renovação de token...');
      
      return this.oidcSecurityService.forceRefreshSession().pipe(
        switchMap((result: any) => {
          this.isRefreshing = false;
          
          if (result && result.isAuthenticated) {
            console.log('✅ [INTERCEPTOR] Token renovado com sucesso');
            
            return this.oidcSecurityService.getAccessToken().pipe(
              switchMap(newToken => {
                this.refreshTokenSubject.next(newToken);
                const authReq = request.clone({
                  headers: request.headers.set('Authorization', `Bearer ${newToken}`)
                });
                return next.handle(authReq);
              })
            );
          } else {
            console.log('❌ [INTERCEPTOR] Falha na renovação do token, redirecionando para login');
            this.isRefreshing = false;
            this.oidcSecurityService.authorize();
            return throwError(() => new Error('Token renewal failed'));
          }
        }),
        catchError((error) => {
          this.isRefreshing = false;
          console.error('❌ [INTERCEPTOR] Erro durante renovação do token:', error);
          this.oidcSecurityService.authorize();
          return throwError(() => error);
        })
      );
    } else {
      // Token refresh is already in progress, wait for it to complete
      console.log('⏳ [INTERCEPTOR] Aguardando renovação de token em andamento...');
      return this.refreshTokenSubject.pipe(
        filter(token => token != null),
        take(1),
        switchMap(token => {
          const authReq = request.clone({
            headers: request.headers.set('Authorization', `Bearer ${token}`)
          });
          return next.handle(authReq);
        })
      );
    }
  }
}