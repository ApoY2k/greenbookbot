package apoy2k.greenbookbot

import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class MemoryStorage : Storage {
    private val log = LoggerFactory.getLogger(this::class.java)!!

    override fun addTagsToRecentFav(userId: String, tags: Collection<String>): CompletableFuture<Void> {
        log.info("Adding [$tags] to most recent fav of user [$userId]")
        return CompletableFuture.completedFuture(null)
    }

    override fun saveNewFav(userId: String, guildId: String, messageId: String): CompletableFuture<Void> {
        log.info("Saving new fav for user [${userId}] on guild [${guildId}], message [${messageId}]")
        return CompletableFuture.completedFuture(null)
    }

    override fun getFav(userId: String, guildId: String, tags: Collection<String>): CompletableFuture<List<Fav>> {
        return CompletableFuture.completedFuture(emptyList())
    }

    override fun removeFav(userId: String, messageId: String): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }

    override fun overwriteTags(userId: String, messageId: String): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }
}
