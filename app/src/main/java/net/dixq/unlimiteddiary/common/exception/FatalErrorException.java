package net.dixq.unlimiteddiary.common.exception;

public class FatalErrorException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public FatalErrorException(String msg){
        super(msg);
    }
}
