package apoy2k.greenbookbot

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

val autoIncrementId = AtomicInteger(1)

class MemoryStorage : Storage {
    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val storage = ArrayList<Fav>()

    override suspend fun addTagsToRecentFav(userId: String, tags: Collection<String>) {
        log.info("Adding $tags to most recent fav of user [$userId]")
        val fav = storage.lastOrNull { it.tags.isEmpty() && it.userId == userId }
        if (fav == null) {
            log.warn("No fav found to add tags to")
            return
        }
        fav.tags.addAll(tags)
    }

    override suspend fun saveNewFav(
        userId: String,
        guildId: String,
        channelId: String,
        messageId: String,
        authorId: String
    ) {
        log.info("Saving new fav for user [${userId}] on guild [${guildId}], message [${messageId}]")
        storage.add(
            Fav(
                autoIncrementId.getAndIncrement().toString(),
                userId,
                guildId,
                channelId,
                messageId,
                authorId,
                mutableListOf()
            )
        )
    }

    override suspend fun getFavs(userId: String, guildId: String?, tags: Collection<String>): List<Fav> {
        return storage
            .filter { it.userId == userId }
            .filter { guildId == null || it.guildId == guildId }
            .filter { tags.isEmpty() || it.tags.any { tag -> tags.contains(tag) } }
    }

    override suspend fun removeFav(favId: String) {
        storage.removeAll { it.id == favId }
    }

    override suspend fun overwriteTags(userId: String, messageId: String) {
    }

    override suspend fun getFav(favId: String): Fav? {
        return storage.firstOrNull { it.id == favId }
    }
}
