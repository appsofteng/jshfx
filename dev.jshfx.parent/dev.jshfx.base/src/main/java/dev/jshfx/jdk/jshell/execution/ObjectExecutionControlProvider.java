package dev.jshfx.jdk.jshell.execution;

import java.util.Map;

import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

public class ObjectExecutionControlProvider implements ExecutionControlProvider {

    private ObjectExecutionControl executionControl;
    
    public ObjectExecutionControl getExecutionControl() {
        return executionControl;
    }
    
    @Override
    public String name() {
        return "object";
    }
    
    @Override
    public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) {
        return executionControl = new ObjectExecutionControl();
    }
}
