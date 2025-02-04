/*
 * Copyright (C) 2024 adidas AG.
 */

package com.ajustadoati.sc.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static java.util.Objects.nonNull;

/**
 * Utility class for Json conversion
 *
 * @author rojasric
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestFileUtils {

  private final static ClassLoader CLASS_LOADER = Thread
      .currentThread()
      .getContextClassLoader();

  /**
   * Loads Json from the specified file and converts it to the target object.
   *
   * @param fileName File name.
   * @param valueTypeRef Type reference of the target object.
   * @param <T> Type of the target object.
   * @return Object.
   */
  public static <T> T getJsonAndConvert(String fileName, TypeReference<T> valueTypeRef) {
    var json = TestFileUtils.getJson(fileName);
    return TestFileUtils.convertJsonToObject(json, valueTypeRef);
  }

  /**
   * Loads Json string from the specified file.
   *
   * @param fileName File name.
   * @return File content.
   */
  public static String getJson(String fileName) {
    String content = null;
    try {
      var fileResource = CLASS_LOADER.getResource(fileName);
      if (nonNull(fileResource)) {
        var filePath = new File(fileResource.getFile()).getAbsolutePath();
        content = FileUtils.readFileToString(new File(filePath.replaceAll("%20", " ")),
            Charset.defaultCharset());
      }
    } catch (IOException e) {
      log.error("Error occurred while getJson for the file: " + fileName, e);
    }
    return content;
  }

  /**
   * Converts Json string value to specified Object.
   *
   * @param json Json value that needs to be converted.
   * @param valueTypeRef Type ref for the target object conversion.
   * @return Object.
   */
  public static <T> T convertJsonToObject(String json, TypeReference<T> valueTypeRef) {
    T convertedObj = null;
    try {
      final ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      convertedObj = objectMapper.readValue(json, valueTypeRef);
    } catch (IOException e) {
      log.error("Error occurred while convertJsonToObject for the json: " + json, e);
    }
    return convertedObj;
  }

  /**
   * Converts an object to json string
   * @param o object to convert to json string
   * @return json string
   */
  public static String convertObjectToJson(Object o) {
    String convertedObj = null;
    try {
      final ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.registerModule(new Jdk8Module());
      convertedObj = objectMapper.writeValueAsString(o);
    } catch (IOException e) {
      log.error("Error occurred while convertObjectToJson for the object: " + o, e);
    }
    return convertedObj;
  }
}
