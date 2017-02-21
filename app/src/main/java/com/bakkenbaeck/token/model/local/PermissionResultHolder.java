package com.bakkenbaeck.token.model.local;


public class PermissionResultHolder {

    private final int requestCode;
    private final String[] permissions;
    private final int[] grantResults;

    public PermissionResultHolder(final int requestCode, final String[] permissions, final int[] grantResults) {
        this.requestCode = requestCode;
        this.permissions = permissions;
        this.grantResults = grantResults;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public int[] getGrantResults() {
        return grantResults;
    }
}
