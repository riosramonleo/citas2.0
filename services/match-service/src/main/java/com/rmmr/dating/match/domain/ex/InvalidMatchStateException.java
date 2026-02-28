package com.rmmr.dating.match.domain.ex;

import java.util.UUID;

public class InvalidMatchStateException extends RuntimeException {
    public InvalidMatchStateException(UUID matchId) {
        super("invalid_state:" + matchId);
    }
}