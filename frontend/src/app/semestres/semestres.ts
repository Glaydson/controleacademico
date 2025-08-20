import { Component, OnInit } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { OidcSecurityService, AuthenticatedResult } from 'angular-auth-oidc-client';
import { Semestre, CreateSemestreRequest, UpdateSemestreRequest } from '../models/semestre.model';
import { SemestreService } from '../services/semestre.service';

@Component({
  selector: 'app-semestres',
  standalone: false,
  templateUrl: './semestres.html',
  styleUrl: './semestres.css'
})
export class SemestresComponent implements OnInit {
  semestres: Semestre[] = [];
  isLoading = false;
  isCreating = false;
  isEditing = false;
  errorMessage = '';
  successMessage = '';

  // Form data
  formData: CreateSemestreRequest = {
    ano: new Date().getFullYear(),
    periodo: '1'
  };

  // Inline editing
  editingSemestre: Semestre | null = null;
  originalEditingData: Semestre | null = null;

  // Período options
  periodoOptions = [
    { value: '1', label: '1º Semestre' },
    { value: '2', label: '2º Semestre' }
  ];

  constructor(
    private semestreService: SemestreService,
    private router: Router,
    private oidcSecurityService: OidcSecurityService,
    private toastr: ToastrService
  ) {
    console.log('🔧 [SEMESTRES] Componente inicializado');
  }

  ngOnInit(): void {
    console.log('🔧 [SEMESTRES] ngOnInit iniciado');
    
    // Wait for auth to stabilize
    setTimeout(() => {
      console.log('🔧 [SEMESTRES] Verificando autenticação após delay...');
      
      this.oidcSecurityService.isAuthenticated$.subscribe({
        next: (authResult: AuthenticatedResult) => {
          console.log('🔐 [SEMESTRES] Estado de autenticação:', authResult.isAuthenticated);
          
          if (authResult.isAuthenticated) {
            console.log('✅ [SEMESTRES] Usuário autenticado, carregando dados...');
            setTimeout(() => {
              this.loadSemestres();
            }, 200);
          } else {
            console.log('❌ [SEMESTRES] Usuário não autenticado');
            this.errorMessage = 'Usuário não autenticado. Por favor, faça login.';
          }
        },
        error: (error: any) => {
          console.error('❌ [SEMESTRES] Erro na verificação de autenticação:', error);
          this.errorMessage = 'Erro na verificação de autenticação';
        }
      });
    }, 1000);
  }

  loadSemestres(): void {
    this.isLoading = true;
    this.clearMessages();
    
    console.log('📋 [SEMESTRES] Carregando semestres...');
    
    this.semestreService.getSemestres().subscribe({
      next: (semestres) => {
        console.log('✅ [SEMESTRES] Semestres carregados:', semestres);
        this.semestres = semestres.sort((a, b) => {
          // Sort by year desc, then by period desc
          if (a.ano !== b.ano) {
            return b.ano - a.ano;
          }
          return b.periodo.localeCompare(a.periodo);
        });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('❌ [SEMESTRES] Erro ao carregar semestres:', error);
        
        if (error.status === 401) {
          console.log('🔄 [SEMESTRES] Token expirado durante carregamento, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Recarregando...';
          
          // Wait a moment for the interceptor to refresh the token, then retry
          setTimeout(() => {
            this.errorMessage = '';
            console.log('🔄 [SEMESTRES] Tentando recarregar após renovação do token...');
            this.loadSemestres(); // Retry the operation
          }, 2000);
          return;
        }
        
  this.errorMessage = 'Erro ao carregar semestres. Tente novamente.';
  this.toastr.error(this.errorMessage);
        this.isLoading = false;
      }
    });
  }

  startCreating(): void {
    console.log('➕ [SEMESTRES] Iniciando criação de semestre');
    this.isCreating = true;
    this.isEditing = false;
    this.editingSemestre = null;
    this.clearMessages();
    this.resetForm();
  }

  startEditing(semestre: Semestre): void {
    console.log('✏️ [SEMESTRES] Iniciando edição de semestre:', semestre);
    this.isCreating = false;
    this.isEditing = true;
    this.editingSemestre = null;
    this.clearMessages();
    
    this.formData = {
      ano: semestre.ano,
      periodo: semestre.periodo
    };
  }

  cancelForm(): void {
    console.log('❌ [SEMESTRES] Cancelando formulário');
    this.isCreating = false;
    this.isEditing = false;
    this.editingSemestre = null;
    this.resetForm();
    this.clearMessages();
  }

  submitForm(): void {
    console.log('💾 [SEMESTRES] Submetendo formulário:', this.formData);
    
    if (this.isCreating) {
      this.createSemestre();
    } else if (this.isEditing) {
      this.updateSemestre();
    }
  }

  createSemestre(): void {
    this.isLoading = true;
    this.clearMessages();
    
    console.log('➕ [SEMESTRES] Criando semestre:', this.formData);
    
    this.semestreService.createSemestre(this.formData).subscribe({
      next: (novoSemestre) => {
        console.log('✅ [SEMESTRES] Semestre criado:', novoSemestre);
        this.loadSemestres(); // Reload to get updated list
  this.successMessage = 'Semestre criado com sucesso!';
  this.toastr.success(this.successMessage);
        this.cancelForm();
        this.isLoading = false;
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [SEMESTRES] Erro ao criar semestre:', error);
        
        if (error.status === 401) {
          console.log('🔄 [SEMESTRES] Token expirado durante criação, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Tentando renovar... Tente criar novamente em alguns segundos.';
          this.toastr.error(this.errorMessage);
          this.isLoading = false;
          
          // Wait a moment for the interceptor to refresh the token
          setTimeout(() => {
            this.errorMessage = '';
          }, 3000);
          return;
        }
        
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
          this.toastr.error(this.errorMessage);
        } else {
          this.errorMessage = 'Erro ao criar semestre. Tente novamente.';
          this.toastr.error(this.errorMessage);
        }
        this.isLoading = false;
      }
    });
  }

  updateSemestre(): void {
    const semestreIndex = this.semestres.findIndex(s => 
      s.ano === this.formData.ano && s.periodo === this.formData.periodo
    );
    
    if (semestreIndex === -1) {
      this.errorMessage = 'Semestre não encontrado para atualização.';
      return;
    }

    const semestre = this.semestres[semestreIndex];
    if (!semestre.id) {
      this.errorMessage = 'ID do semestre não encontrado.';
      return;
    }

    this.isLoading = true;
    this.clearMessages();
    
    const updateData: UpdateSemestreRequest = {
      id: semestre.id,
      ano: this.formData.ano,
      periodo: this.formData.periodo
    };
    
    console.log('✏️ [SEMESTRES] Atualizando semestre:', updateData);
    
    this.semestreService.updateSemestre(semestre.id, updateData).subscribe({
      next: (semestreAtualizado) => {
        console.log('✅ [SEMESTRES] Semestre atualizado:', semestreAtualizado);
        this.loadSemestres(); // Reload to get updated list
  this.successMessage = 'Semestre atualizado com sucesso!';
  this.toastr.success(this.successMessage);
        this.cancelForm();
        this.isLoading = false;
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [SEMESTRES] Erro ao atualizar semestre:', error);
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
        } else {
          this.errorMessage = 'Erro ao atualizar semestre. Tente novamente.';
        }
        this.toastr.error(this.errorMessage);
        this.isLoading = false;
      }
    });
  }

  deleteSemestre(semestre: Semestre): void {
    if (!semestre.id) {
      this.errorMessage = 'ID do semestre não encontrado.';
      return;
    }

    if (!confirm(`Tem certeza que deseja excluir o semestre "${semestre.descricao}"?`)) {
      return;
    }
    
    console.log('🗑️ [SEMESTRES] Deletando semestre:', semestre);
    this.clearMessages();
    
    this.semestreService.deleteSemestre(semestre.id).subscribe({
      next: () => {
        console.log('✅ [SEMESTRES] Semestre deletado');
        this.semestres = this.semestres.filter(s => s.id !== semestre.id);
  this.successMessage = 'Semestre excluído com sucesso!';
  this.toastr.success(this.successMessage);
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [SEMESTRES] Erro ao deletar semestre:', error);
        
        if (error.status === 401) {
          console.log('🔄 [SEMESTRES] Token expirado durante exclusão, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Tentando renovar... Tente excluir novamente em alguns segundos.';
          this.toastr.error(this.errorMessage);
          
          // Wait a moment for the interceptor to refresh the token
          setTimeout(() => {
            this.errorMessage = '';
          }, 3000);
          return;
        }
        
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
        } else {
          this.errorMessage = 'Erro ao excluir semestre. Tente novamente.';
        }
        this.toastr.error(this.errorMessage);
      }
    });
  }

  // Inline editing methods
  startInlineEdit(semestre: Semestre): void {
    console.log('✏️ [SEMESTRES] Iniciando edição inline:', semestre);
    this.editingSemestre = { ...semestre };
    this.originalEditingData = { ...semestre };
    this.isCreating = false;
    this.isEditing = false;
    this.clearMessages();
  }

  saveInlineEdit(): void {
    if (!this.editingSemestre || !this.editingSemestre.id) {
      this.errorMessage = 'Dados de edição não encontrados.';
      return;
    }

    console.log('💾 [SEMESTRES] Salvando edição inline:', this.editingSemestre);
    
    const updateData: UpdateSemestreRequest = {
      id: this.editingSemestre.id,
      ano: this.editingSemestre.ano,
      periodo: this.editingSemestre.periodo
    };
    
    this.semestreService.updateSemestre(this.editingSemestre.id, updateData).subscribe({
      next: (semestreAtualizado) => {
        console.log('✅ [SEMESTRES] Semestre atualizado via inline edit:', semestreAtualizado);
        this.loadSemestres(); // Reload to get updated list
        this.editingSemestre = null;
        this.originalEditingData = null;
        this.successMessage = 'Semestre atualizado com sucesso!';
        this.clearSuccessAfterDelay();
      },
      error: (error) => {
        console.error('❌ [SEMESTRES] Erro ao atualizar semestre via inline edit:', error);
        
        if (error.status === 401) {
          console.log('🔄 [SEMESTRES] Token expirado, aguardando renovação automática...');
          this.errorMessage = 'Sessão expirada. Tentando renovar... Tente novamente em alguns segundos.';
          
          // Wait a moment for the interceptor to refresh the token, then retry
          setTimeout(() => {
            this.errorMessage = '';
            console.log('🔄 [SEMESTRES] Tentando novamente após renovação do token...');
            this.saveInlineEdit(); // Retry the operation
          }, 2000);
          return;
        }
        
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
        } else {
          this.errorMessage = 'Erro ao atualizar semestre. Tente novamente.';
        }
        this.cancelInlineEdit();
      }
    });
  }

  cancelInlineEdit(): void {
    console.log('❌ [SEMESTRES] Cancelando edição inline');
    this.editingSemestre = null;
    this.originalEditingData = null;
  }

  // Utility methods
  resetForm(): void {
    this.formData = {
      ano: new Date().getFullYear(),
      periodo: '1'
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

  getPeriodoLabel(periodo: string): string {
    const option = this.periodoOptions.find(opt => opt.value === periodo);
    return option ? option.label : periodo;
  }

  getCurrentYear(): number {
    return new Date().getFullYear();
  }

  getYearOptions(): number[] {
    const currentYear = this.getCurrentYear();
    const years = [];
    for (let i = currentYear - 5; i <= currentYear + 5; i++) {
      years.push(i);
    }
    return years;
  }

  trackBySemestre(index: number, semestre: Semestre): any {
    return semestre.id || index;
  }
}
