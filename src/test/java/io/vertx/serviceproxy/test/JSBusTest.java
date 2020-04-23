package io.vertx.serviceproxy.test;

import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JSBusTest extends VertxTestBase {

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
      // load the test
      vertx.runOnContext(v -> eval(context, script));
      await();
    }
  }

  @Test
  public void testBusReconnect() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(0, msg.headers().size());
      testComplete();
    });
    execute("/bus_test_reconnect.js");
  }

  @Test
  public void testBusSend1() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(0, msg.headers().size());
      testComplete();
    });
    execute("/bus_test_send_1.js");
  }

  @Test
  public void testBusSend2() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(1, msg.headers().size());
      assertEquals("the_header_value", msg.headers().get("the_header_name"));
      testComplete();
    });
    execute("/bus_test_send_2.js");
  }

  @Test
  public void testBusSend3() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(0, msg.headers().size());
      msg.reply("whatever");
    });
    vertx.eventBus().consumer("done", msg -> testComplete());
    execute("/bus_test_send_3.js");
  }

  @Test
  public void testBusSend4() {
    AtomicInteger count = new AtomicInteger();
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(0, msg.headers().size());
      count.incrementAndGet();
      msg.reply("whatever");
    });
    vertx.eventBus().consumer("the_address_fail", msg -> {
      count.incrementAndGet();
      msg.fail(0, "the_failure");
    });
    vertx.eventBus().consumer("done", msg -> {
      assertEquals(2, count.get());
      testComplete();
    });
    execute("/bus_test_send_4.js");
  }

  @Test
  public void testBusSend5() {
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(1, msg.headers().size());
      assertEquals("the_header_value", msg.headers().get("the_header_name"));
      msg.reply("whatever");
    });
    vertx.eventBus().consumer("done", msg -> testComplete());
    execute("/bus_test_send_5.js");
  }

  @Test
  public void testBusSend6() {
    AtomicInteger count = new AtomicInteger();
    vertx.eventBus().consumer("the_address", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(1, msg.headers().size());
      assertEquals("the_header_value", msg.headers().get("the_header_name"));
      count.incrementAndGet();
      msg.reply("whatever");
    });
    vertx.eventBus().consumer("the_address_fail", msg -> {
      assertEquals(new JsonObject().put("body", "the_message"), msg.body());
      assertEquals(1, msg.headers().size());
      assertEquals("the_header_value_fail", msg.headers().get("the_header_name"));
      count.incrementAndGet();
      msg.fail(0, "the_failure");
    });
    vertx.eventBus().consumer("done", msg -> {
      assertEquals(2, count.get());
      testComplete();
    });
    execute("/bus_test_send_6.js");
  }
}
