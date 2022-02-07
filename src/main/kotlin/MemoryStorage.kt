package apoy2k.greenbookbot

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

val autoIncrementId = AtomicInteger(1)

class MemoryStorage : Storage {
    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val storage = ArrayList<Fav>()

    override suspend fun writeTags(favId: String, tags: Collection<String>) {
        log.info("Adding $tags to fav [$favId]")
        val fav = storage.firstOrNull { it.id == favId }
        if (fav == null) {
            log.warn("No fav found to add tags to")
            return
        }
        fav.tags.clear()
        fav.tags.addAll(tags)
    }

    override suspend fun saveNewFav(
        userId: String,
        guildId: String,
        channelId: String,
        messageId: String,
        authorId: String
    ): String {
        log.info("Saving new fav for user [$userId] on guild [$guildId] channel [$channelId], message [$messageId]")
        val fav = Fav(
            autoIncrementId.getAndIncrement().toString(),
            userId,
            guildId,
            channelId,
            messageId,
            authorId,
            mutableListOf()
        )
        storage.add(fav)
        return fav.id
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

    override suspend fun getFav(favId: String): Fav? {
        return storage.firstOrNull { it.id == favId }
    }
}
