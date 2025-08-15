import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OidcSecurityService, AuthenticatedResult } from 'angular-auth-oidc-client';
import { Usuario, CreateUsuarioRequest, UpdateUsuarioRequest } from '../models/usuario.model';
import { UsuarioService } from '../services/usuario.service';
import { CursoService } from '../services/curso';
import { Curso } from '../models/curso.model';

@Component({
  selector: 'app-gerenciar-usuarios',
  standalone: false,
  templateUrl: './gerenciar-usuarios.html',
  styleUrl: './gerenciar-usuarios.css'
})
export class GerenciarUsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  cursos: Curso[] = [];
  isLoading = false;
  isCreating = false;
  isEditing = false;
  errorMessage = '';
  successMessage = '';

  // Form data
  formData: CreateUsuarioRequest = {
    nome: '',
    email: '',
    matricula: '',
    password: '',
    role: 'ALUNO'
  };

  // Inline editing
  editingUsuario: Usuario | null = null;
  originalEditingData: Usuario | null = null;

  constructor(
    private usuarioService: UsuarioService,
    private cursoService: CursoService,
    private router: Router,
    private oidcSecurityService: OidcSecurityService
  ) {
    console.log('🔧 [GERENCIAR-USUARIOS] Componente inicializado');
  }

  ngOnInit(): void {
    console.log('🔧 [GERENCIAR-USUARIOS] ngOnInit iniciado');
    
    // Don't immediately check auth - wait for it to stabilize
    setTimeout(() => {
      console.log('🔧 [GERENCIAR-USUARIOS] Verificando autenticação após delay...');
      
      this.oidcSecurityService.isAuthenticated$.subscribe({
        next: (authResult: AuthenticatedResult) => {
          console.log('🔐 [GERENCIAR-USUARIOS] Estado de autenticação:', authResult.isAuthenticated);
          
          if (authResult.isAuthenticated) {
            console.log('✅ [GERENCIAR-USUARIOS] Usuário autenticado, carregando dados...');
            // Add another small delay to ensure everything is ready
            setTimeout(() => {
              this.loadUsuarios();
              this.loadCursos();
            }, 200);
          } else {
            console.log('❌ [GERENCIAR-USUARIOS] Usuário não autenticado');
            this.errorMessage = 'Usuário não autenticado. Por favor, faça login.';
            // Don't redirect immediately - the auth system will handle it
          }
        },
        error: (error: any) => {
          console.error('❌ [GERENCIAR-USUARIOS] Erro na verificação de autenticação:', error);
          this.errorMessage = 'Erro na verificação de autenticação';
        }
      });
    }, 1000); // Wait 1 second for auth to fully stabilize
  }

  loadUsuarios(): void {
    this.isLoading = true;
    this.clearMessages();
    
    console.log('📋 [GERENCIAR-USUARIOS] Carregando usuários...');
    
    // First check if we have a token
    this.oidcSecurityService.getAccessToken().subscribe({
      next: (token) => {
        console.log('🔑 [GERENCIAR-USUARIOS] Token disponível:', token ? 'Sim' : 'Não');
        if (token) {
          console.log('🔑 [GERENCIAR-USUARIOS] Token length:', token.length);
          // Try to decode token to see roles
          try {
            const decodedToken: any = JSON.parse(atob(token.split('.')[1]));
            console.log('🔑 [GERENCIAR-USUARIOS] Token roles:', decodedToken.realm_access?.roles);
          } catch (e) {
            console.log('🔑 [GERENCIAR-USUARIOS] Erro ao decodificar token:', e);
          }
        }
        
        // Now make the actual request
        this.usuarioService.getUsuarios().subscribe({
          next: (usuarios) => {
            console.log('✅ [GERENCIAR-USUARIOS] Usuários carregados:', usuarios);
            this.usuarios = usuarios;
            this.isLoading = false;
          },
          error: (error) => {
            console.error('❌ [GERENCIAR-USUARIOS] Erro ao carregar usuários:', error);
            console.error('❌ [GERENCIAR-USUARIOS] Error status:', error.status);
            console.error('❌ [GERENCIAR-USUARIOS] Error message:', error.message);
            this.errorMessage = 'Erro ao carregar usuários. Tente novamente.';
            this.isLoading = false;
          }
        });
      },
      error: (tokenError) => {
        console.error('❌ [GERENCIAR-USUARIOS] Erro ao obter token:', tokenError);
        this.errorMessage = 'Erro de autenticação. Faça login novamente.';
        this.isLoading = false;
      }
    });
  }

  loadCursos(): void {
    console.log('📚 [GERENCIAR-USUARIOS] Carregando cursos...');
    
    this.cursoService.getCursos().subscribe({
      next: (cursos) => {
        console.log('✅ [GERENCIAR-USUARIOS] Cursos carregados:', cursos);
        this.cursos = cursos;
      },
      error: (error) => {
        console.error('❌ [GERENCIAR-USUARIOS] Erro ao carregar cursos:', error);
        // Non-blocking error - users can still be managed without courses loaded
      }
    });
  }

  startCreating(): void {
    console.log('➕ [GERENCIAR-USUARIOS] Iniciando criação de usuário');
    this.isCreating = true;
    this.isEditing = false;
    this.editingUsuario = null;
    this.clearMessages();
    this.resetForm();
  }

  startEditing(usuario: Usuario): void {
    console.log('✏️ [GERENCIAR-USUARIOS] Iniciando edição de usuário:', usuario);
    this.isCreating = false;
    this.isEditing = true;
    this.editingUsuario = null;
    this.clearMessages();
    
    // Populate form with usuario data (excluding password for editing)
    this.formData = {
      nome: usuario.nome,
      email: usuario.email,
      matricula: usuario.matricula,
      password: '', // Don't populate password for editing
      role: usuario.role,
      cursoId: usuario.cursoId // Include curso for editing
    };
  }

  cancelForm(): void {
    console.log('❌ [GERENCIAR-USUARIOS] Cancelando formulário');
    this.isCreating = false;
    this.isEditing = false;
    this.editingUsuario = null;
    this.resetForm();
    this.clearMessages();
  }

  submitForm(): void {
    console.log('💾 [GERENCIAR-USUARIOS] Submetendo formulário:', this.formData);
    
    // Validate course requirement for COORDENADOR and ALUNO
    if (this.isRoleRequiresCurso(this.formData.role) && !this.formData.cursoId) {
      this.errorMessage = `Curso é obrigatório para ${this.formData.role === 'COORDENADOR' ? 'Coordenadores' : 'Alunos'}.`;
      return;
    }
    
    if (this.isCreating) {
      this.createUsuario();
    } else if (this.isEditing) {
      this.updateUsuario();
    }
  }

  createUsuario(): void {
    this.isLoading = true;
    this.clearMessages();
    
    console.log('➕ [GERENCIAR-USUARIOS] Criando usuário:', this.formData);
    
    this.usuarioService.createUsuario(this.formData).subscribe({
      next: (novoUsuario) => {
        console.log('✅ [GERENCIAR-USUARIOS] Usuário criado:', novoUsuario);
        this.usuarios.push(novoUsuario);
        this.successMessage = 'Usuário criado com sucesso!';
        this.cancelForm();
        this.isLoading = false;
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [GERENCIAR-USUARIOS] Erro ao criar usuário:', error);
        this.errorMessage = 'Erro ao criar usuário. Verifique os dados e tente novamente.';
        this.isLoading = false;
      }
    });
  }

  updateUsuario(): void {
    if (!this.isEditing) return;
    
    // Find the usuario being edited
    const usuarioIndex = this.usuarios.findIndex(u => 
      u.nome === this.formData.nome || 
      u.email === this.formData.email ||
      u.matricula === this.formData.matricula
    );
    
    if (usuarioIndex === -1) {
      this.errorMessage = 'Usuário não encontrado para atualização.';
      return;
    }

    const usuario = this.usuarios[usuarioIndex];
    if (!usuario.id) {
      this.errorMessage = 'ID do usuário não encontrado.';
      return;
    }

    this.isLoading = true;
    this.clearMessages();
    
    // Don't include ID in the request body since it's in the URL path
    const updateData: any = {
      nome: this.formData.nome,
      email: this.formData.email,
      matricula: this.formData.matricula,
      role: this.formData.role,
      password: this.formData.password || '' // Include password if provided
    };
    
    // Only include cursoId if it has a valid value
    if (this.formData.cursoId && this.formData.cursoId > 0) {
      updateData.cursoId = this.formData.cursoId;
    }
    
    console.log('✏️ [GERENCIAR-USUARIOS] Atualizando usuário:', updateData);
    console.log('✏️ [GERENCIAR-USUARIOS] Update data being sent:', JSON.stringify(updateData, null, 2));
    
    this.usuarioService.updateUsuario(usuario.id, updateData).subscribe({
      next: (response) => {
        console.log('✅ [GERENCIAR-USUARIOS] Usuário atualizado:', response);
        // Since backend returns success message, reload users to get updated data
        this.loadUsuarios();
        this.successMessage = 'Usuário atualizado com sucesso!';
        this.cancelForm();
        this.isLoading = false;
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [GERENCIAR-USUARIOS] Erro ao atualizar usuário:', error);
        console.error('❌ [GERENCIAR-USUARIOS] Error details:', error.error);
        console.error('❌ [GERENCIAR-USUARIOS] Status:', error.status);
        console.error('❌ [GERENCIAR-USUARIOS] StatusText:', error.statusText);
        console.error('❌ [GERENCIAR-USUARIOS] URL:', error.url);
        
        if (error.error && error.error.message) {
          this.errorMessage = `Erro ao atualizar usuário: ${error.error.message}`;
        } else {
          this.errorMessage = 'Erro ao atualizar usuário. Tente novamente.';
        }
        this.isLoading = false;
      }
    });
  }

  deleteUsuario(usuario: Usuario): void {
    if (!usuario.id) {
      this.errorMessage = 'ID do usuário não encontrado.';
      return;
    }

    if (!confirm(`Tem certeza que deseja excluir o usuário "${usuario.nome}"?`)) {
      return;
    }
    
    console.log('🗑️ [GERENCIAR-USUARIOS] Deletando usuário:', usuario);
    this.clearMessages();
    
    this.usuarioService.deleteUsuario(usuario.id).subscribe({
      next: () => {
        console.log('✅ [GERENCIAR-USUARIOS] Usuário deletado');
        this.usuarios = this.usuarios.filter(u => u.id !== usuario.id);
        this.successMessage = 'Usuário excluído com sucesso!';
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [GERENCIAR-USUARIOS] Erro ao deletar usuário:', error);
        this.errorMessage = 'Erro ao excluir usuário. Tente novamente.';
      }
    });
  }

  // Inline editing methods
  startInlineEdit(usuario: Usuario): void {
    console.log('✏️ [GERENCIAR-USUARIOS] Iniciando edição inline:', usuario);
    this.editingUsuario = { ...usuario };
    this.originalEditingData = { ...usuario };
    this.isCreating = false;
    this.isEditing = false;
    this.clearMessages();
  }

  saveInlineEdit(): void {
    if (!this.editingUsuario || !this.editingUsuario.id) {
      this.errorMessage = 'Dados de edição não encontrados.';
      return;
    }

    // Validate course requirement for COORDENADOR and ALUNO
    if (this.isRoleRequiresCurso(this.editingUsuario.role) && !this.editingUsuario.cursoId) {
      this.errorMessage = `Curso é obrigatório para ${this.editingUsuario.role === 'COORDENADOR' ? 'Coordenadores' : 'Alunos'}.`;
      return;
    }

    console.log('💾 [GERENCIAR-USUARIOS] Salvando edição inline:', this.editingUsuario);
    console.log('💾 [GERENCIAR-USUARIOS] User ID being sent:', this.editingUsuario.id);
    console.log('💾 [GERENCIAR-USUARIOS] User ID type:', typeof this.editingUsuario.id);
    
    // Don't include ID in the request body since it's in the URL path
    const updateData: any = {
      nome: this.editingUsuario.nome,
      email: this.editingUsuario.email,
      matricula: this.editingUsuario.matricula,
      role: this.editingUsuario.role,
      password: '' // Backend might require password field even for updates
    };
    
    // Only include cursoId if it has a valid value
    if (this.editingUsuario.cursoId && this.editingUsuario.cursoId > 0) {
      updateData.cursoId = this.editingUsuario.cursoId;
    }
    
    console.log('💾 [GERENCIAR-USUARIOS] Update data being sent:', JSON.stringify(updateData, null, 2));
    console.log('💾 [GERENCIAR-USUARIOS] Original user data from backend:', {
      id: this.editingUsuario.id,
      nome: this.editingUsuario.nome,
      email: this.editingUsuario.email,
      matricula: this.editingUsuario.matricula,
      role: this.editingUsuario.role,
      cursoId: this.editingUsuario.cursoId,
      cursoNome: this.editingUsuario.cursoNome
    });
    
    this.usuarioService.updateUsuario(this.editingUsuario.id, updateData).subscribe({
      next: (response) => {
        console.log('✅ [GERENCIAR-USUARIOS] Usuário atualizado via inline edit:', response);
        // Since backend returns success message, reload users to get updated data
        this.loadUsuarios();
        this.editingUsuario = null;
        this.originalEditingData = null;
        this.successMessage = 'Usuário atualizado com sucesso!';
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [GERENCIAR-USUARIOS] Erro ao atualizar usuário via inline edit:', error);
        console.error('❌ [GERENCIAR-USUARIOS] Error details:', error.error);
        console.error('❌ [GERENCIAR-USUARIOS] Status:', error.status);
        console.error('❌ [GERENCIAR-USUARIOS] StatusText:', error.statusText);
        console.error('❌ [GERENCIAR-USUARIOS] URL:', error.url);
        
        if (error.error && error.error.message) {
          this.errorMessage = `Erro ao atualizar usuário: ${error.error.message}`;
        } else {
          this.errorMessage = 'Erro ao atualizar usuário. Tente novamente.';
        }
        this.cancelInlineEdit();
      }
    });
  }

  cancelInlineEdit(): void {
    console.log('❌ [GERENCIAR-USUARIOS] Cancelando edição inline');
    this.editingUsuario = null;
    this.originalEditingData = null;
  }

  // Utility methods
  resetForm(): void {
    this.formData = {
      nome: '',
      email: '',
      matricula: '',
      password: '',
      role: 'ALUNO',
      cursoId: undefined
    };
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  clearError(): void {
    this.errorMessage = '';
  }

  clearSuccess(): void {
    this.successMessage = '';
  }

  clearSuccessAfterDelay(): void {
    setTimeout(() => {
      this.clearSuccess();
    }, 5000);
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'COORDENADOR': return 'Coordenador';
      case 'PROFESSOR': return 'Professor';
      case 'ALUNO': return 'Aluno';
      default: return role;
    }
  }

  getCursoNome(usuario: Usuario): string {
    // If cursoNome is already provided in the response, use it
    if (usuario.cursoNome) {
      return usuario.cursoNome;
    }
    
    // If no cursoId, show dash
    if (!usuario.cursoId) {
      return '-';
    }
    
    // Fallback: try to find in cursos array
    const curso = this.cursos.find(c => c.id === usuario.cursoId);
    if (curso) {
      return curso.nome;
    }
    
    // Last resort: show ID with not found message
    return `Curso não encontrado (ID: ${usuario.cursoId})`;
  }

  isRoleRequiresCurso(role: string): boolean {
    return role === 'COORDENADOR' || role === 'ALUNO';
  }

  onRoleChange(): void {
    // Clear course selection if role doesn't require course
    if (!this.isRoleRequiresCurso(this.formData.role)) {
      this.formData.cursoId = undefined;
    }
  }

  trackByUsuario(index: number, usuario: Usuario): any {
    return usuario.id || index;
  }
}
