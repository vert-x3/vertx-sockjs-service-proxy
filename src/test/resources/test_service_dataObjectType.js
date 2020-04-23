var eb = new EventBus();

eb.onopen = function () {
  var testService = new TestService(eb, 'someaddress');

  testService.dataObjectType({
    "string": "foo",
    "number": 123,
    "bool": true
  });
};
