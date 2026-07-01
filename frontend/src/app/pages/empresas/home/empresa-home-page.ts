import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { BeneficioMaisResgatado } from '../../../models/vantagem.models';
import { ToastService } from '../../../services/toast.service';
import { VantagensService } from '../../../services/vantagens.service';

@Component({
  selector: 'app-empresa-home-page',
  templateUrl: './empresa-home-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmpresaHomePage {
  private readonly vantagensService = inject(VantagensService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly beneficiosMaisResgatados = signal<BeneficioMaisResgatado[]>([]);
  protected readonly carregando = signal(false);

  constructor() {
    this.carregarRanking();
  }

  private carregarRanking(): void {
    this.carregando.set(true);
    this.vantagensService
      .listarBeneficiosMaisResgatados()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (beneficios) => {
          this.beneficiosMaisResgatados.set(beneficios);
          this.carregando.set(false);
        },
        error: (erro: unknown) => {
          this.carregando.set(false);
          this.toastService.erro('Erro ao carregar ranking', this.extrairMensagemErro(erro));
        },
      });
  }

  private extrairMensagemErro(erro: unknown): string {
    if (typeof erro === 'object' && erro !== null && 'error' in erro) {
      const httpError = erro as { error?: unknown };
      if (typeof httpError.error === 'string') {
        return httpError.error;
      }
    }

    return 'Não foi possível completar a operação. Tente novamente.';
  }
}
