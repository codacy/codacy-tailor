//#Patterns: forced-type-cast

//#Issue: {"severity": "Info", "line": 5, "patternId": "forced-type-cast"}

let movie = item as! Movie
print("Movie: '\(movie.name)', dir. \(movie.director)")
