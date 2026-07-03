package br.com.User.walletcore.exceptions;

import br.com.User.walletcore.entities.CategoryType;
import br.com.User.walletcore.entities.TransactionType;

public class CategoryTypeMismatchException extends RuntimeException {

    public CategoryTypeMismatchException(CategoryType categoryType, TransactionType transactionType) {
        super("Category is of type " + categoryType + " and cannot be used for a " + transactionType + " transaction");
    }
}
