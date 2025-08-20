import { Disciplina } from './disciplina.model';

export interface PeriodoMatriz {
  numero: number;
  disciplinas: Disciplina[];
}

export interface MatrizCurricular {
  id: number;
  cursoId: number;
  cursoNome: string;
  periodos: PeriodoMatriz[];
}

export interface CreatePeriodoMatrizRequest {
  numero: number;
  disciplinaIds: number[];
}

export interface CreateMatrizCurricularRequest {
  cursoId: number;
  periodos: CreatePeriodoMatrizRequest[];
}

export interface UpdateMatrizCurricularRequest extends CreateMatrizCurricularRequest {}
