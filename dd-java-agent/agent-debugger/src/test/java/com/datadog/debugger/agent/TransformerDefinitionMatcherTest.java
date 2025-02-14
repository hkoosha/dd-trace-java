package com.datadog.debugger.agent;

import static com.datadog.debugger.util.ClassFileHelperTest.getClassFileBytes;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

import com.datadog.debugger.probe.LogProbe;
import com.datadog.debugger.probe.MetricProbe;
import com.datadog.debugger.probe.ProbeDefinition;
import com.datadog.debugger.probe.SpanProbe;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TransformerDefinitionMatcherTest {
  private static final String PROBE_ID1 = "beae1807-f3b0-4ea8-a74f-826790c5e6f6";
  private static final String PROBE_ID2 = "beae1807-f3b0-4ea8-a74f-826790c5e6f7";
  private static final String SERVICE_NAME = "service-name";

  @Test
  public void empty() {
    TransformerDefinitionMatcher matcher = createMatcher(emptyList(), emptyList(), emptyList());
    assertTrue(matcher.isEmpty());
  }

  @Test
  public void fullQualifiedClassName() {
    LogProbe probe = createProbe(PROBE_ID1, "java.lang.String", "indexOf");
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(1, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
  }

  @Test
  public void simpleClassName() {
    LogProbe probe = createProbe(PROBE_ID1, "String", "indexOf");
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(1, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
  }

  @Test
  public void simpleClassNameNoClassRedefined() {
    LogProbe probe = createProbe(PROBE_ID1, "String", "indexOf");
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe), emptyList());
    List<ProbeDefinition> probeDefinitions =
        matcher.match(
            null,
            getClassPath(String.class),
            String.class.getTypeName(),
            getClassFileBytes(String.class));
    assertEquals(1, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
  }

  @Test
  public void sourceFileFullFileName() {
    LogProbe probe = createProbe(PROBE_ID1, "src/main/java/java/lang/String.java", 23);
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(1, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
  }

  @Test
  public void sourceFileAbsoluteFileName() {
    LogProbe probe =
        createProbe(PROBE_ID1, "/home/user/project/src/main/java/java/lang/String.java", 23);
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(1, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
  }

  @Test
  public void sourceFileSimpleFileName() {
    LogProbe probe = createProbe(PROBE_ID1, "String.java", 23);
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(1, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
  }

  @Test
  public void multiProbesFQN() {
    LogProbe probe1 = createProbe(PROBE_ID1, "java.lang.String", "indexOf");
    LogProbe probe2 = createProbe(PROBE_ID2, "java.lang.String", "substring");
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe1, probe2), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(2, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
    assertEquals(PROBE_ID2, probeDefinitions.get(1).getId());
  }

  @Test
  public void multiProbesSimpleName() {
    LogProbe probe1 = createProbe(PROBE_ID1, "String", "indexOf");
    LogProbe probe2 = createProbe(PROBE_ID2, "String", "substring");
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe1, probe2), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(2, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
    assertEquals(PROBE_ID2, probeDefinitions.get(1).getId());
  }

  @Test
  public void multiProbesSourceFile() {
    LogProbe probe1 = createProbe(PROBE_ID1, "src/main/java/java/lang/String.java", 23);
    LogProbe probe2 = createProbe(PROBE_ID2, "src/main/java/java/lang/String.java", 42);
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe1, probe2), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(2, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
    assertEquals(PROBE_ID2, probeDefinitions.get(1).getId());
  }

  @Test
  public void mixedProbesFQNSimple() {
    LogProbe probe1 = createProbe(PROBE_ID1, "java.lang.String", "indexOf");
    LogProbe probe2 = createProbe(PROBE_ID2, "String", "substring");
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe1, probe2), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(2, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
    assertEquals(PROBE_ID2, probeDefinitions.get(1).getId());
  }

  @Test
  public void mixedSnapshotMetricProbes() {
    LogProbe probe1 = createProbe(PROBE_ID1, "java.lang.String", "indexOf");
    MetricProbe probe2 = createMetric(PROBE_ID2, "String", "substring");
    TransformerDefinitionMatcher matcher =
        createMatcher(Arrays.asList(probe2), Arrays.asList(probe1), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(2, probeDefinitions.size());
    assertEquals(PROBE_ID1, probeDefinitions.get(0).getId());
    assertEquals(PROBE_ID2, probeDefinitions.get(1).getId());
  }

  @Test
  public void partialSimpleNameShouldNotMatch() {
    LogProbe probe1 = createProbe(PROBE_ID1, "SuperString.java", 11);
    TransformerDefinitionMatcher matcher =
        createMatcher(emptyList(), Arrays.asList(probe1), emptyList());
    List<ProbeDefinition> probeDefinitions = match(matcher, String.class);
    assertEquals(0, probeDefinitions.size());
  }

  private TransformerDefinitionMatcher createMatcher(
      Collection<MetricProbe> metricProbes,
      Collection<LogProbe> logProbes,
      Collection<SpanProbe> spanProbes) {
    return new TransformerDefinitionMatcher(
        new Configuration(SERVICE_NAME, metricProbes, logProbes, spanProbes));
  }

  private static List<ProbeDefinition> match(TransformerDefinitionMatcher matcher, Class<?> clazz) {
    return matcher.match(clazz, getClassPath(clazz), clazz.getName(), getClassFileBytes(clazz));
  }

  private LogProbe createProbe(String probeId, String typeName, String methodName) {
    return LogProbe.builder().probeId(probeId).where(typeName, methodName).build();
  }

  private LogProbe createProbe(String probeId, String sourceFileName, int line) {
    return LogProbe.builder()
        .probeId(probeId)
        .where(null, null, null, line, sourceFileName)
        .build();
  }

  private MetricProbe createMetric(String probeId, String typeName, String methodName) {
    return MetricProbe.builder()
        .probeId(probeId)
        .where(typeName, methodName)
        .metricName("count")
        .kind(MetricProbe.MetricKind.COUNT)
        .build();
  }

  private static String getClassPath(Class<?> clazz) {
    return clazz.getName().replace('.', '/');
  }
}
