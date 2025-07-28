import { Component, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-root',
  templateUrl: './app.html', 
  styleUrls: ['./app.css'],
  standalone: false 
})
export class AppComponent implements OnInit {
  title = 'Controle Acadêmico Web'; 
  isAuthenticated = false;
  userData: any; 

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