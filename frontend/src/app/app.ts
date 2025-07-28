import { Component, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-root',
  templateUrl: './app.html', // <-- MUDANÇA CRÍTICA: Aponta para o arquivo HTML
  styleUrls: ['./app.css'],
  standalone: false // Se você tiver um arquivo CSS para o app.component
})
export class AppComponent implements OnInit {
  title = 'Controle Acadêmico Web'; // Adicione a propriedade title se for usada no HTML
  isAuthenticated = false;
  userData: any; // Para exibir os dados do usuário no app.html

  constructor(public oidcSecurityService: OidcSecurityService) {}

  ngOnInit() {
  this.oidcSecurityService.checkAuth().subscribe({
    next: ({ isAuthenticated, userData }) => {
      if (!isAuthenticated) {
        console.log('Redirecionando para login...');
        this.oidcSecurityService.authorize();
      } else {
        console.log('Usuário autenticado:', userData);
      }
    },
    error: (err) => {
      console.error('Erro na verificação de autenticação:', err);
      this.oidcSecurityService.logoff();
    }
  });
}
  login() {
    this.oidcSecurityService.authorize(); // Inicia o fluxo de login
  }

  logout() {
    this.oidcSecurityService.logoff(); // Inicia o fluxo de logout
  }
}