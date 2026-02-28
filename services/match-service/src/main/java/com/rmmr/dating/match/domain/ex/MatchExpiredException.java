package com.rmmr.dating.match.domain.ex;

import java.util.UUID;

public class MatchExpiredException extends RuntimeException {
    public MatchExpiredException(UUID matchId) {
        super("match_expired:" + matchId);
    }
}