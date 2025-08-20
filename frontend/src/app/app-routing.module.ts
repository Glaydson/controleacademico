import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { LayoutComponent } from './layout/layout';
import { RoleGuard } from './guards/role.guard'; 
import { AuthCheckerGuard } from './guards/auth-checker.guard'; 

import { GerenciarUsuariosComponent } from './gerenciar-usuarios/gerenciar-usuarios';
import { GerenciarPedagogicoComponent } from './gerenciar-pedagogico/gerenciar-pedagogico';
import { MontarMatrizComponent } from './montar-matriz/montar-matriz';
import { VisualizarMatrizComponent } from './visualizar-matriz/visualizar-matriz';
import { DebugComponent } from './debug/debug';

// NOVOS COMPONENTES para o CRUD
import { CursosComponent } from './cursos/cursos'; // Vamos criar este
import { SemestresComponent } from './semestres/semestres'; // Implementado
import { DisciplinasComponent } from './disciplinas/disciplinas'; // Implementado
import { MatrizCurricularComponent } from './matriz-curricular/matriz-curricular';


const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  {
   path: '',
    component: LayoutComponent, 
    canActivate: [AuthCheckerGuard], 
    children: [
      { path: 'home', component: HomeComponent },

      // Rotas protegidas por RoleGuard (usando roles em MAIÚSCULAS para consistência)
      {
        path: 'gerenciar-usuarios',
        component: GerenciarUsuariosComponent,
        canActivate: [RoleGuard],
        data: { roles: ['ADMIN'] } // Apenas usuários com a role 'ADMIN'
      },
      {
        path: 'gerenciar-pedagogico',
        component: GerenciarPedagogicoComponent,
        canActivate: [RoleGuard],
        data: { roles: ['COORDENADOR'] },
        children: [
          { path: '', redirectTo: 'cursos', pathMatch: 'full' },
          { path: 'cursos', component: CursosComponent },
          { path: 'semestres', component: SemestresComponent },
          { path: 'disciplinas', component: DisciplinasComponent },
          { path: 'matriz-curricular', component: MatrizCurricularComponent },
        ]
      },
      {
        path: 'montar-matriz',
        component: MontarMatrizComponent,
        canActivate: [RoleGuard],
        data: { roles: ['COORDENADOR'] }
      },
      {
        path: 'visualizar-matriz',
        component: VisualizarMatrizComponent,
        canActivate: [RoleGuard],
        data: { roles: ['COORDENADOR', 'PROFESSOR', 'ALUNO'] }
      },
  // (rotas removidas, agora são filhas de gerenciar-pedagogico)
      {
        path: 'debug',
        component: DebugComponent // Debug route accessible to all authenticated users
      },
      
    ]
  },

  // Rota wildcard para páginas não encontradas
  { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }