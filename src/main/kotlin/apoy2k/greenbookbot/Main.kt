package apoy2k.greenbookbot

import apoy2k.greenbookbot.listener.CommandListener
import apoy2k.greenbookbot.listener.MessageListener
import apoy2k.greenbookbot.listener.ReactionListener
import apoy2k.greenbookbot.model.DbStorage
import apoy2k.greenbookbot.model.MemoryStorage
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
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

        val commandListener = CommandListener(storage)

        val jda = JDABuilder.createDefault(env.authToken)
            .addEventListeners(
                ReactionListener(storage),
                MessageListener(storage),
                commandListener
            )
            .build()

        jda.awaitReady()
        commandListener.initCommands(jda, env)
        jda.presence.setPresence(Activity.watching("out for hot takes"), false)
    } catch (e: Exception) {
        log.error(e.message, e)
    }
}
