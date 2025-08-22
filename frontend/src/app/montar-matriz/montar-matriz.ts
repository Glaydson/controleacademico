import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { MatrizCurricularService } from '../services/matriz-curricular.service';
import { CursoService } from '../services/curso';
import { DisciplinaService } from '../services/disciplina.service';
import { Curso } from '../models/curso.model';
import { Disciplina } from '../models/disciplina.model';
import {
  MatrizCurricular,
  CreateMatrizCurricularRequest,
  UpdateMatrizCurricularRequest,
  PeriodoMatriz
} from '../models/matriz-curricular.model';

@Component({
  selector: 'app-montar-matriz',
  standalone: false,
  templateUrl: './montar-matriz.html',
  styleUrl: './montar-matriz.css'
})
export class MontarMatrizComponent implements OnInit {

  // For discipline select per period
  selectedDisciplina: { [key: number]: Disciplina | null } = {};


  get filteredDisciplinas(): Disciplina[] {
    if (!this.selectedCurso) return [];
    return this.disciplinas.filter(d => d.curso && d.curso.id === this.selectedCurso!.id);
  }

  getAvailableDisciplinas(): Disciplina[] {
    if (!this.matriz) return [];
    // Collect all disciplina IDs already associated with any periodo
    const usedIds = new Set<number>();
    this.matriz.periodos.forEach(p => {
      p.disciplinas.forEach(d => usedIds.add(d.id));
    });
    return this.filteredDisciplinas.filter(d => !usedIds.has(d.id));
  }

  canSaveMatriz(): boolean {
    if (!this.matriz || this.isLoading) return false;
    if (!this.matriz.periodos || !this.matriz.periodos.length) return false;
    if (this.matriz.periodos.some((p: PeriodoMatriz) => !p.disciplinas || !p.disciplinas.length)) return false;
    return true;
  }

  isDisciplinaInPeriodo(periodo: PeriodoMatriz, disciplina: Disciplina): boolean {
    return !!periodo.disciplinas.find(d => d.id === disciplina.id);
  }
  cursos: Curso[] = [];
  disciplinas: Disciplina[] = [];
  selectedCurso: Curso | null = null;
  matriz: MatrizCurricular | null = null;
  isLoading = false;
  isEditing = false;
  // Remove old message variables

  constructor(
    private matrizService: MatrizCurricularService,
    private cursoService: CursoService,
    private disciplinaService: DisciplinaService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    console.log('[MontarMatrizComponent] ngOnInit');
    this.loadCursos();
    this.loadDisciplinas();
  }

  loadCursos(): void {
    this.cursoService.getCursos().subscribe({
      next: cursos => this.cursos = cursos,
      error: () => this.toastr.error('Erro ao carregar cursos.')
    });
  }

  loadDisciplinas(): void {
    this.disciplinaService.getDisciplinas().subscribe({
      next: disciplinas => this.disciplinas = disciplinas,
      error: () => this.toastr.error('Erro ao carregar disciplinas.')
    });
  }

  onCursoChange(): void {
  // Clear feedback (toasts now handle messages)
    if (this.selectedCurso) {
      this.isLoading = true;
      this.matrizService.getMatrizes().subscribe({
        next: matrizes => {
          const found = matrizes.find(m => m.cursoId === this.selectedCurso!.id);
          if (found) {
            this.matriz = found;
          } else {
            this.matriz = {
              id: 0,
              cursoId: this.selectedCurso?.id ?? 0,
              cursoNome: this.selectedCurso?.nome ?? '',
              periodos: []
            };
          }
          this.isLoading = false;
        },
        error: () => {
          this.toastr.error('Erro ao buscar matriz curricular.');
          this.isLoading = false;
        }
      });
    } else {
      // Always initialize an empty matriz when a curso is selected
      if (this.selectedCurso) {
        const curso = this.selectedCurso as Curso;
        this.matriz = {
          id: 0,
          cursoId: curso.id ?? 0,
          cursoNome: curso.nome ?? '',
          periodos: []
        };
      } else {
        this.matriz = null;
      }
    }
  }

  addPeriodo(): void {
    if (!this.matriz) return;
    const nextNumero = (this.matriz.periodos.length > 0)
      ? Math.max(...this.matriz.periodos.map(p => p.numero)) + 1
      : 1;
    this.matriz.periodos.push({ numero: nextNumero, disciplinas: [] });
  }

  removePeriodo(idx: number): void {
    if (!this.matriz) return;
    if (confirm('Tem certeza que deseja remover este período?')) {
      this.matriz.periodos.splice(idx, 1);
    }
  }

  addDisciplinaToPeriodo(periodo: PeriodoMatriz, disciplina: Disciplina | null): void {
    if (!disciplina) return;
    if (!periodo.disciplinas.find(d => d.id === disciplina.id)) {
      periodo.disciplinas.push(disciplina);
    }
  }

  removeDisciplinaFromPeriodo(periodo: PeriodoMatriz, disciplinaId: number): void {
    periodo.disciplinas = periodo.disciplinas.filter(d => d.id !== disciplinaId);
  }

  saveMatriz(): void {
    if (!this.matriz) return;
    this.isLoading = true;
    const req: CreateMatrizCurricularRequest = {
      cursoId: this.matriz.cursoId,
      periodos: this.matriz.periodos.map(p => ({
        numero: p.numero,
        disciplinaIds: p.disciplinas.map(d => d.id)
      }))
    };
    const extractErrorMessage = (error: any, fallback: string) => {
      let msg = fallback;
      if (error && error.error) {
        if (typeof error.error === 'string') {
          msg = error.error;
        } else if (error.error.message) {
          msg = error.error.message;
        }
      } else if (error && error.message) {
        msg = error.message;
      }
      // Replace disciplina IDs with names if present in the message
      if (msg && this.disciplinas && this.disciplinas.length > 0) {
        msg = msg.replace(/Disciplina(?:\s+ID)?\s*[:\[]?\s*(\d+)\]?/g, (match, id) => {
          const found = this.disciplinas.find(d => d.id === Number(id));
          if (found) {
            return `Disciplina: ${found.nome}`;
          }
          return match;
        });
        msg = msg.replace(/\[(\d+)\]/g, (match, id) => {
          const found = this.disciplinas.find(d => d.id === Number(id));
          if (found) {
            return `[${found.nome}]`;
          }
          return '';
        });
      }
      return msg;
    };
    if (this.matriz.id && this.matriz.id !== 0) {
      // Update
      this.matrizService.updateMatriz(this.matriz.id, req).subscribe({
        next: matriz => {
          this.matriz = matriz;
          this.toastr.success('Matriz curricular atualizada com sucesso!');
          this.isLoading = false;
        },
        error: (error) => {
          this.toastr.error(extractErrorMessage(error, 'Erro ao atualizar matriz curricular.'));
          this.isLoading = false;
        }
      });
    } else {
      // Create
      this.matrizService.createMatriz(req).subscribe({
        next: matriz => {
          this.matriz = matriz;
          this.toastr.success('Matriz curricular criada com sucesso!');
          this.isLoading = false;
        },
        error: (error) => {
          this.toastr.error(extractErrorMessage(error, 'Erro ao criar matriz curricular.'));
          this.isLoading = false;
        }
      });
    }
  }

  deleteMatriz(): void {
    if (!this.matriz || !this.matriz.id) return;
    if (!confirm('Tem certeza que deseja remover esta matriz curricular?')) return;
    this.isLoading = true;
    this.matrizService.deleteMatriz(this.matriz.id).subscribe({
      next: () => {
        this.toastr.success('Matriz curricular removida.');
        this.matriz = null;
        this.isLoading = false;
      },
      error: () => {
        this.toastr.error('Erro ao remover matriz curricular.');
        this.isLoading = false;
      }
    });
  }
}
