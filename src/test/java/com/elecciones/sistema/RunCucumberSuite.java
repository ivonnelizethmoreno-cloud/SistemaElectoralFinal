package com.elecciones.sistema;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.ConfigurationParameter;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.SNIPPET_TYPE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")  // 👉 tus .feature están en src/test/resources/features
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "com.elecciones.sistema.steps"  // 👉 ruta EXACTA donde está AppSteps.java
)
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-report.html, json:target/cucumber.json"
)
@ConfigurationParameter(
        key = SNIPPET_TYPE_PROPERTY_NAME,
        value = "camelcase"
)
public class RunCucumberSuite { }
