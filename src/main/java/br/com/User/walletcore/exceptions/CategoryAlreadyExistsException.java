package br.com.User.walletcore.exceptions;

public class CategoryAlreadyExistsException extends ConflictException {

    public CategoryAlreadyExistsException(String name) {
        super("Category already exists: " + name);
    }
}
