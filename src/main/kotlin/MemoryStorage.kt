package apoy2k.greenbookbot

import org.slf4j.LoggerFactory

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

    override suspend fun saveNewFav(userId: String, guildId: String, messageId: String) {
        log.info("Saving new fav for user [${userId}] on guild [${guildId}], message [${messageId}]")
        storage.add(Fav(userId, guildId, messageId, mutableListOf()))
    }

    override suspend fun getFavs(userId: String, guildId: String, tags: Collection<String>): List<Fav> {
        return storage
            .filter { it.userId == userId && it.guildId == guildId }
            .filter { tags.isEmpty() || it.tags.any { tag -> tags.contains(tag) } }
    }

    override suspend fun removeFav(userId: String, messageId: String) {
        storage.removeAll { it.userId == userId && it.messageId == messageId }
    }

    override suspend fun overwriteTags(userId: String, messageId: String) {
    }
}
