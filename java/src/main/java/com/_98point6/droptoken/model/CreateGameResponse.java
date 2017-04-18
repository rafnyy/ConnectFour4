package com._98point6.droptoken.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 */
public class CreateGameResponse {
    private String gameId;

    public CreateGameResponse() {}

    private CreateGameResponse(Builder builder) {
        this.gameId = Preconditions.checkNotNull(builder.gameId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("gameId", gameId)
                .toString();
    }

    public String getGameId() {
        return gameId;
    }

    public static class Builder {
        private String gameId;

        public Builder gameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder fromPrototype(CreateGameResponse prototype) {
            gameId = prototype.gameId;
            return this;
        }

        public CreateGameResponse build() {
            return new CreateGameResponse(this);
        }
    }
}
