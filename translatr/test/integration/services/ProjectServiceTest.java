package integration.services;

import static org.fest.assertions.api.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;

import models.Project;
import play.Application;
import play.ApplicationLoader.Context;
import play.Environment;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;
import play.test.Helpers;
import play.test.WithApplication;
import services.ProjectService;

/**
 * @author resamsel
 * @version 11 Jan 2017
 */
public class ProjectServiceTest extends WithApplication {
  @Inject
  Application application;

  @Inject
  ProjectService projectService;

  @Test
  public void create() {
    running(fakeApplication(), () -> {
      projectService.create(new Project().withName("blubbb"));
      assertThat(1).isEqualTo(1);
    });
  }

  @Before
  public void setup() {
    Module testModule = new AbstractModule() {
      @Override
      public void configure() {
        // Install custom test binding here
      }
    };

    GuiceApplicationBuilder builder = new GuiceApplicationLoader()
        .builder(new Context(Environment.simple())).overrides(testModule);
    Guice.createInjector(builder.applicationModule()).injectMembers(this);

    Helpers.start(application);
  }

  @After
  public void teardown() {
    Helpers.stop(application);
  }
}
