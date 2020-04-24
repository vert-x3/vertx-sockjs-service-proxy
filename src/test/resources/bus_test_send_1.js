var bus = new EventBus();

bus.onopen = function () {
  bus.send("the_address", {"body":"the_message"});
};
