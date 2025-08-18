export interface Curso {
  id: number;
  nome: string;
  codigo: string;
}

export interface Disciplina {
  id: number;
  nome: string;
  codigo: string;
  curso: Curso;
}

export interface CreateDisciplinaRequest {
  nome: string;
  codigo: string;
  cursoId: number;
}

export interface UpdateDisciplinaRequest {
  nome: string;
  codigo: string;
  cursoId: number;
}
