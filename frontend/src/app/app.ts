// src/app/app.component.ts
import { Component, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common'; // Adicione CommonModule para *ngIf e json pipe se for usar no app.html

@Component({
  selector: 'app-root',
  templateUrl: './app.html', // <-- MUDANÇA CRÍTICA: Aponta para o arquivo HTML
  styleUrls: ['./app.css'], // Se você tiver um arquivo CSS para o app.component
  standalone: true, // <-- Adicione esta linha se a intenção é que seja Standalone
  imports: [RouterModule, CommonModule] // <-- Mantenha RouterModule e adicione CommonModule
})
export class AppComponent implements OnInit {
  title = 'Controle Acadêmico Web'; // Adicione a propriedade title se for usada no HTML
  isAuthenticated = false;
  userData: any; // Para exibir os dados do usuário no app.html

  constructor(public oidcSecurityService: OidcSecurityService) {}

  ngOnInit() {
    this.oidcSecurityService.checkAuth().subscribe(({ isAuthenticated, userData, errorMessage }) => {
      this.isAuthenticated = isAuthenticated;
      this.userData = userData; // Atribua os dados do usuário
      console.log('App Component - Auth Status:', isAuthenticated);
      if (errorMessage) {
        console.error('Auth Error:', errorMessage);
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