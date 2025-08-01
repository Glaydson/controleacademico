// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AuthModule } from 'angular-auth-oidc-client';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../environments/environment';
import { AppRoutingModule } from './app-routing.module';

// HTTP - Abordagem clássica que ainda funciona
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

// Guards
import { AuthCheckerGuard } from './guards/auth-checker.guard';
import { RoleGuard } from './guards/role.guard';

// Interceptor clássico
import { AuthInterceptor } from './interceptors/auth.interceptor';

// Componentes
import { AppComponent } from './app';
import { GerenciarUsuariosComponent } from './gerenciar-usuarios/gerenciar-usuarios';
import { GerenciarPedagogicoComponent } from './gerenciar-pedagogico/gerenciar-pedagogico';
import { MontarMatrizComponent } from './montar-matriz/montar-matriz';
import { VisualizarMatrizComponent } from './visualizar-matriz/visualizar-matriz';
import { CursosComponent } from './cursos/cursos';
import { HomeComponent } from './home/home';
import { LayoutComponent } from './layout/layout';

@NgModule({
  imports: [
    BrowserModule,
    CommonModule,
    AppRoutingModule,
    HttpClientModule, // Ainda funciona, apenas deprecated
    FormsModule,
    AuthModule.forRoot({
      config: environment.oidcConfig
    })
  ],
  providers: [
    AuthCheckerGuard,
    RoleGuard,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  declarations: [
    AppComponent,
    GerenciarUsuariosComponent,
    GerenciarPedagogicoComponent,
    MontarMatrizComponent,
    VisualizarMatrizComponent,
    CursosComponent,
    HomeComponent,
    LayoutComponent
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
   constructor() {
    console.log('🏗️ [APP-MODULE] Interceptor registrado!');
  }
 }