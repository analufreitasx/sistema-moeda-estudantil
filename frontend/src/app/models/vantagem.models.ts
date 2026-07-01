export interface Vantagem {
  id: number;
  descricao: string;
  fotoUrl: string;
  custoMoedas: number;
  empresaId: number;
  nomeEmpresa: string;
}

export interface BeneficioMaisResgatado {
  id: number;
  descricao: string;
  resgates: number;
  utilizados: number;
}

export interface SalvarVantagemRequest {
  descricao: string;
  fotoUrl: string;
  custoMoedas: number;
}

export interface ResgateResponse {
  id: number;
  vantagemId: number;
  descricaoVantagem: string;
  nomeEmpresa: string;
  valorMoedas: number;
  codigoCupom: string;
  data: string;
  saldoAtual: number;
  urlVerificacao: string;
}

export interface ResgateVerificacao {
  id: number;
  codigoCupom: string;
  vantagemId: number;
  descricaoVantagem: string;
  nomeEmpresa: string;
  alunoId: number;
  nomeAluno: string;
  emailAluno: string;
  valorMoedas: number;
  data: string;
  utilizado: boolean;
  utilizadoPorId?: number | null;
  nomeUtilizadoPor?: string | null;
  emailUtilizadoPor?: string | null;
}

export interface AprovarUtilizacaoResgateRequest {
  alunoId: number;
}
