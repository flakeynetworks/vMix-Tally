package uk.co.flakeynetworks.vmixtally.model;

import androidx.annotation.NonNull;

public class ErrorMessage {


    private final String message;
    private boolean hasBeenDisplayed = false;


    public ErrorMessage(String message) { this.message = message; } // end of constructor

    public synchronized boolean hasBeenDisplayed() { return hasBeenDisplayed; } // end of hasBeenDisplayed

    public synchronized void setHasBeenDisplayed() { hasBeenDisplayed = true; } // end of setHasBeenDisplayed

    public String getMessage() { return message; } // end of getMessage

    @NonNull
    @Override
    public String toString() { return message; } // end of toString
} // end of ErrorMessage
