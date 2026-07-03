package br.com.User.walletcore.exceptions;

import java.util.UUID;

public class InsufficientBalanceException extends ConflictException {

    public InsufficientBalanceException(UUID accountId) {
        super("Operation would result in a negative balance for account: " + accountId);
    }
}
