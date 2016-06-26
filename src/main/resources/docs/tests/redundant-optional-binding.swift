//#Patterns: redundant-optional-binding

//#Issue: {"severity": "Error", "line": 7, "patternId": "redundant-optional-binding"}
//#Issue: {"severity": "Error", "line": 7, "patternId": "redundant-optional-binding"}
//#Issue: {"severity": "Error", "line": 11, "patternId": "redundant-optional-binding"}

if var a = a, var b = b, var c = c where c != 0 {
    print("(a + b) / c = \((a + b) / c)")     // (a + b) / c = 5
}

if let a = a, let b = b, var c = c where c != 0 {
    print("(a + b) / c = \((a + b) / c)")     // (a + b) / c = 5
}
