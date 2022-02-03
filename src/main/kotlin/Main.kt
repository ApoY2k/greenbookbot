package apoy2k.greenbookbot

import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.LoggerFactory

val LOG = LoggerFactory.getLogger("Main")!!
val ENV = Dotenv.configure().load()!!

fun main(args: Array<String>) {
    LOG.info("Starting up GreenBookBot")

    try {
        val jda = JDABuilder.createDefault(ENV["AUTH_TOKEN"])
            .addEventListeners(
                ReactionListener(),
                CommandListener(),
            )
            .build()

        jda.awaitReady()

        CommandListener.init(jda)

    } catch (e: Exception) {
        LOG.error(e.message, e)
    }
}
