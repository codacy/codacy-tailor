//#Patterns: parenthesis-whitespace

//#Issue: {"severity": "Info", "line": 13, "patternId": "parenthesis-whitespace"}
//#Issue: {"severity": "Info", "line": 17, "patternId": "parenthesis-whitespace"}

func sum(a: Int, b: Int) -> Int {
  return a + b;
}

print("Hello, World!")
Not Preferred

func sum ( a: Int, b: Int ) -> Int {
  return a + b;
}

print( "Hello, World!" )
