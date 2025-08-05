package com.daylily.domain.github.action_type;

public enum InstallationActionType implements GitHubWebhookActionType {

    CREATED("created"),
    DELETED("deleted"),
    NEW_PERMISSIONS_ACCEPTED("new_permissions_accepted"),
    SUSPEND("suspend"),
    UNSUSPEND("unsuspend");

    private final String action;

    InstallationActionType(String action) {
        this.action = action.toLowerCase();
    }

    @Override
    public String action() {
        return action;
    }
}
