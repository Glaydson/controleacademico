import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usuario, CreateUsuarioRequest, UpdateUsuarioRequest } from '../models/usuario.model';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private apiUrl = '/api/users'; // Use relative URL that goes through nginx proxy (matches backend endpoint)

  constructor(private http: HttpClient) { 
    console.log('🔧 [USUARIO-SERVICE] Inicializado com URL:', this.apiUrl);
  }

  // Obter todos os usuários (o interceptor vai adicionar o token automaticamente)
  getUsuarios(): Observable<Usuario[]> {
    console.log('📋 [USUARIO-SERVICE] Fazendo requisição GET para:', this.apiUrl);
    console.log('📋 [USUARIO-SERVICE] Full URL will be:', window.location.origin + this.apiUrl);
    return this.http.get<Usuario[]>(this.apiUrl);
  }

  getUsuarioById(id: string): Observable<Usuario> {
    console.log('🔍 UsuarioService: Buscando usuário por ID:', id);
    return this.http.get<Usuario>(`${this.apiUrl}/${id}`);
  }

  createUsuario(usuario: CreateUsuarioRequest): Observable<Usuario> {
    console.log('➕ UsuarioService: Criando usuário:', usuario);
    return this.http.post<Usuario>(this.apiUrl, usuario);
  }

  updateUsuario(id: string, usuario: Partial<UpdateUsuarioRequest>): Observable<any> {
    const url = `${this.apiUrl}/${id}`;
    console.log('✏️ UsuarioService: Atualizando usuário:', id, usuario);
    console.log('✏️ UsuarioService: PUT URL:', url);
    console.log('✏️ UsuarioService: Full URL will be:', window.location.origin + url);
    console.log('✏️ UsuarioService: Request body:', JSON.stringify(usuario, null, 2));
    console.log('✏️ UsuarioService: ID being used:', id, 'Type:', typeof id);
    return this.http.put<any>(url, usuario);
  }

  // Alternative method to update by matricula if ID doesn't work
  updateUsuarioByMatricula(matricula: string, usuario: Partial<UpdateUsuarioRequest>): Observable<any> {
    const url = `${this.apiUrl}/matricula/${matricula}`;
    console.log('✏️ UsuarioService: Atualizando usuário por matrícula:', matricula, usuario);
    return this.http.put<any>(url, usuario);
  }

  deleteUsuario(id: string): Observable<void> {
    console.log('🗑️ UsuarioService: Deletando usuário:', id);
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
