package uk.co.flakeynetworks.vmixtally.model;

import uk.co.flakeynetworks.vmix.status.Input;

public class NullInput extends Input {


    public static final String DEFAULT_INPUT_NAME = "NULL";
    public static final String DEFAULT_INPUT_TYPE = "NULL";


    private static NullInput singletonRef;


    public NullInput() {

        setName(DEFAULT_INPUT_NAME);
        setType(DEFAULT_INPUT_TYPE);
    } // end of constructor


    public static NullInput getInstance() {

        if(singletonRef != null) return singletonRef;

        synchronized (NullInput.class) {

            if(singletonRef != null) return singletonRef;

            singletonRef = new NullInput();
            return singletonRef;
        } // end of synchronized
    } // end of getInstance
} // end of NullInput
