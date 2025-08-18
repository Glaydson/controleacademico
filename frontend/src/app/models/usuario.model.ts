export interface Usuario {
  id?: string; // Keycloak ID is string, not number
  nome: string;
  email: string;
  matricula: string;
  password?: string;
  role: 'COORDENADOR' | 'PROFESSOR' | 'ALUNO';
  enabled?: boolean; // Backend includes this field
  cursoId?: number; // For ALUNO and COORDENADOR
  cursoNome?: string; // For display purposes
  disciplinaIds?: number[]; // For PROFESSOR - converted from Set to Array
  disciplinaNomes?: string[]; // For display purposes - converted from Set to Array
}

export interface CreateUsuarioRequest {
  nome: string;
  email: string;
  matricula: string;
  password: string;
  role: 'COORDENADOR' | 'PROFESSOR' | 'ALUNO';
  cursoId?: number; // For ALUNO and COORDENADOR
  disciplinaIds?: number[]; // For PROFESSOR
}

export interface UpdateUsuarioRequest {
  id: string; // Keycloak ID is string
  nome: string;
  email: string;
  matricula: string;
  role: 'COORDENADOR' | 'PROFESSOR' | 'ALUNO';
  password?: string; // Optional for updates
  cursoId?: number; // For ALUNO and COORDENADOR
  disciplinaIds?: number[]; // For PROFESSOR
}
