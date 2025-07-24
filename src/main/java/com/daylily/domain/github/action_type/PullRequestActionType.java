package com.daylily.domain.github.action_type;

/**
 * <a href="https://docs.github.com/en/webhooks/webhook-events-and-payloads#pull_request">Webhook events &amp; payloads</a>에 따른 Action Type들
 */
public enum PullRequestActionType {

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
        this.action = action;
    }

    /**
     * GitHub에서 사용하는 Pull Request Action Type 문자열을 enum으로 변환합니다.
     * @param action Pull Request Action Type 문자열, 예: "opened", "closed" 등
     * @return PullRequestActionType enum 값
     * @throws IllegalArgumentException 공식 API에 정의되지 않은 Action Type 문자열이 들어올 경우 IllegalArgumentException을 발생
     */
    public static PullRequestActionType fromString(String action) {
        for (var type : PullRequestActionType.values()) {
            if (type.action.equalsIgnoreCase(action)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown pull request action type: " + action);
    }
}
