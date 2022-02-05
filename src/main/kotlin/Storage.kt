package apoy2k.greenbookbot

import java.util.concurrent.CompletableFuture

interface Storage {
    fun addTagsToRecentFav(userId: String, tags: Collection<String>): CompletableFuture<Void>
    fun saveNewFav(userId: String, guildId: String, messageId: String): CompletableFuture<Void>
    fun getFav(userId: String, guildId: String, tags: Collection<String>): CompletableFuture<List<Fav>>
    fun removeFav(userId: String, messageId: String): CompletableFuture<Void>
    fun overwriteTags(userId: String, messageId: String): CompletableFuture<Void>
}
