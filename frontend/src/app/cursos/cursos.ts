import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CursoService } from '../services/curso'; // Importe o serviço
import { Curso } from '../models/curso.model'; // Importe o modelo
import { OidcSecurityService } from 'angular-auth-oidc-client'; // ADICIONE ESTA IMPORTAÇÃO

@Component({
  selector: 'app-cursos',
  templateUrl: './cursos.html',
  styleUrls: ['./cursos.css'],
  standalone: false
})
export class CursosComponent implements OnInit {
  cursos: Curso[] = [];
  novoCurso: Curso = { nome: '', codigo: '' };
  cursoEmEdicao: Curso | null = null;

  constructor(
    private cursoService: CursoService,
    private oidcSecurityService: OidcSecurityService // ADICIONE ESTA INJEÇÃO
  ) { }

  ngOnInit(): void {
    this.carregarCursos();
  }

    carregarCursos(): void {
    // DEBUG: Verificar token manualmente
    this.oidcSecurityService.getAccessToken().subscribe(token => {
      console.log('=== DEBUG TOKEN ===');
      console.log('Token presente:', !!token);
      if (token) {
        console.log('Token (primeiros 100 chars):', token.substring(0, 100));
        try {
          const decoded = JSON.parse(atob(token.split('.')[1]));
          console.log('Token decodificado - sub:', decoded.sub);
          console.log('Token decodificado - roles:', decoded.realm_access?.roles);
          console.log('Token decodificado - exp:', new Date(decoded.exp * 1000));
          console.log('Token válido?', decoded.exp * 1000 > Date.now());
        } catch (e) {
          console.error('Erro ao decodificar token:', e);
        }
      }
      console.log('=== FIM DEBUG TOKEN ===');
    });

    // Seu código original aqui...
    this.cursoService.getCursos().subscribe({
      next: (data) => {
        this.cursos = data;
      },
      error: (error) => {
        console.error('Erro ao carregar cursos:', error);
        alert('Erro ao carregar cursos. Verifique o console.');
      }
    });
  }

  criarCurso(): void {
    if (this.novoCurso.nome && this.novoCurso.codigo) {
      this.cursoService.createCurso(this.novoCurso).subscribe({
        next: (cursoCriado) => {
          this.cursos.push(cursoCriado);
          this.novoCurso = { nome: '', codigo: '' }; // Limpa o formulário
          alert('Curso criado com sucesso!');
        },
        error: (error) => {
          console.error('Erro ao criar curso:', error);
          alert('Erro ao criar curso. Verifique o console.');
        }
      });
    } else {
      alert('Por favor, preencha todos os campos do novo curso.');
    }
  }

  iniciarEdicao(curso: Curso): void {
    this.cursoEmEdicao = { ...curso }; // Cria uma cópia para edição
  }

  salvarEdicao(): void {
    if (this.cursoEmEdicao && this.cursoEmEdicao.id && this.cursoEmEdicao.nome && this.cursoEmEdicao.codigo) {
      this.cursoService.updateCurso(this.cursoEmEdicao.id, this.cursoEmEdicao).subscribe({
        next: (cursoAtualizado) => {
          const index = this.cursos.findIndex(c => c.id === cursoAtualizado.id);
          if (index !== -1) {
            this.cursos[index] = cursoAtualizado;
          }
          this.cursoEmEdicao = null;
          alert('Curso atualizado com sucesso!');
        },
        error: (error) => {
          console.error('Erro ao atualizar curso:', error);
          alert('Erro ao atualizar curso. Verifique o console.');
        }
      });
    } else {
      alert('Por favor, preencha todos os campos para edição.');
    }
  }

  cancelarEdicao(): void {
    this.cursoEmEdicao = null;
  }

  removerCurso(id: number | undefined): void {
    if (id === undefined) {
      alert('ID do curso não fornecido para remoção.');
      return;
    }
    if (confirm('Tem certeza que deseja remover este curso?')) {
      this.cursoService.deleteCurso(id).subscribe({
        next: () => {
          this.cursos = this.cursos.filter(curso => curso.id !== id);
          alert('Curso removido com sucesso!');
        },
        error: (error) => {
          console.error('Erro ao remover curso:', error);
          alert('Erro ao remover curso. Verifique o console.');
        }
      });
    }
  }

  debugToken() {
  this.oidcSecurityService.getAccessToken().subscribe(token => {
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('=== TOKEN PAYLOAD DEBUG ===');
        console.log('Subject (sub):', payload.sub);
        console.log('Issuer (iss):', payload.iss);
        console.log('Audience (aud):', payload.aud);
        console.log('Client ID (azp):', payload.azp);
        console.log('Realm Access:', payload.realm_access);
        console.log('Resource Access:', payload.resource_access);
        console.log('Scopes:', payload.scope);
        console.log('Expiration:', new Date(payload.exp * 1000));
        console.log('===========================');
      } catch (e) {
        console.error('Erro ao decodificar token:', e);
      }
    }
  });
}
}