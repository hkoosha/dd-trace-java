import datadog.trace.instrumentation.testng.TestNGTest

class TestNG64Test extends TestNGTest {

  @Override
  String expectedTestFrameworkVersion() {
    // For the TestNG version used for tests (minimum supported)
    // it's not possible to extract the version.
    return null
  }

  @Override
  String assertionErrorMessage() {
    "expected:<true> but was:<false>"
  }

  @Override
  String classSkipReason() {
    // For the TestNG version used for tests (minimum supported)
    // class skip reason is not provided
    return null
  }

  @Override
  Map<String, String> testCaseTagsIfSuiteSetUpFailedOrSkipped(String skipReason) {
    // For the TestNG version used for tests (minimum supported)
    // if suite set up fails, skip listeners do not receive error message
    return null
  }
}
