//#Patterns: angle-bracket-whitespace

//#Issue: {"severity": "Info", "line": 14, "patternId": "angle-bracket-whitespace"}
//#Issue: {"severity": "Info", "line": 14, "patternId": "angle-bracket-whitespace"}
//#Issue: {"severity": "Info", "line": 14, "patternId": "angle-bracket-whitespace"}

func simpleMax<T: Comparable>(x: T, _ y: T) -> T {
    if x < y {
        return y
    }
    return x
}

func simpleMax < T: Comparable >(x: T, _ y: T) -> T {
    if x < y {
        return y
    }
    return x
}
