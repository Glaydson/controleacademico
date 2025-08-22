import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { DisciplinaService } from '../services/disciplina.service';
import { CursoService } from '../services/curso';
import { Disciplina, CreateDisciplinaRequest, UpdateDisciplinaRequest } from '../models/disciplina.model';
import { Usuario } from '../models/usuario.model';
import { ProfessorService } from '../services/professor.service';
import { Curso } from '../models/curso.model';

@Component({
  selector: 'app-disciplinas',
  templateUrl: './disciplinas.html',
  styleUrls: ['./disciplinas.css'],
  standalone: false
})
export class DisciplinasComponent implements OnInit {
  get editingProfessorId(): string | null {
    return this.editingDisciplina && this.editingDisciplina.professor ? this.editingDisciplina.professor.id ?? null : null;
  }

  set editingProfessorId(id: string | null) {
    if (this.editingDisciplina) {
      if (!this.editingDisciplina.professor) {
        this.editingDisciplina.professor = { id: undefined, nome: '' };
      }
      this.editingDisciplina.professor.id = id ?? undefined;
      // Optionally update nome if you want to show it immediately
      const found = this.professores.find(p => p.id === id);
      if (found) this.editingDisciplina.professor.nome = found.nome;
    }
  }
  groupDisciplinasByCurso(): void {
    const map = new Map<number, { curso: Curso, disciplinas: Disciplina[] }>();
    for (const d of this.disciplinas) {
      if (!d.curso) continue;
      if (!map.has(d.curso.id)) {
        map.set(d.curso.id, { curso: d.curso, disciplinas: [] });
      }
      map.get(d.curso.id)!.disciplinas.push(d);
    }
    this.disciplinasPorCurso = Array.from(map.values())
      .sort((a, b) => a.curso.nome.localeCompare(b.curso.nome))
      .map(group => ({
        curso: group.curso,
        disciplinas: group.disciplinas.sort((a, b) => a.nome.localeCompare(b.nome)),
        collapsed: false
      }));
  }
  // Data
  disciplinas: Disciplina[] = [];
  cursos: Curso[] = [];
  professores: Usuario[] = [];
  disciplinasPorCurso: { curso: Curso, disciplinas: Disciplina[], collapsed: boolean }[] = [];
  
  // Form state
  isCreating = false;
  isEditing = false;
  isLoading = false;
  
  // Form data
  formData: CreateDisciplinaRequest = {
    nome: '',
    codigo: '',
    cursoId: 0
  };
  
  // Inline editing
  editingDisciplina: Disciplina | null = null;
  originalEditingData: any = null;
  
  // Messages
  errorMessage = '';
  successMessage = '';

  constructor(
    private disciplinaService: DisciplinaService,
    private cursoService: CursoService,
    private professorService: ProfessorService,
    private oidcSecurityService: OidcSecurityService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    console.log('🚀 [DISCIPLINAS] Componente inicializado');
    // Wait for authentication to stabilize
    setTimeout(() => {
      this.oidcSecurityService.isAuthenticated$.subscribe({
        next: (authResult) => {
          if (authResult.isAuthenticated) {
            console.log('✅ [DISCIPLINAS] Usuário autenticado, carregando dados');
            setTimeout(() => {
              this.loadDisciplinas();
              this.loadCursos();
              this.loadProfessores();
            }, 200);
          } else {
            console.log('❌ [DISCIPLINAS] Usuário não autenticado');
            this.errorMessage = 'Usuário não autenticado. Por favor, faça login.';
            this.toastr.error(this.errorMessage);
          }
        },
        error: (error: any) => {
          console.error('❌ [DISCIPLINAS] Erro na verificação de autenticação:', error);
          this.errorMessage = 'Erro na verificação de autenticação';
          this.toastr.error(this.errorMessage);
        }
      });
    }, 1000);
  }

  loadProfessores(): void {
    this.professorService.getProfessores().subscribe({
      next: (professores) => {
        console.log('[DEBUG] Professores loaded from backend:', professores);
        this.professores = professores;
      },
      error: (error) => {
        console.error('[ERROR] Failed to load professores:', error);
        this.toastr.error('Erro ao carregar professores.');
      }
    });
  }

  loadDisciplinas(): void {
    this.isLoading = true;
    this.clearMessages();
    
    console.log('📋 [DISCIPLINAS] Carregando disciplinas...');
    
    this.disciplinaService.getDisciplinas().subscribe({
      next: (disciplinas) => {
        console.log('✅ [DISCIPLINAS] Disciplinas carregadas:', disciplinas);
          this.disciplinas = disciplinas;
          this.groupDisciplinasByCurso();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ [DISCIPLINAS] Erro ao carregar disciplinas:', error);
        
        if (error.status === 401) {
          console.log('🔄 [DISCIPLINAS] Token expirado, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Tentando renovar... Recarregue a página em alguns segundos.';
          this.toastr.error(this.errorMessage);
          return;
        }
        
  this.errorMessage = 'Erro ao carregar disciplinas. Tente novamente.';
  this.toastr.error(this.errorMessage);
        this.isLoading = false;
      }
    });
    // Group disciplinas by curso
    const map = new Map<number, { curso: Curso, disciplinas: Disciplina[] }>();
    for (const d of this.disciplinas) {
      if (!d.curso) continue;
      if (!map.has(d.curso.id)) {
        map.set(d.curso.id, { curso: d.curso, disciplinas: [] });
      }
      map.get(d.curso.id)!.disciplinas.push(d);
    }
    // Sort cursos by name, and disciplinas by name
    this.disciplinasPorCurso = Array.from(map.values())
      .sort((a, b) => a.curso.nome.localeCompare(b.curso.nome))
      .map(group => ({
        curso: group.curso,
        disciplinas: group.disciplinas.sort((a, b) => a.nome.localeCompare(b.nome)),
        collapsed: false
      }));
  }

  loadCursos(): void {
    console.log('📋 [DISCIPLINAS] Carregando cursos...');
    this.cursoService.getCursos().subscribe({
      next: (cursos) => {
        console.log('✅ [DISCIPLINAS] Cursos carregados:', cursos);
        this.cursos = cursos.sort((a, b) => a.nome.localeCompare(b.nome));
      },
      error: (error) => {
        console.error('❌ [DISCIPLINAS] Erro ao carregar cursos:', error);
        if (error.status !== 401) { // Don't show error for token expiration
    this.errorMessage = 'Erro ao carregar cursos. Algumas funcionalidades podem não funcionar.';
    this.toastr.error(this.errorMessage);
        }
      }
    });
  }

  toggleCollapse(cursoId: number): void {
    const group = this.disciplinasPorCurso.find(g => g.curso.id === cursoId);
    if (group) {
      group.collapsed = !group.collapsed;
    }
  }

  startCreating(): void {
    console.log('➕ [DISCIPLINAS] Iniciando criação de disciplina');
    this.isCreating = true;
    this.isEditing = false;
    this.editingDisciplina = null;
    this.clearMessages();
    this.resetForm();
  }

  startEditing(disciplina: Disciplina): void {
    console.log('✏️ [DISCIPLINAS] Iniciando edição de disciplina:', disciplina);
    this.isCreating = false;
    this.isEditing = true;
    this.editingDisciplina = null;
    this.clearMessages();
    
    this.formData = {
      nome: disciplina.nome,
      codigo: disciplina.codigo,
      cursoId: disciplina.curso.id
    };
  }

  createDisciplina(): void {
    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.clearMessages();
    
    console.log('➕ [DISCIPLINAS] Criando disciplina:', this.formData);
    
    this.disciplinaService.createDisciplina(this.formData).subscribe({
      next: (novaDisciplina) => {
        console.log('✅ [DISCIPLINAS] Disciplina criada:', novaDisciplina);
        this.loadDisciplinas(); // Reload to get updated list
  this.successMessage = 'Disciplina criada com sucesso!';
  this.toastr.success(this.successMessage);
        this.cancelForm();
        this.isLoading = false;
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [DISCIPLINAS] Erro ao criar disciplina:', error);
        
        if (error.status === 401) {
          console.log('🔄 [DISCIPLINAS] Token expirado durante criação, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Tentando renovar... Tente criar novamente em alguns segundos.';
          this.toastr.error(this.errorMessage);
          this.isLoading = false;
          
          setTimeout(() => {
            this.errorMessage = '';
          }, 3000);
          return;
        }
        
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
          this.toastr.error(this.errorMessage);
        } else {
          this.errorMessage = 'Erro ao criar disciplina. Tente novamente.';
          this.toastr.error(this.errorMessage);
        }
        this.isLoading = false;
      }
    });
  }

  updateDisciplina(): void {
    const disciplinaIndex = this.disciplinas.findIndex(d => 
      d.nome === this.formData.nome && d.codigo === this.formData.codigo && d.curso.id === this.formData.cursoId
    );
    
    if (disciplinaIndex === -1) {
  this.errorMessage = 'Disciplina não encontrada para atualização.';
  this.toastr.error(this.errorMessage);
      return;
    }

    const disciplinaId = this.disciplinas[disciplinaIndex].id;
    if (!disciplinaId) {
  this.errorMessage = 'ID da disciplina não encontrado.';
  this.toastr.error(this.errorMessage);
      return;
    }

    if (!this.validateForm()) {
      return;
    }

    this.isLoading = true;
    this.clearMessages();
    
    console.log('✏️ [DISCIPLINAS] Atualizando disciplina:', disciplinaId, this.formData);
    
    const updateData: UpdateDisciplinaRequest = {
      nome: this.formData.nome,
      codigo: this.formData.codigo,
      cursoId: this.formData.cursoId,
      professorId: this.editingProfessorId ?? null
    };
    
    this.disciplinaService.updateDisciplina(disciplinaId, updateData).subscribe({
      next: (disciplinaAtualizada) => {
        console.log('✅ [DISCIPLINAS] Disciplina atualizada:', disciplinaAtualizada);
        this.loadDisciplinas(); // Reload to get updated list
  this.successMessage = 'Disciplina atualizada com sucesso!';
  this.toastr.success(this.successMessage);
        this.cancelForm();
        this.isLoading = false;
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [DISCIPLINAS] Erro ao atualizar disciplina:', error);
        
        if (error.status === 401) {
          console.log('🔄 [DISCIPLINAS] Token expirado durante atualização, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Tentando renovar... Tente atualizar novamente em alguns segundos.';
          this.toastr.error(this.errorMessage);
          this.isLoading = false;
          
          setTimeout(() => {
            this.errorMessage = '';
          }, 3000);
          return;
        }
        
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
          this.toastr.error(this.errorMessage);
        } else {
          this.errorMessage = 'Erro ao atualizar disciplina. Tente novamente.';
          this.toastr.error(this.errorMessage);
        }
        this.isLoading = false;
      }
    });
  }

  deleteDisciplina(disciplina: Disciplina): void {
    if (!confirm(`Tem certeza que deseja excluir a disciplina "${disciplina.nome}"?`)) {
      return;
    }
    
    console.log('🗑️ [DISCIPLINAS] Deletando disciplina:', disciplina);
    this.clearMessages();
    
    this.disciplinaService.deleteDisciplina(disciplina.id).subscribe({
      next: () => {
        console.log('✅ [DISCIPLINAS] Disciplina deletada');
        this.disciplinas = this.disciplinas.filter(d => d.id !== disciplina.id);
  this.successMessage = 'Disciplina excluída com sucesso!';
  this.toastr.success(this.successMessage);
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [DISCIPLINAS] Erro ao deletar disciplina:', error);
        
        if (error.status === 401) {
          console.log('🔄 [DISCIPLINAS] Token expirado durante exclusão, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Tentando renovar... Tente excluir novamente em alguns segundos.';
          this.toastr.error(this.errorMessage);
          
          setTimeout(() => {
            this.errorMessage = '';
          }, 3000);
          return;
        }
        
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
        } else {
          this.errorMessage = 'Erro ao excluir disciplina. Tente novamente.';
        }
      }
    });
  }

  // Inline editing methods
  startInlineEdit(disciplina: Disciplina): void {
    console.log('✏️ [DISCIPLINAS] Iniciando edição inline:', disciplina);
      this.editingDisciplina = { ...disciplina };
      if (!this.editingDisciplina.professor) {
        this.editingDisciplina.professor = { id: undefined, nome: '' };
      }
      this.originalEditingData = { ...disciplina };
    this.clearMessages();
  }

  saveInlineEdit(): void {
    if (!this.editingDisciplina || !this.originalEditingData) {
      this.errorMessage = 'Dados de edição não encontrados.';
      return;
    }

    // Basic validation
    if (!this.editingDisciplina.nome.trim()) {
      this.errorMessage = 'Nome da disciplina é obrigatório.';
      return;
    }
    
    if (!this.editingDisciplina.codigo.trim()) {
      this.errorMessage = 'Código da disciplina é obrigatório.';
      return;
    }

    if (!this.editingDisciplina.curso || !this.editingDisciplina.curso.id) {
      this.errorMessage = 'Curso é obrigatório.';
      return;
    }

    console.log('💾 [DISCIPLINAS] Salvando edição inline:', this.editingDisciplina);
    
    const updateData: UpdateDisciplinaRequest = {
      nome: this.editingDisciplina.nome,
      codigo: this.editingDisciplina.codigo,
      cursoId: this.editingDisciplina.curso.id,
      professorId: this.editingDisciplina.professor?.id ?? null
    };
    
    this.disciplinaService.updateDisciplina(this.editingDisciplina.id, updateData).subscribe({
      next: (disciplinaAtualizada) => {
        console.log('✅ [DISCIPLINAS] Disciplina atualizada via inline edit:', disciplinaAtualizada);
        this.loadDisciplinas(); // Reload to get updated list
        this.editingDisciplina = null;
        this.originalEditingData = null;
        this.successMessage = 'Disciplina atualizada com sucesso!';
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [DISCIPLINAS] Erro ao atualizar disciplina via inline edit:', error);
        
        if (error.status === 401) {
          console.log('🔄 [DISCIPLINAS] Token expirado, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Tentando renovar... Tente novamente em alguns segundos.';
          
          // Wait a moment for the interceptor to refresh the token, then retry
          setTimeout(() => {
            this.errorMessage = '';
            console.log('🔄 [DISCIPLINAS] Tentando novamente após renovação do token...');
            this.saveInlineEdit(); // Retry the operation
          }, 2000);
          return;
        }
        
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
        } else {
          this.errorMessage = 'Erro ao atualizar disciplina. Tente novamente.';
        }
        this.cancelInlineEdit();
      }
    });
  }

  cancelInlineEdit(): void {
    console.log('❌ [DISCIPLINAS] Cancelando edição inline');
    this.editingDisciplina = null;
    this.originalEditingData = null;
  }

  // Utility methods
  resetForm(): void {
    this.formData = {
      nome: '',
      codigo: '',
      cursoId: 0
    };
  }
  // Always regroup after create, update, or delete
  // Call this.groupDisciplinasByCurso() after any change to this.disciplinas

  cancelForm(): void {
    console.log('❌ [DISCIPLINAS] Cancelando formulário');
    this.isCreating = false;
    this.isEditing = false;
    this.resetForm();
    this.clearMessages();
  }

  validateForm(): boolean {
    this.clearMessages();
    
    if (!this.formData.nome.trim()) {
      this.errorMessage = 'Nome da disciplina é obrigatório.';
      return false;
    }
    
    if (!this.formData.codigo.trim()) {
      this.errorMessage = 'Código da disciplina é obrigatório.';
      return false;
    }

    if (!this.formData.cursoId || this.formData.cursoId === 0) {
      this.errorMessage = 'Curso é obrigatório.';
      return false;
    }
    
    return true;
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

  getCursoById(cursoId: number): Curso | undefined {
    return this.cursos.find(c => c.id === cursoId);
  }
}
