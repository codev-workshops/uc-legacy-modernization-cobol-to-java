package com.carddemo.harness.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class GoldenDataLoader {
  private static final TypeReference<List<Map<String, Object>>> RECORDS =
      new TypeReference<>() {};
  private static final TypeReference<Map<String, Object>> OBJECT = new TypeReference<>() {};
  private final ObjectMapper mapper = new ObjectMapper()
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
  private final RepoPaths paths;

  public GoldenDataLoader() {
    this(new RepoPaths());
  }

  public GoldenDataLoader(RepoPaths paths) {
    this.paths = paths;
  }

  public List<Map<String, Object>> records(String dataset) throws IOException {
    Path path = paths.resolve("golden_data/" + dataset.toLowerCase() + ".json");
    return mapper.readValue(Files.readString(path), RECORDS);
  }

  public Map<String, Object> manifest() throws IOException {
    return mapper.readValue(Files.readString(paths.resolve("golden_data/manifest.json")), OBJECT);
  }

  public Map<String, Object> datasetManifest(String dataset) throws IOException {
    Map<String, Object> datasets = object(manifest().get("datasets"));
    return object(datasets.get(dataset));
  }

  public static Map<String, Object> object(Object value) {
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) value;
    return result;
  }
}
