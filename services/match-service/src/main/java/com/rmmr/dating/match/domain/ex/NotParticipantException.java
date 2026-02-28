package com.rmmr.dating.match.domain.ex;

import java.util.UUID;

public class NotParticipantException extends RuntimeException {
    public NotParticipantException(UUID matchId) {
        super("not_a_participant:" + matchId);
    }
}