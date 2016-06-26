//#Patterns: arrow-whitespace

//#Issue: {"severity": "Error", "line": 17, "patternId": "arrow-whitespace"}
//#Issue: {"severity": "Error", "line": 17, "patternId": "arrow-whitespace"}
//#Issue: {"severity": "Error", "line": 22, "patternId": "arrow-whitespace"}
//#Issue: {"severity": "Error", "line": 22, "patternId": "arrow-whitespace"}

func onePlusTwo() -> Int {
  return 1 + 2
}

names.map() {
  (name) -> Int in
  return 1
}

func onePlusTwo()->Int {
  return 1 + 2
}

names.map() {
  (name)  ->  Int in
  return 1
}
