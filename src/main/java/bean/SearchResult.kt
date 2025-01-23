package bean

data class SearchResult(
    val stock: List<SearchedStock>?,

    val fund: List<SearchedStock>?,
)

data class SearchedStock(
    val code: String,
    val name: String,
) {
    var followed: Boolean = false
    val displayName: String
        get() = (if (followed) "★" else " ") + "$code-$name"
}