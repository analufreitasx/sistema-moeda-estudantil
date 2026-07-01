package br.pucminas.karv_coins.dto.response;

public record BeneficioMaisResgatadoResponseDto(
        Long id,
        String descricao,
        Long resgates,
        Long utilizados
) {
}
