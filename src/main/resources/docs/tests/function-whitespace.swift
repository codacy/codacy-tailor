//#Patterns: function-whitespace

// #Issue: {"severity": "Info", "line": 9, "patternId": "function-whitespace"}
// #Issue: {"severity": "Info", "line": 12, "patternId": "function-whitespace"}

func function1() {
  var text = 1
  var text = 2
}
function1()
// a comment
func function2() {
  // something goes here
}
