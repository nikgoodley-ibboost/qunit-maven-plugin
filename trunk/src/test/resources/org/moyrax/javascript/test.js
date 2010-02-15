// Init
//load("env.rhino.js");


// Must upgrade Rhino to 1.7R2 for this function to work.
// function crankyLoad() {
//   var context = Packages.org.mozilla.javascript.Context.currentContext;
//   var SourceReader = Packages.org.mozilla.javascript.tools.SourceReader;
//
//   for(var i=0; i < arguments.length; i++) {
//     var filename = arguments[i];
//     var source = SourceReader.readFileOrUrl(filename, true, "UTF-8");
//     context.evaluateString(this, source, filename, 0, null);
//   }
// }


(function(){
  test("test1", function() {
    print("HERE1");
    ok( true, "test running before jquery loads");
  });
  //load("web-app/js/lib/jquery/jquery.js");
  //load("http://code.jquery.com/jquery-latest.js");
  print("HERE2");
  test("test2", function() {
    print("HERE3");
    ok( true, "test running after jquery loads");
  });

   //Tests to run
  //load("test/javascript/views/test_homeView.js");
})();
