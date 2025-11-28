package com.elecciones.sistema;

import io.cucumber.junit.platform.engine.Cucumber;

@Cucumber
@io.cucumber.junit.platform.engine.ConfigurationParameter(
    key = io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME,
    value = "classpath:features"
)
public class RunCucumberTest { }
