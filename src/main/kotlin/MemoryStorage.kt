package apoy2k.greenbookbot

import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class MemoryStorage : Storage {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    override fun addTagsToRecentFav(tags: String): CompletableFuture<Void> {
        log.info("Adding [{}] to most recent fav", tags)
        return CompletableFuture.completedFuture(null)
    }
}
