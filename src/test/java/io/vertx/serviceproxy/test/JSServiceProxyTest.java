package io.vertx.serviceproxy.test;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.Addresses;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.testmodel.TestService;
import io.vertx.test.core.VertxTestBase;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JSServiceProxyTest extends VertxTestBase {

  TestService service;
  MessageConsumer<JsonObject> consumer;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = TestService.create(vertx);
    consumer = ProxyHelper.registerService(TestService.class, vertx, service, Addresses.SERVICE_ADDRESS);
    vertx.eventBus().<String>consumer(Addresses.TEST_ADDRESS).handler(msg -> {
      assertEquals("ok", msg.body());
      testComplete();
    });
  }

  private static void eval(Context context, String filename) {
    try {
      context.eval(
        Source.newBuilder(
          "js",
          new InputStreamReader(JSBusTest.class.getResourceAsStream(filename)), filename).build());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void execute(String script) {
    try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
      context.getBindings("js").putMember("vertx", vertx);
      // load sockjs mock
      eval(context, "/node_modules/sockjs-client.js");
      // load sockjs client
      eval(context, "/vertx-js/vertx-web-client.js");
      // load the service dependencies
      eval(context, "/test-js/test_connection-proxy.js");
      eval(context, "/test-js/test_connection_with_close_future-proxy.js");
      // load the service
      eval(context, "/test-js/test_service-proxy.js");

      // load the test
      vertx.runOnContext(v -> eval(context, script));
      await();
    }
  }

  @Override
  public void tearDown() throws Exception {
    consumer.unregister();
    super.tearDown();
  }

  @Test
  public void testInvalidParams() {
    execute("/test_service_invalidParams.js");
  }

  @Test
  public void testNoParams() {
    execute("/test_service_noParams.js");
  }

  @Test
  public void testBasicTypes() {
    execute("/test_service_basicTypes.js");
  }

  @Test
  public void testBasicBoxedTypes() {
    execute("/test_service_basicBoxedTypes.js");
  }

  @Test
  public void testJsonTypes() {
    execute("/test_service_jsonTypes.js");
  }

  @Test
  public void testEnumType() {
    execute("/test_service_enumType.js");
  }

  @Test
  public void testDataObjectType() {
    execute("/test_service_dataObjectType.js");
  }

  @Test
  public void testListTypes() {
    execute("/test_service_listTypes.js");
  }

  @Test
  public void testSetTypes() {
    execute("/test_service_setTypes.js");
  }

  @Test
  public void testMapTypes() {
    execute("/test_service_mapTypes.js");
  }

  @Test
  public void testStringHandler() {
    execute("/test_service_stringHandler.js");
  }

  @Test
  public void testStringNullHandler() {
    execute("/test_service_stringNullHandler.js");
  }

  @Test
  public void testByteHandler() {
    execute("/test_service_byteHandler.js");
  }

  @Test
  public void testByteNullHandler() {
    execute("/test_service_byteNullHandler.js");
  }

  @Test
  public void testShortHandler() {
    execute("/test_service_shortHandler.js");
  }

  @Test
  public void testShortNullHandler() {
    execute("/test_service_shortNullHandler.js");
  }

  @Test
  public void testIntHandler() {
    execute("/test_service_intHandler.js");
  }

  @Test
  public void testIntNullHandler() {
    execute("/test_service_intNullHandler.js");
  }

  @Test
  public void testLongHandler() {
    execute("/test_service_longHandler.js");
  }

  @Test
  public void testLongNullHandler() {
    execute("/test_service_longNullHandler.js");
  }

  @Test
  public void testFloatHandler() {
    execute("/test_service_floatHandler.js");
  }

  @Test
  public void testFloatNullHandler() {
    execute("/test_service_floatNullHandler.js");
  }

  @Test
  public void testDoubleHandler() {
    execute("/test_service_doubleHandler.js");
  }

  @Test
  public void testDoubleNullHandler() {
    execute("/test_service_doubleNullHandler.js");
  }

  @Test
  public void testCharHandler() {
    execute("/test_service_charHandler.js");
  }

  @Test
  public void testCharNullHandler() {
    execute("/test_service_charNullHandler.js");
  }

  @Test
  public void testJsonObjectHandler() {
    execute("/test_service_jsonObjectHandler.js");
  }

  @Test
  public void testJsonObjectNullHandler() {
    execute("/test_service_jsonObjectNullHandler.js");
  }

  @Test
  public void testJsonArrayHandler() {
    execute("/test_service_jsonArrayHandler.js");
  }

  @Test
  public void testJsonArrayNullHandler() {
    execute("/test_service_jsonArrayNullHandler.js");
  }

  @Test
  public void testDataObjectHandler() {
    execute("/test_service_dataObjectHandler.js");
  }

  @Test
  public void testDataObjectNullHandler() {
    execute("/test_service_dataObjectNullHandler.js");
  }

  @Test
  public void testVoidHandler() {
    execute("/test_service_voidHandler.js");
  }

  @Test
  public void testFluentMethod() {
    execute("/test_service_fluentMethod.js");
  }

  @Test
  public void testFluentNoParams() {
    execute("/test_service_fluentNoParams.js");
  }

  @Test
  public void testFailingMethod() {
    execute("/test_service_failingMethod.js");
  }

  @Test
  public void testListStringHandler() {
    execute("/test_service_listStringHandler.js");
  }

  @Test
  public void testListByteHandler() {
    execute("/test_service_listByteHandler.js");
  }

  @Test
  public void testListShortHandler() {
    execute("/test_service_listShortHandler.js");
  }

  @Test
  public void testListIntHandler() {
    execute("/test_service_listIntHandler.js");
  }

  @Test
  public void testListLongHandler() {
    execute("/test_service_listLongHandler.js");
  }

  @Test
  public void testListFloatHandler() {
    execute("/test_service_listFloatHandler.js");
  }

  @Test
  public void testListDoubleHandler() {
    execute("/test_service_listDoubleHandler.js");
  }

  @Test
  public void testListCharHandler() {
    execute("/test_service_listCharHandler.js");
  }

  @Test
  public void testListBoolHandler() {
    execute("/test_service_listBoolHandler.js");
  }

  @Test
  public void testListJsonObjectHandler() {
    execute("/test_service_listJsonObjectHandler.js");
  }

  @Test
  public void testListJsonArrayHandler() {
    execute("/test_service_listJsonArrayHandler.js");
  }

  @Test
  public void testListDataObjectHandler() {
    execute("/test_service_listDataObjectHandler.js");
  }

  @Test
  public void testSetStringHandler() {
    execute("/test_service_setStringHandler.js");

  }

  @Test
  public void testSetByteHandler() {
    execute("/test_service_setByteHandler.js");
  }

  @Test
  public void testSetShortHandler() {
    execute("/test_service_setShortHandler.js");
  }

  @Test
  public void testSetIntHandler() {
    execute("/test_service_setIntHandler.js");
  }

  @Test
  public void testSetLongHandler() {
    execute("/test_service_setLongHandler.js");
  }

  @Test
  public void testSetFloatHandler() {
    execute("/test_service_setFloatHandler.js");
  }

  @Test
  public void testSetDoubleHandler() {
    execute("/test_service_setDoubleHandler.js");
  }

  @Test
  public void testSetCharHandler() {
    execute("/test_service_setCharHandler.js");
  }

  @Test
  public void testSetBoolHandler() {
    execute("/test_service_setBoolHandler.js");
  }

  @Test
  public void testSetJsonObjectHandler() {
    execute("/test_service_setJsonObjectHandler.js");
  }

  @Test
  public void testSetJsonArrayHandler() {
    execute("/test_service_setJsonArrayHandler.js");
  }

  @Test
  public void testSetDataObjectHandler() {
    execute("/test_service_setDataObjectHandler.js");
  }

  @Test
  public void testProxyIgnore() {
    execute("/test_service_proxyIgnore.js");
  }

  @Test
  public void testConnection() {
    execute("/test_service_connection.js");
  }

  @Test
  public void testConnectionTimeout() {
    consumer.unregister();
    long timeoutSeconds = 2;
    consumer = ProxyHelper.registerService(TestService.class, vertx, service, Addresses.SERVICE_ADDRESS, timeoutSeconds);
    execute("/test_service_connectionTimeout.js");
  }

  @Test
  public void testConnectionWithCloseFutureTimeout() {
    consumer.unregister();
    long timeoutSeconds = 2;
    consumer = ProxyHelper.registerService(TestService.class, vertx, service, Addresses.SERVICE_ADDRESS, timeoutSeconds);
    execute("/test_service_connectionWithCloseFutureTimeout.js");
  }

  @Test
  public void testLongDelivery1() {
    execute("/test_service_longDeliverySuccess.js");
  }

  @Test
  public void testLongDelivery2() {
    execute("/test_service_longDeliveryFailed.js");
  }
}
