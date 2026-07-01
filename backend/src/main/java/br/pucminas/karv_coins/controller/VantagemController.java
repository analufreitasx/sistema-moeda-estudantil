package br.pucminas.karv_coins.controller;

import br.pucminas.karv_coins.dto.request.SalvarVantagemRequestDto;
import br.pucminas.karv_coins.dto.response.BeneficioMaisResgatadoResponseDto;
import br.pucminas.karv_coins.dto.response.VantagemResponseDto;
import br.pucminas.karv_coins.service.VantagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/empresas/me/vantagens")
@Tag(name = "Vantagens", description = "Cadastro e gestão de vantagens por empresas parceiras")
public class VantagemController {
    private final VantagemService vantagemService;

    public VantagemController(VantagemService vantagemService) {
        this.vantagemService = vantagemService;
    }

    @GetMapping
    @Operation(summary = "Listar vantagens da empresa autenticada")
    public ResponseEntity<List<VantagemResponseDto>> listarMinhasVantagens(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(vantagemService.listarMinhasVantagens(jwt.getSubject()));
    }

    @GetMapping("/mais-resgatados")
    @Operation(summary = "Ranking de benefícios mais resgatados da empresa autenticada")
    public ResponseEntity<List<BeneficioMaisResgatadoResponseDto>> listarMaisResgatados(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(vantagemService.listarMaisResgatados(jwt.getSubject()));
    }

    @PostMapping
    @Operation(summary = "Cadastrar vantagem para a empresa autenticada")
    public ResponseEntity<Object> criar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SalvarVantagemRequestDto request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(formatarErros(bindingResult));
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(vantagemService.criar(jwt.getSubject(), request));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar vantagem da empresa autenticada")
    public ResponseEntity<Object> atualizar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody SalvarVantagemRequestDto request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(formatarErros(bindingResult));
        }

        try {
            return ResponseEntity.ok(vantagemService.atualizar(jwt.getSubject(), id, request));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private String formatarErros(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }
}
