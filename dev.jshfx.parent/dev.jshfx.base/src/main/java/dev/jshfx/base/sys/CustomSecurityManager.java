package dev.jshfx.base.sys;

import java.security.Permission;

public class CustomSecurityManager extends SecurityManager {

    @Override
    public void checkExit(int arg0) {
        throw new SecurityException("System exit not allowed.");
    }

    @Override
    public void checkPermission(Permission perm) {
        // Allow other activities by default
    }
}
