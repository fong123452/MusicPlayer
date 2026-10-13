package musicplayer.core

/** Search matches grouped by kind, each in Library order. */
data class SearchResults(
    val tracks: List<Track>,
    val albums: List<Album>,
    val artists: List<Artist>,
) {
    val isEmpty: Boolean get() = tracks.isEmpty() && albums.isEmpty() && artists.isEmpty()
}

/** Case-insensitive substring match over Track titles, Album titles and Artist names. A blank query matches nothing. */
fun LibrarySnapshot.search(query: String): SearchResults {
    val needle = query.trim()
    if (needle.isEmpty()) return SearchResults(emptyList(), emptyList(), emptyList())
    fun String.matches() = contains(needle, ignoreCase = true)
    return SearchResults(
        tracks = tracks.filter { it.title.matches() },
        albums = albums.filter { it.title.matches() },
        artists = artists.filter { it.name.matches() },
    )
}
