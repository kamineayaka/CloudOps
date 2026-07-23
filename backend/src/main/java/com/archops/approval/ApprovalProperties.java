package com.archops.approval;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "archops.approval")
public class ApprovalProperties {

    /** TTL for session execution grants created via "remember for this session". */
    private int grantTtlMinutes = 60;

    /**
     * When false (default), HIGH-risk approvals cannot create grants and HIGH
     * invocations never match an existing grant.
     */
    private boolean allowHighGrants = false;

    public int getGrantTtlMinutes() {
        return grantTtlMinutes;
    }

    public void setGrantTtlMinutes(int grantTtlMinutes) {
        this.grantTtlMinutes = grantTtlMinutes;
    }

    public boolean isAllowHighGrants() {
        return allowHighGrants;
    }

    public void setAllowHighGrants(boolean allowHighGrants) {
        this.allowHighGrants = allowHighGrants;
    }
}
