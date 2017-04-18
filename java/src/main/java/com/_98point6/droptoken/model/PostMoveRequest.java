package com._98point6.droptoken.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 */
public class PostMoveRequest {
    private Integer column;

    public PostMoveRequest() {}

    private PostMoveRequest(Builder builder) {
        this.column = Preconditions.checkNotNull(builder.column);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("column", column)
                .toString();
    }

    public Integer getColumn() {
        return column;
    }

    public static class Builder {
        private Integer column;

        public Builder column(Integer column) {
            this.column = column;
            return this;
        }

        public Builder fromPrototype(PostMoveRequest prototype) {
            column = prototype.column;
            return this;
        }

        public PostMoveRequest build() {
            return new PostMoveRequest(this);
        }
    }
}
