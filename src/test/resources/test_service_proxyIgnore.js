var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  if (testService.proxyIgnore !== undefined) {
    vertx.eventBus().send("testaddress", "proxy ignore method not ignored");
  } else {
    vertx.eventBus().send("testaddress", "ok");
  }
};
