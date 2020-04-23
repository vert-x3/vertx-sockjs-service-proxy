var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.jsonTypes({"foo": "bar"}, ["wibble"]);
};
