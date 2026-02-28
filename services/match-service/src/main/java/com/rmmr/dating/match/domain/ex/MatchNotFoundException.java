package com.rmmr.dating.match.domain.ex;

import java.util.UUID;

public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(UUID id) {
        super("match_not_found:" + id);
    }
}