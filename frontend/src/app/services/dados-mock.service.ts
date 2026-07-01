import { computed, Injectable, signal } from '@angular/core';

export interface BeneficioDisponivel {
  id: number;
  nome: string;
  empresa: string;
  custoMoedas: number;
  descricao: string;
}

export interface AlunoRelacionado {
  id: number;
  nome: string;
  curso: string;
  turma: string;
  saldoMoedas: number;
}

@Injectable({ providedIn: 'root' })
export class DadosMockService {
  private readonly saldoAlunoSignal = signal(420);
  private readonly saldoProfessorSignal = signal(1000);
  private readonly alunosRelacionadosSignal = signal<AlunoRelacionado[]>([
    { id: 1, nome: 'Mariana Souza', curso: 'Engenharia de Software', turma: 'ES-5A', saldoMoedas: 320 },
    { id: 2, nome: 'Pedro Almeida', curso: 'Ciência da Computação', turma: 'CC-3B', saldoMoedas: 180 },
    { id: 3, nome: 'Lívia Fernandes', curso: 'Sistemas de Informação', turma: 'SI-4A', saldoMoedas: 510 },
  ]);

  readonly saldoAluno = this.saldoAlunoSignal.asReadonly();
  readonly saldoProfessor = this.saldoProfessorSignal.asReadonly();
  readonly alunosRelacionados = this.alunosRelacionadosSignal.asReadonly();
  readonly totalAlunosRelacionados = computed(() => this.alunosRelacionados().length);

  readonly beneficiosDisponiveis: BeneficioDisponivel[] = [
    {
      id: 1,
      nome: 'Desconto em cafeteria',
      empresa: 'Café Campus',
      custoMoedas: 120,
      descricao: 'Cupom de 25% para bebidas e lanches durante a semana.',
    },
    {
      id: 2,
      nome: 'Mentoria de carreira',
      empresa: 'TechBridge',
      custoMoedas: 300,
      descricao: 'Sessão individual com profissionais parceiros da área de tecnologia.',
    },
    {
      id: 3,
      nome: 'Material de estudos',
      empresa: 'Livraria Saber',
      custoMoedas: 220,
      descricao: 'Voucher para livros, apostilas e materiais complementares.',
    },
  ];

  distribuirMoedas(alunoId: number, quantidade: number): boolean {
    if (quantidade <= 0 || quantidade > this.saldoProfessor()) {
      return false;
    }

    this.saldoProfessorSignal.update((saldoAtual) => saldoAtual - quantidade);
    this.alunosRelacionadosSignal.update((alunos) =>
      alunos.map((aluno) =>
        aluno.id === alunoId ? { ...aluno, saldoMoedas: aluno.saldoMoedas + quantidade } : aluno,
      ),
    );

    return true;
  }
}
