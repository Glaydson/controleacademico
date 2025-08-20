import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  MatrizCurricular,
  CreateMatrizCurricularRequest,
  UpdateMatrizCurricularRequest
} from '../models/matriz-curricular.model';

@Injectable({ providedIn: 'root' })
export class MatrizCurricularService {
  private apiUrl = `${environment.apiUrl}/matrizes-curriculares`;

  constructor(private http: HttpClient) {}

  getMatrizes(): Observable<MatrizCurricular[]> {
    console.log('[MatrizCurricularService] GET:', this.apiUrl);
    return this.http.get<MatrizCurricular[]>(this.apiUrl);
  }

  getMatrizById(id: number): Observable<MatrizCurricular> {
    const url = `${this.apiUrl}/${id}`;
    console.log('[MatrizCurricularService] GET:', url);
    return this.http.get<MatrizCurricular>(url);
  }

  createMatriz(matriz: CreateMatrizCurricularRequest): Observable<MatrizCurricular> {
    console.log('[MatrizCurricularService] POST:', this.apiUrl, matriz);
    return this.http.post<MatrizCurricular>(this.apiUrl, matriz);
  }

  updateMatriz(id: number, matriz: UpdateMatrizCurricularRequest): Observable<MatrizCurricular> {
    const url = `${this.apiUrl}/${id}`;
    console.log('[MatrizCurricularService] PUT:', url, matriz);
    return this.http.put<MatrizCurricular>(url, matriz);
  }

  deleteMatriz(id: number): Observable<void> {
    const url = `${this.apiUrl}/${id}`;
    console.log('[MatrizCurricularService] DELETE:', url);
    return this.http.delete<void>(url);
  }
}
