## Overview ##

TODO

## Basic test ##

This test shows how to set up a simple page and use QUnit to run some tests. It
also add the PrototypeFramework library to be used in the future.

```
<html>
  <head>
    <script type="text/javascript">
      include("lib:/prototype.js");

      module("core");
        /* This test will fail because need an additional assertion. */
        test("test1", function() {
          expect(3);

          ok( true, "test running before jquery loads");
          ok( true, "test running before jquery loads");
        });

        /* This test will pass successfully. */
        test("test2", function() {
          ok( true, "test running after jquery loads");
        });
    </script>
  </head>
  <body>
    <!-- This is the unique required constraint to make QMP work as expected.
      QUnit will report part of the tests result in this special tag.-->
    <div id="qunit-tests" class="result"></div>
  </body>
</html>
```
