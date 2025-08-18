import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Semestre, CreateSemestreRequest, UpdateSemestreRequest } from '../models/semestre.model';

@Injectable({
  providedIn: 'root'
})
export class SemestreService {
  private apiUrl = '/api/semestres'; // Use relative URL that goes through nginx proxy

  constructor(private http: HttpClient) { 
    console.log('🔧 [SEMESTRE-SERVICE] Inicializado com URL:', this.apiUrl);
  }

  // Obter todos os semestres
  getSemestres(): Observable<Semestre[]> {
    console.log('📋 [SEMESTRE-SERVICE] Fazendo requisição GET para:', this.apiUrl);
    return this.http.get<Semestre[]>(this.apiUrl);
  }

  // Obter semestre por ID
  getSemestreById(id: number): Observable<Semestre> {
    console.log('🔍 [SEMESTRE-SERVICE] Buscando semestre por ID:', id);
    return this.http.get<Semestre>(`${this.apiUrl}/${id}`);
  }

  // Obter semestre por ano e período
  getSemestreByAnoPeriodo(ano: number, periodo: string): Observable<Semestre> {
    console.log('🔍 [SEMESTRE-SERVICE] Buscando semestre por ano/período:', ano, periodo);
    return this.http.get<Semestre>(`${this.apiUrl}/ano/${ano}/periodo/${periodo}`);
  }

  // Criar novo semestre
  createSemestre(semestre: CreateSemestreRequest): Observable<Semestre> {
    console.log('➕ [SEMESTRE-SERVICE] Criando semestre:', semestre);
    return this.http.post<Semestre>(this.apiUrl, semestre);
  }

  // Atualizar semestre
  updateSemestre(id: number, semestre: UpdateSemestreRequest): Observable<Semestre> {
    console.log('✏️ [SEMESTRE-SERVICE] Atualizando semestre:', id, semestre);
    return this.http.put<Semestre>(`${this.apiUrl}/${id}`, semestre);
  }

  // Deletar semestre
  deleteSemestre(id: number): Observable<void> {
    console.log('🗑️ [SEMESTRE-SERVICE] Deletando semestre:', id);
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
