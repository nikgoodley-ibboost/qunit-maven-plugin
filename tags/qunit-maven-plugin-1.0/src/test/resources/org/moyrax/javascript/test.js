
test("test1", function() {
  print("HERE1");
  ok( true, "test running before jquery loads");
});

print("HERE2");

test("test2", function() {
  print("HERE3");
  ok( true, "test running after jquery loads");
});
