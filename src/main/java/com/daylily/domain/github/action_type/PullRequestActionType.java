package com.daylily.domain.github.action_type;

/**
 * <a href="https://docs.github.com/en/webhooks/webhook-events-and-payloads#pull_request">Webhook events &amp; payloads</a>에 따른 Action Type들
 */
public enum PullRequestActionType implements GitHubWebhookActionType {

    ASSIGNED("assigned"),
    AUTO_MERGE_DISABLED("auto_merge_disabled"),
    AUTO_MERGE_ENABLED("auto_merge_enabled"),
    CLOSED("closed"),
    CONVERTED_TO_DRAFT("converted_to_draft"),
    DEMILESTONED("demilestoned"),
    DEQUEUED("dequeued"),
    EDITED("edited"),
    ENQUEUED("enqueued"),
    LABELED("labeled"),
    LOCKED("locked"),
    MILESTONED("milestoned"),
    OPENED("opened"),
    READY_FOR_REVIEW("ready_for_review"),
    REOPENED("reopened"),
    REVIEW_REQUEST_REMOVED("review_request_removed"),
    REVIEW_REQUESTED("review_requested"),
    SYNCHRONIZE("synchronize"),
    UNASSIGNED("unassigned"),
    UNLABELED("unlabeled"),
    UNLOCKED("unlocked")
    ;

    private final String action;

    PullRequestActionType(String action) {
        this.action = action.toLowerCase(); // 소문자 확실하게
    }

    @Override
    public String action() {
        return action;
    }
}
