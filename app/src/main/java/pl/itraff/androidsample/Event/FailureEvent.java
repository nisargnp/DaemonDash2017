package pl.itraff.androidsample.Event;

public class FailureEvent {

    protected String message;

    public FailureEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
