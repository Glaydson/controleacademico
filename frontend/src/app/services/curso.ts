import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Curso } from '../models/curso.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CursoService {
  private apiUrl = '/api/cursos'; // Use relative URL that goes through nginx proxy

  constructor(private http: HttpClient) { 
    console.log('🔧 [CURSO-SERVICE] Inicializado com URL:', this.apiUrl);
  }

  // Obter todos os cursos (o interceptor vai adicionar o token automaticamente)
  getCursos(): Observable<Curso[]> {
    console.log('📋 [CURSO-SERVICE] Fazendo requisição GET para:', this.apiUrl);
    return this.http.get<Curso[]>(this.apiUrl);
  }

  getCursoById(id: number): Observable<Curso> {
    console.log('🔍 CursoService: Buscando curso por ID:', id);
    return this.http.get<Curso>(`${this.apiUrl}/${id}`);
  }

  createCurso(curso: Curso): Observable<Curso> {
    console.log('➕ CursoService: Criando curso:', curso);
    return this.http.post<Curso>(this.apiUrl, curso);
  }

  updateCurso(id: number, curso: Curso): Observable<Curso> {
    console.log('✏️ CursoService: Atualizando curso:', id, curso);
    return this.http.put<Curso>(`${this.apiUrl}/${id}`, curso);
  }

  deleteCurso(id: number): Observable<void> {
    console.log('🗑️ CursoService: Deletando curso:', id);
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}