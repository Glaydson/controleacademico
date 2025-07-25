// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AuthModule, LogLevel } from 'angular-auth-oidc-client';
import { CommonModule } from '@angular/common'; // Já está aqui
import { environment } from '../environments/environment';
import { AppRoutingModule } from './app-routing.module'; // Já está aqui

// Importe os componentes standalone que serão usados diretamente no AppModule
import { AppComponent } from './app'; // Importe o AppComponent standalone
import { AuthCheckerGuard } from './guards/auth-checker.guard'; // Seu novo AuthCheckerGuard
import { RoleGuard } from './guards/role.guard'; // Seu RoleGuard

// Importe os outros componentes que são declarados no AppModule, se não forem standalone
import { GerenciarUsuariosComponent } from './gerenciar-usuarios/gerenciar-usuarios'; // Renomeei para GerenciarUsuariosComponent
import { GerenciarPedagogicoComponent } from './gerenciar-pedagogico/gerenciar-pedagogico'; // Renomeei
import { MontarMatrizComponent } from './montar-matriz/montar-matriz'; // Renomeei
import { VisualizarMatrizComponent } from './visualizar-matriz/visualizar-matriz'; // Renomeei

@NgModule({
   imports: [
    BrowserModule,
    CommonModule, // Mantenha para uso geral no módulo
    AppRoutingModule, // Contém o RouterModule.forRoot
    AuthModule.forRoot({
      config: environment.oidcConfig
    }),
    // Se AppComponent for standalone, você o importa aqui:
    AppComponent // <-- Importe o AppComponent se ele for standalone
  ],
  providers: [
    AuthCheckerGuard, // Mantenha os guards nos providers
    RoleGuard
  ],
  // Remova AppComponent, HomeComponent, LayoutComponent das declarações se forem standalone
  declarations: [
    GerenciarUsuariosComponent, // Mantenha estes se NÃO forem standalone
    GerenciarPedagogicoComponent,
    MontarMatrizComponent,
    VisualizarMatrizComponent
  ],
  bootstrap: [AppComponent] // O componente raiz para iniciar a aplicação
})
export class AppModule { }