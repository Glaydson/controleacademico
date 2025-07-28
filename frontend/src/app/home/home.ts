import { Component, OnInit } from '@angular/core';
import { OidcSecurityService, UserDataResult } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
  standalone: false
})
export class HomeComponent implements OnInit {
  userData: any; // Tipo para os dados do usuário

  constructor(private oidcSecurityService: OidcSecurityService) { }

  ngOnInit(): void {
    this.oidcSecurityService.userData$.subscribe((userDataResult: UserDataResult) => {
      if (userDataResult.userData) {
        this.userData = userDataResult.userData;
      }
    });
  }
}
