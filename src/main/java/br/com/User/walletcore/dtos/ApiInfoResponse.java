package br.com.User.walletcore.dtos;

public record ApiInfoResponse(
        String name,
        String version,
        String description
) {
}
