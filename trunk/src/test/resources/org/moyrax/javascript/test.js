
test("test1", function() {
  print("HERE1");
  ok( true, "test running before jquery loads");
});

print("HERE2");

test("test2", function() {
  print("HERE3");
  ok( true, "test running after jquery loads");
});

include("lib:/env.js");

includePage({
  url : "/mnt/development/src/maven/qunit-maven-plugin/src/test/resources/org/moyrax/javascript/test.html",
  logLevel : Envjs.ERROR | Envjs.WARN
});

