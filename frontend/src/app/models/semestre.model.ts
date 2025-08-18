export interface Semestre {
  id?: number;
  ano: number;
  periodo: string;
  descricao?: string; // Campo derivado (ex: "2024.1")
}

export interface CreateSemestreRequest {
  ano: number;
  periodo: string;
}

export interface UpdateSemestreRequest {
  id: number;
  ano: number;
  periodo: string;
}
