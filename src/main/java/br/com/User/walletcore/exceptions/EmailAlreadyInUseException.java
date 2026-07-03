package br.com.User.walletcore.exceptions;

public class EmailAlreadyInUseException extends ConflictException {

    public EmailAlreadyInUseException(String email) {
        super("Email already in use: " + email);
    }
}
