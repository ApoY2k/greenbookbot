package apoy2k.greenbookbot.model

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

val autoIncrementId = AtomicInteger(1)

class MemoryStorage : Storage {
    private val log = LoggerFactory.getLogger(this::class.java)!!
    private val storage = ArrayList<Fav>()

    override suspend fun saveNewFav(
        userId: String,
        guildId: String,
        channelId: String,
        messageId: String,
        authorId: String
    ): String {
        log.info("Creating fav for User[$userId] Guild[$guildId] Channel[$channelId] Message[$messageId]")
        val fav = Fav(
            autoIncrementId.getAndIncrement().toString(),
            userId,
            guildId,
            channelId,
            messageId,
            authorId,
            mutableListOf(),
            0
        )
        storage.add(fav)
        return fav.id
    }

    override suspend fun getFavs(userId: String, guildId: String?, tags: Collection<String>): List<Fav> {
        log.info("Getting favs for User[$userId] Guild[$guildId] Tags$tags")
        return storage
            .filter { it.userId == userId }
            .filter { guildId == null || it.guildId == guildId }
            .filter { tags.isEmpty() || it.tags.any { tag -> tags.contains(tag) } }
    }

    override suspend fun removeFav(favId: String) {
        log.info("Removing Fav[$favId]")
        storage.removeAll { it.id == favId }
    }

    override suspend fun writeTags(favId: String, tags: Collection<String>) {
        log.info("Setting tags of Fav[$favId] to $tags")
        storage.replaceAll {
            when (it.id) {
                favId -> Fav(
                    it.id,
                    it.userId,
                    it.guildId,
                    it.channelId,
                    it.messageId,
                    it.authorId,
                    tags,
                    it.used
                )
                else -> it
            }
        }
    }

    override suspend fun getFav(favId: String): Fav? {
        log.info("Fetching Fav[$favId]")
        return storage.firstOrNull { it.id == favId }
    }

    override suspend fun increaseUsed(fav: Fav) {
        storage.replaceAll {
            when (it.id) {
                fav.id -> Fav(
                    it.id,
                    it.userId,
                    it.guildId,
                    it.channelId,
                    it.messageId,
                    it.authorId,
                    it.tags,
                    it.used + 1
                )
                else -> it
            }
        }
    }
}
