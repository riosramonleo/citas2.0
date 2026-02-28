package com.rmmr.dating.match.domain.ex;

import java.util.UUID;

public class FirstMoverOnlyException extends RuntimeException {
    public FirstMoverOnlyException(UUID matchId) {
        super("first_mover_only:" + matchId);
    }
}