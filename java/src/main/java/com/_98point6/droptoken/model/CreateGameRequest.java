package com._98point6.droptoken.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 *
 */
public class CreateGameRequest {
    private List<String> players;
    private Integer columns;
    private Integer rows;

    public CreateGameRequest() {}

    private CreateGameRequest(Builder builder) {
        this.players = Preconditions.checkNotNull(builder.players);
        this.columns = Preconditions.checkNotNull(builder.columns);
        this.rows = Preconditions.checkNotNull(builder.rows);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("players", players)
                .append("columns", columns)
                .append("rows", rows)
                .toString();
    }

    public List<String> getPlayers() {
        return players;
    }

    public Integer getColumns() {
        return columns;
    }

    public Integer getRows() {
        return rows;
    }


    public static class Builder {
        private List<String> players;
        private Integer columns;
        private Integer rows;

        public Builder players(List<String> players) {
            this.players = players;
            return this;
        }

        public Builder columns(Integer columns) {
            this.columns = columns;
            return this;
        }

        public Builder rows(Integer rows) {
            this.rows = rows;
            return this;
        }

        public Builder fromPrototype(CreateGameRequest prototype) {
            players = prototype.players;
            columns = prototype.columns;
            rows = prototype.rows;
            return this;
        }

        public CreateGameRequest build() {
            return new CreateGameRequest(this);
        }
    }
}
