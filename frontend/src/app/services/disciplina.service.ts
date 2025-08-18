import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Disciplina, CreateDisciplinaRequest, UpdateDisciplinaRequest } from '../models/disciplina.model';

@Injectable({
  providedIn: 'root'
})
export class DisciplinaService {
  private apiUrl = `${environment.apiUrl}/disciplinas`;

  constructor(private http: HttpClient) {}

  getDisciplinas(): Observable<Disciplina[]> {
    console.log('📋 [DISCIPLINA-SERVICE] Buscando todas as disciplinas');
    return this.http.get<Disciplina[]>(this.apiUrl);
  }

  getDisciplinaById(id: number): Observable<Disciplina> {
    console.log('🔍 [DISCIPLINA-SERVICE] Buscando disciplina por ID:', id);
    return this.http.get<Disciplina>(`${this.apiUrl}/${id}`);
  }

  getDisciplinaByCodigo(codigo: string): Observable<Disciplina> {
    console.log('🔍 [DISCIPLINA-SERVICE] Buscando disciplina por código:', codigo);
    return this.http.get<Disciplina>(`${this.apiUrl}/codigo/${codigo}`);
  }

  createDisciplina(disciplina: CreateDisciplinaRequest): Observable<Disciplina> {
    console.log('➕ [DISCIPLINA-SERVICE] Criando disciplina:', disciplina);
    return this.http.post<Disciplina>(this.apiUrl, disciplina);
  }

  updateDisciplina(id: number, disciplina: UpdateDisciplinaRequest): Observable<Disciplina> {
    console.log('✏️ [DISCIPLINA-SERVICE] Atualizando disciplina:', id, disciplina);
    return this.http.put<Disciplina>(`${this.apiUrl}/${id}`, disciplina);
  }

  deleteDisciplina(id: number): Observable<void> {
    console.log('🗑️ [DISCIPLINA-SERVICE] Deletando disciplina:', id);
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
