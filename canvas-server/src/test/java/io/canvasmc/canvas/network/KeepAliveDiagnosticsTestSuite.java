package io.canvasmc.canvas.network;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Keepalive diagnostics tests")
@SelectClasses(KeepAliveDiagnosticsTest.class)
public class KeepAliveDiagnosticsTestSuite {
}
