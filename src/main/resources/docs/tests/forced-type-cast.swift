//#Patterns: forced-type-cast

//#Issue: {"severity": "Error", "line": 5, "patternId": "forced-type-cast"}

let movie = item as! Movie
print("Movie: '\(movie.name)', dir. \(movie.director)")
