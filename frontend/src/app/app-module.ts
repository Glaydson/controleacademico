// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';
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
import { SemestresComponent } from './semestres/semestres';
import { DisciplinasComponent } from './disciplinas/disciplinas';
import { HomeComponent } from './home/home';
import { LayoutComponent } from './layout/layout';
import { DebugComponent } from './debug/debug';
import { MatrizCurricularComponent } from './matriz-curricular/matriz-curricular';

@NgModule({
  imports: [
    BrowserAnimationsModule, // Must be first for animations to work
    BrowserModule,
    CommonModule,
    AppRoutingModule,
    HttpClientModule, // Ainda funciona, apenas deprecated
    FormsModule,
    ToastrModule.forRoot({
      positionClass: 'toast-top-right',
      timeOut: 4000,
      closeButton: true,
      progressBar: true,
      preventDuplicates: true,
    }),
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
    SemestresComponent,
    DisciplinasComponent,
    HomeComponent,
    LayoutComponent,
  DebugComponent,
  MatrizCurricularComponent
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
   constructor() {
    console.log('🏗️ [APP-MODULE] Interceptor registrado!');
  }
 }