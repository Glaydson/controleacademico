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
  professor?: { id?: string; nome: string };
}

export interface CreateDisciplinaRequest {
  nome: string;
  codigo: string;
  cursoId: number;
  professorId?: string | null;
}

export interface UpdateDisciplinaRequest {
  nome: string;
  codigo: string;
  cursoId: number;
  professorId?: string | null;
}
