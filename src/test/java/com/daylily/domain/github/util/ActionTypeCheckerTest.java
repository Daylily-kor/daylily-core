package com.daylily.domain.github.util;

import com.daylily.domain.github.action_type.PullRequestActionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionTypeCheckerTest {

    @Test
    void testValidActionType() {
        String action = "opened";
        PullRequestActionType createdAction = ActionTypeChecker.fromPullRequestActionString(action);
        assertEquals(PullRequestActionType.OPENED, createdAction);
    }

    @Test
    void testInvalidActionType() {
        String action = "invalid_action";
        assertThrows(
                IllegalArgumentException.class,
                () -> ActionTypeChecker.fromPullRequestActionString(action)
        );
    }
}
