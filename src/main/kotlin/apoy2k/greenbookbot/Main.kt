package apoy2k.greenbookbot

import apoy2k.greenbookbot.listener.CommandListener
import apoy2k.greenbookbot.listener.FavListener
import apoy2k.greenbookbot.model.DbStorage
import apoy2k.greenbookbot.model.MemoryStorage
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.LoggerFactory

fun main() = runBlocking {
    val env = Env()
    val log = LoggerFactory.getLogger("apoy2k.greenbot.Main")!!

    try {
        log.info("Starting up GreenBookBot")

        val storage = if (env.dbUrl.isNotBlank()) {
            DbStorage(env)
        } else {
            log.info("No jdbc url found, using memory storage")
            MemoryStorage()
        }

        val favListener = FavListener(storage)
        val commandListener = CommandListener(storage)

        val jda = JDABuilder.createDefault(env.authToken)
            .addEventListeners(favListener, commandListener)
            .build()
        jda.awaitReady()

        commandListener.initCommands(jda, env)
    } catch (e: Exception) {
        log.error(e.message, e)
    }
}
