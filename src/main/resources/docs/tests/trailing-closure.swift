//#Patterns: trailing-closure

//#Issue: {"severity": "Error", "line": 7, "patternId": "trailing-closure"}

reversed = names.sort { s1, s2 in return s1 > s2 }

reversed = names.sort({ s1, s2 in return s1 > s2 })
