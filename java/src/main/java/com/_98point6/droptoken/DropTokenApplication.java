package com._98point6.droptoken;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.dropwizard.Application;
import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.jersey.validation.JerseyViolationExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 *
 */
public class DropTokenApplication extends Application<DropTokenConfiguration> {
        public static void main(String[] args) throws Exception {
            new DropTokenApplication().run(args);
        }

        @Override
        public String getName() {
            return "98Point6 - Drop Token";
        }

        @Override
        public void initialize(Bootstrap<DropTokenConfiguration> bootstrap) {
        }

        @Override
        public void run(DropTokenConfiguration configuration,
                Environment environment) {
            environment.getObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .registerModule(new Jdk8Module());
            environment.getObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            environment.jersey().register(new DropTokenExceptionMapper());
            environment.jersey().register(new JerseyViolationExceptionMapper());
            environment.jersey().register(new JsonProcessingExceptionMapper());
            environment.jersey().register(new EarlyEofExceptionMapper());

            final DropTokenResource resource = new DropTokenResource();
            environment.jersey().register(resource);

        }

}
