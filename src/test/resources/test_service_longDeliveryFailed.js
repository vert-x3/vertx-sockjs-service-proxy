var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.longDeliveryFailed(function (err, res) {
    if (res != null) {
      vertx.eventBus().send("testaddress", "was expecting null result");
    } else if (err.message.indexOf('Timed out ') != 0) {
      vertx.eventBus().send("testaddress", "unexpected error message <" + err.message + ">");
    } else {
      vertx.eventBus().send("testaddress", "ok");
    }
  });
};
