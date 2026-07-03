package br.com.User.walletcore.controllers;

import br.com.User.walletcore.dtos.ApiInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<ApiInfoResponse> info() {
        return ResponseEntity.ok(new ApiInfoResponse(
                "WalletCore API",
                "1.0.0",
                "API pública de gestão financeira"
        ));
    }
}
