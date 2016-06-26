//#Patterns: comma-whitespace

//#Issue: {"severity": "Error", "line": 10, "patternId": "comma-whitespace"}

func someFunction<T: SomeClass, U: SomeProtocol>(someT: T, someU: U) {
    // function body goes here
}


func someFunction<T: SomeClass,U: SomeProtocol>(someT: T, someU: U) {
    // function body goes here
}
