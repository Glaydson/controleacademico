import { Component, OnInit } from '@angular/core';
import { MatrizCurricularService } from '../services/matriz-curricular.service';
import { CursoService } from '../services/curso';
import { Curso } from '../models/curso.model';
import { MatrizCurricular } from '../models/matriz-curricular.model';

@Component({
  selector: 'app-visualizar-matriz',
  standalone: false,
  templateUrl: './visualizar-matriz.html',
  styleUrl: './visualizar-matriz.css'
})
export class VisualizarMatrizComponent implements OnInit {
  cursos: Curso[] = [];
  selectedCurso: Curso | null = null;
  matriz: MatrizCurricular | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    private matrizService: MatrizCurricularService,
    private cursoService: CursoService
  ) {}

  ngOnInit(): void {
    console.log('[VisualizarMatrizComponent] ngOnInit');
    this.loadCursos();
  }

  loadCursos(): void {
    this.cursoService.getCursos().subscribe({
      next: cursos => this.cursos = cursos,
      error: () => this.errorMessage = 'Erro ao carregar cursos.'
    });
  }

  onCursoChange(): void {
    this.errorMessage = '';
    if (this.selectedCurso) {
      this.isLoading = true;
      this.matrizService.getMatrizes().subscribe({
        next: matrizes => {
          const found = matrizes.find(m => m.cursoId === this.selectedCurso!.id);
          if (found) {
            this.matriz = found;
          } else {
            this.matriz = null;
          }
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Erro ao buscar matriz curricular.';
          this.isLoading = false;
        }
      });
    } else {
      this.matriz = null;
    }
  }
}
