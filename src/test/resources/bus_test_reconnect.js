var bus = new EventBus();
bus.enableReconnect(true);

bus.onopen = function () {
  vertx.setTimer(250, function () {
  	bus.sockJSConn.close();
  });
};

bus.onreconnect = function () {
  bus.send("the_address", {"body":"the_message"});
};
