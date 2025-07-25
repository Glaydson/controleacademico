import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { LayoutComponent } from './layout/layout';
import { RoleGuard } from './guards/role.guard'; // Importe o RoleGuard
import { AuthCheckerGuard } from './guards/auth-checker.guard'; // <-- IMPORTE O NOVO GUARD

// Importe seus novos componentes
import { GerenciarUsuariosComponent } from './gerenciar-usuarios/gerenciar-usuarios';
import { GerenciarPedagogicoComponent } from './gerenciar-pedagogico/gerenciar-pedagogico';
import { MontarMatrizComponent } from './montar-matriz/montar-matriz';
import { VisualizarMatrizComponent } from './visualizar-matriz/visualizar-matriz';

const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  {
   path: '',
    component: LayoutComponent, // O LayoutComponent será renderizado
    canActivate: [AuthCheckerGuard], // Garante que o usuário esteja autenticado para qualquer rota sob o layout
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
        data: { roles: ['COORDENADOR'] } // Apenas usuários com a role 'COORDENADOR'
      },
      {
        path: 'montar-matriz',
        component: MontarMatrizComponent,
        canActivate: [RoleGuard],
        data: { roles: ['COORDENADOR'] } // Apenas usuários com a role 'COORDENADOR'
      },
      {
        path: 'visualizar-matriz',
        component: VisualizarMatrizComponent,
        canActivate: [RoleGuard],
        data: { roles: ['COORDENADOR', 'PROFESSOR', 'ALUNO'] } // Coordenador, Professor, Aluno
      },
      // Adicione outras rotas aqui conforme necessário
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