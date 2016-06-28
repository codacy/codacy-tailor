//#Patterns: arrow-whitespace

//#Issue: {"severity": "Info", "line": 17, "patternId": "arrow-whitespace"}
//#Issue: {"severity": "Info", "line": 17, "patternId": "arrow-whitespace"}
//#Issue: {"severity": "Info", "line": 22, "patternId": "arrow-whitespace"}
//#Issue: {"severity": "Info", "line": 22, "patternId": "arrow-whitespace"}

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
