package apoy2k.greenbookbot

import java.util.concurrent.CompletableFuture

interface Storage {
    fun addTagsToRecentFav(tags: String): CompletableFuture<Void>
}
