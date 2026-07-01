package br.pucminas.karv_coins.service;

import br.pucminas.karv_coins.dto.request.SalvarVantagemRequestDto;
import br.pucminas.karv_coins.dto.response.BeneficioMaisResgatadoResponseDto;
import br.pucminas.karv_coins.dto.response.PaginaResponseDto;
import br.pucminas.karv_coins.dto.response.VantagemResponseDto;
import br.pucminas.karv_coins.model.Empresa;
import br.pucminas.karv_coins.model.Vantagem;
import br.pucminas.karv_coins.repository.EmpresaRepository;
import br.pucminas.karv_coins.repository.ResgateRepository;
import br.pucminas.karv_coins.repository.VantagemRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VantagemService {
    private final VantagemRepository vantagemRepository;
    private final EmpresaRepository empresaRepository;
    private final ResgateRepository resgateRepository;

    public VantagemService(VantagemRepository vantagemRepository, EmpresaRepository empresaRepository,
                           ResgateRepository resgateRepository) {
        this.vantagemRepository = vantagemRepository;
        this.empresaRepository = empresaRepository;
        this.resgateRepository = resgateRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('EMPRESA')")
    public List<VantagemResponseDto> listarMinhasVantagens(String emailEmpresa) {
        return vantagemRepository.findByEmpresaEmailOrderByIdDesc(emailEmpresa)
                .stream()
                .map(VantagemResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('EMPRESA')")
    public List<BeneficioMaisResgatadoResponseDto> listarMaisResgatados(String emailEmpresa) {
        return resgateRepository.findRankingResgatesByEmpresaEmail(emailEmpresa);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ALUNO')")
    public PaginaResponseDto<VantagemResponseDto> listarVantagensDisponiveis(int page, int size) {
        int paginaNormalizada = Math.max(page, 0);
        int tamanhoNormalizado = Math.min(Math.max(size, 1), 30);
        PageRequest pageRequest = PageRequest.of(
                paginaNormalizada,
                tamanhoNormalizado,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return PaginaResponseDto.from(vantagemRepository
                .findAllByOrderByIdDesc(pageRequest)
                .map(VantagemResponseDto::from));
    }

    @Transactional
    @PreAuthorize("hasAuthority('EMPRESA')")
    public VantagemResponseDto criar(String emailEmpresa, SalvarVantagemRequestDto request) {
        Empresa empresa = buscarEmpresaAutenticada(emailEmpresa);

        Vantagem vantagem = new Vantagem();
        vantagem.setEmpresa(empresa);
        preencherVantagem(vantagem, request);

        return VantagemResponseDto.from(vantagemRepository.save(vantagem));
    }

    @Transactional
    @PreAuthorize("hasAuthority('EMPRESA')")
    public VantagemResponseDto atualizar(String emailEmpresa, Long id, SalvarVantagemRequestDto request) {
        Vantagem vantagem = vantagemRepository.findByIdAndEmpresaEmail(id, emailEmpresa)
                .orElseThrow(() -> new EntityNotFoundException("Vantagem não encontrada para a empresa autenticada."));

        preencherVantagem(vantagem, request);

        return VantagemResponseDto.from(vantagemRepository.save(vantagem));
    }

    private Empresa buscarEmpresaAutenticada(String emailEmpresa) {
        return empresaRepository.findByEmail(emailEmpresa)
                .orElseThrow(() -> new EntityNotFoundException("Empresa autenticada não encontrada."));
    }

    private void preencherVantagem(Vantagem vantagem, SalvarVantagemRequestDto request) {
        vantagem.setDescricao(request.descricao().trim());
        vantagem.setFotoUrl(request.fotoUrl().trim());
        vantagem.setCustoMoedas(request.custoMoedas());
    }
}
