/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.serviceproxy.testmodel;

import io.vertx.codegen.annotations.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.testmodel.impl.TestServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@ProxyGen
@VertxGen
public interface TestService {

  static TestService create(Vertx vertx) {
    return new TestServiceImpl(vertx);
  }

  Future<String> longDeliverySuccess();

  Future<String> longDeliveryFailed();

  Future<TestConnection> createConnection(String str);

  Future<TestConnectionWithCloseFuture> createConnectionWithCloseFuture();

  void noParams();

  void basicTypes(String str, byte b, short s, int i, long l, float f, double d, char c, boolean bool);

  void basicBoxedTypes(String str, Byte b, Short s, Integer i, Long l, Float f, Double d, Character c,
                       Boolean bool);

  void basicBoxedTypesNull(@Nullable String str, @Nullable Byte b, @Nullable Short s, @Nullable Integer i, @Nullable Long l, @Nullable Float f, @Nullable Double d, @Nullable Character c,
                           @Nullable Boolean bool);

  void jsonTypes(JsonObject jsonObject, JsonArray jsonArray);

  void jsonTypesNull(@Nullable JsonObject jsonObject, @Nullable JsonArray jsonArray);

  void enumType(SomeEnum someEnum);

  void enumTypeNull(@Nullable SomeEnum someEnum);

  Future<SomeEnum> enumTypeAsResult();

  Future<@Nullable SomeEnum> enumTypeAsResultNull();

  void dataObjectType(TestDataObject options);

  void dataObjectTypeNull(@Nullable TestDataObject options);

  void listParams(List<String> listString, List<Byte> listByte, List<Short> listShort, List<Integer> listInt, List<Long> listLong, List<JsonObject> listJsonObject, List<JsonArray> listJsonArray, List<TestDataObject> listDataObject);

  void setParams(Set<String> setString, Set<Byte> setByte, Set<Short> setShort, Set<Integer> setInt, Set<Long> setLong, Set<JsonObject> setJsonObject, Set<JsonArray> setJsonArray, Set<TestDataObject> setDataObject);

  void mapParams(Map<String, String> mapString, Map<String, Byte> mapByte, Map<String, Short> mapShort, Map<String, Integer> mapInt, Map<String, Long> mapLong, Map<String, JsonObject> mapJsonObject, Map<String, JsonArray> mapJsonArray);

  Future<String> stringHandler();

  Future<@Nullable String> stringNullHandler();

  Future<Byte> byteHandler();

  Future<@Nullable Byte> byteNullHandler();

  Future<Short> shortHandler();

  Future<@Nullable Short> shortNullHandler();

  Future<Integer> intHandler();

  Future<@Nullable Integer> intNullHandler();

  Future<Long> longHandler();

  Future<@Nullable Long> longNullHandler();

  Future<Float> floatHandler();

  Future<@Nullable Float> floatNullHandler();

  Future<Double> doubleHandler();

  Future<@Nullable Double> doubleNullHandler();

  Future<Character> charHandler();

  Future<@Nullable Character> charNullHandler();

  Future<Boolean> booleanHandler();

  Future<@Nullable Boolean> booleanNullHandler();

  Future<JsonObject> jsonObjectHandler();

  Future<@Nullable JsonObject> jsonObjectNullHandler();

  Future<JsonArray> jsonArrayHandler();

  Future<@Nullable JsonArray> jsonArrayNullHandler();

  Future<TestDataObject> dataObjectHandler();

  Future<@Nullable TestDataObject> dataObjectNullHandler();

  Future<Void> voidHandler();

  @Fluent
  TestService fluentNoParams();

  Future<JsonObject> failingMethod();

  Future<String> invokeWithMessage(JsonObject object, String str, int i, char chr, SomeEnum senum);

  Future<List<String>> listStringHandler();

  Future<List<Byte>> listByteHandler();

  Future<List<Short>> listShortHandler();

  Future<List<Integer>> listIntHandler();

  Future<List<Long>> listLongHandler();

  Future<List<Float>> listFloatHandler();

  Future<List<Double>> listDoubleHandler();

  Future<List<Character>> listCharHandler();

  Future<List<Boolean>> listBoolHandler();

  Future<List<JsonObject>> listJsonObjectHandler();

  Future<List<JsonArray>> listJsonArrayHandler();

  Future<List<TestDataObject>> listDataObjectHandler();

  Future<Set<String>> setStringHandler();

  Future<Set<Byte>> setByteHandler();

  Future<Set<Short>> setShortHandler();

  Future<Set<Integer>> setIntHandler();

  Future<Set<Long>> setLongHandler();

  Future<Set<Float>> setFloatHandler();

  Future<Set<Double>> setDoubleHandler();

  Future<Set<Character>> setCharHandler();

  Future<Set<Boolean>> setBoolHandler();

  Future<Set<JsonObject>> setJsonObjectHandler();

  Future<Set<JsonArray>> setJsonArrayHandler();

  Future<Set<TestDataObject>> setDataObjectHandler();

  @ProxyIgnore
  void ignoredMethod();
}
