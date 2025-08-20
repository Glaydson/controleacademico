import { Component, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { MatrizCurricularService } from '../services/matriz-curricular.service';
import { CursoService } from '../services/curso';
import { DisciplinaService } from '../services/disciplina.service';
import { MatrizCurricular, CreateMatrizCurricularRequest, UpdateMatrizCurricularRequest, PeriodoMatriz } from '../models/matriz-curricular.model';
import { Curso } from '../models/curso.model';
import { Disciplina } from '../models/disciplina.model';

@Component({
  selector: 'app-matriz-curricular',
  templateUrl: './matriz-curricular.html',
  styleUrls: ['./matriz-curricular.css'],
  standalone: false
})
export class MatrizCurricularComponent implements OnInit {
  matrizes: MatrizCurricular[] = [];
  cursos: Curso[] = [];
  disciplinas: Disciplina[] = [];
  selectedMatriz: MatrizCurricular | null = null;
  isLoading = false;
  isCreating = false;
  isEditing = false;
  errorMessage = '';
  successMessage = '';
  userRoles: string[] = [];

  // Form data
  formData: CreateMatrizCurricularRequest = {
    cursoId: 0,
    periodos: []
  };

  constructor(
    private matrizService: MatrizCurricularService,
    private cursoService: CursoService,
    private disciplinaService: DisciplinaService,
    private oidcSecurityService: OidcSecurityService
  ) {}

  ngOnInit(): void {
    this.oidcSecurityService.userData$.subscribe(({ userData }) => {
      this.userRoles = userData?.realm_access?.roles || [];
    });
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;
    this.matrizService.getMatrizes().subscribe({
      next: (matrizes) => {
        this.matrizes = matrizes;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar matrizes curriculares.';
        this.isLoading = false;
      }
    });
    this.cursoService.getCursos().subscribe({
      next: (cursos) => this.cursos = cursos,
      error: () => this.errorMessage = 'Erro ao carregar cursos.'
    });
    this.disciplinaService.getDisciplinas().subscribe({
      next: (disciplinas) => this.disciplinas = disciplinas,
      error: () => this.errorMessage = 'Erro ao carregar disciplinas.'
    });
  }

  canEdit(): boolean {
    return this.userRoles.includes('COORDENADOR');
  }

  canView(): boolean {
    return this.userRoles.includes('COORDENADOR') || this.userRoles.includes('PROFESSOR') || this.userRoles.includes('ALUNO');
  }

  startCreating(): void {
    this.isCreating = true;
    this.isEditing = false;
    this.selectedMatriz = null;
    this.formData = { cursoId: 0, periodos: [] };
    this.clearMessages();
  }

  startEditing(matriz: MatrizCurricular): void {
    this.isEditing = true;
    this.isCreating = false;
    this.selectedMatriz = matriz;
    this.formData = {
      cursoId: matriz.cursoId,
      periodos: matriz.periodos.map(p => ({
        numero: p.numero,
        disciplinaIds: p.disciplinas.map(d => d.id)
      }))
    };
    this.clearMessages();
  }

  viewMatriz(matriz: MatrizCurricular): void {
    this.selectedMatriz = matriz;
    this.isCreating = false;
    this.isEditing = false;
    this.clearMessages();
  }

  cancel(): void {
    this.isCreating = false;
    this.isEditing = false;
    this.selectedMatriz = null;
    this.clearMessages();
  }

  createMatriz(): void {
    this.isLoading = true;
    this.matrizService.createMatriz(this.formData).subscribe({
      next: (matriz) => {
        this.successMessage = 'Matriz curricular criada com sucesso!';
        this.isLoading = false;
        this.isCreating = false;
        this.loadAll();
      },
      error: () => {
        this.errorMessage = 'Erro ao criar matriz curricular.';
        this.isLoading = false;
      }
    });
  }

  updateMatriz(): void {
    if (!this.selectedMatriz) return;
    this.isLoading = true;
    this.matrizService.updateMatriz(this.selectedMatriz.id, this.formData).subscribe({
      next: () => {
        this.successMessage = 'Matriz curricular atualizada com sucesso!';
        this.isLoading = false;
        this.isEditing = false;
        this.selectedMatriz = null;
        this.loadAll();
      },
      error: () => {
        this.errorMessage = 'Erro ao atualizar matriz curricular.';
        this.isLoading = false;
      }
    });
  }

  deleteMatriz(matriz: MatrizCurricular): void {
    if (!confirm('Tem certeza que deseja excluir esta matriz curricular?')) return;
    this.isLoading = true;
    this.matrizService.deleteMatriz(matriz.id).subscribe({
      next: () => {
        this.successMessage = 'Matriz curricular excluída com sucesso!';
        this.isLoading = false;
        this.loadAll();
      },
      error: () => {
        this.errorMessage = 'Erro ao excluir matriz curricular.';
        this.isLoading = false;
      }
    });
  }

  addPeriodo(): void {
    const nextNumero = (this.formData.periodos.length > 0)
      ? Math.max(...this.formData.periodos.map(p => p.numero)) + 1
      : 1;
    this.formData.periodos.push({ numero: nextNumero, disciplinaIds: [] });
  }

  removePeriodo(index: number): void {
    this.formData.periodos.splice(index, 1);
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  getCursoNomeById(id: number): string {
    return this.cursos.find(c => c.id === id)?.nome || '';
  }

  getDisciplinaNomeById(id: number): string {
    return this.disciplinas.find(d => d.id === id)?.nome || '';
  }
}
