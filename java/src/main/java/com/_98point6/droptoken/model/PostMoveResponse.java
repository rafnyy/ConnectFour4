package com._98point6.droptoken.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 */
public class PostMoveResponse {
    private String moveLink;

    public PostMoveResponse() {}

    private PostMoveResponse(Builder builder) {
        this.moveLink = Preconditions.checkNotNull(builder.moveLink);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("moveLink", moveLink)
                .toString();
    }

    public String getMoveLink() {
        return moveLink;
    }

    public static class Builder {
        private String moveLink;

        public Builder moveLink(String moveLink) {
            this.moveLink = moveLink;
            return this;
        }

        public Builder fromPrototype(PostMoveResponse prototype) {
            moveLink = prototype.moveLink;
            return this;
        }

        public PostMoveResponse build() {
            return new PostMoveResponse(this);
        }
    }
}
