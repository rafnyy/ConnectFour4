package com._98point6.droptoken.model;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 *
 */
public class GetGamesResponse {
    private List<String> games;

    public GetGamesResponse() {}

    private GetGamesResponse(Builder builder) {
        this.games = Preconditions.checkNotNull(builder.games);
    }

    public List<String> getGames() {
        return games;
    }


    public static class Builder {
        private List<String> games;

        public Builder games(List<String> games) {
            this.games = games;
            return this;
        }

        public Builder fromPrototype(GetGamesResponse prototype) {
            games = prototype.games;
            return this;
        }

        public GetGamesResponse build() {
            return new GetGamesResponse(this);
        }
    }
}
