package com.wikia.vignette;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavetteMain extends Application<JavetteConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavetteMain.class);

  public static void main(String[] args) throws Exception {
    new JavetteMain().run("server");
  }

  @Override
  public void run(JavetteConfiguration configuration, Environment environment) {
    environment.jersey().register(ThumbnailResource.class);
    LOGGER.info("started");
  }
}
