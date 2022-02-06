package apoy2k.greenbookbot

import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.LoggerFactory

fun main() = runBlocking {
    val env = Dotenv.configure().load()!!
    val log = LoggerFactory.getLogger("Main")!!

    try {
        log.info("Starting up GreenBookBot")

        val storage = MemoryStorage()
        val favListener = FavListener(storage)
        val commandListener = CommandListener()

        val jda = JDABuilder.createDefault(env["AUTH_TOKEN"])
            .addEventListeners(favListener, commandListener)
            .build()
        jda.awaitReady()
        initCommands(jda, env)
    } catch (e: Exception) {
        log.error(e.message, e)
    }
}
