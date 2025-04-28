package cl.gbarrera.demo.exception;

import cl.gbarrera.demo.util.Messages;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super(String.format(Messages.PRODUCT_ID_NOT_FOUND, id));
    }
    public ProductNotFoundException(String name) {
        super(String.format(Messages.PRODUCT_NOT_FOUND_DETAIL, name));
    }
}