package apoy2k.greenbookbot

import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class DmListener : ListenerAdapter() {

    private val logger = LoggerFactory.getLogger(this::class.java)!!

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.channel !is PrivateChannel) {
            return
        }

        val command = event.message.contentRaw
            .split(" ")
            .first()
            .trim()

        when (command) {
            "list-guilds" -> listGuilds(event)
            "init-commands-guild" -> initCommandsGuild(event)
            "init-commands-global" -> initCommandsGlobal(event)
        }
    }

    // List all guilds the bot is currently on
    private fun listGuilds(event: MessageReceivedEvent) {
        val guilds = event.jda.guilds
            .joinToString("\n") { "${it.name}: ${it.id}" }
        event.message.reply(guilds).submit()
    }

    // Initialize commands on guild level for a single guild (by id)
    private fun initCommandsGuild(event: MessageReceivedEvent) {
        val guildId = event.message.contentRaw
            .split(" ")
            .last()
            .trim()
        event.jda.guilds
            .filter { it.id == guildId }
            .forEach { guild ->
                logger.info("Refreshing commands on guild [{}]", guildId)
                guild.retrieveCommands().submit()
                    .thenAccept { result ->
                        result
                            .map { it.id }
                            .forEach { guild.deleteCommandById(it).submit() }
                    }
                CommandListener.init(guild.updateCommands())
                    .thenAccept { event.message.addReaction("✅").submit() }
            }
    }

    private fun initCommandsGlobal(event: MessageReceivedEvent) {
        logger.info("Refreshing commands globally")
        event.jda.retrieveCommands()
            .submit()
            .thenAccept { result ->
                result
                    .map { it.id }
                    .forEach { event.jda.deleteCommandById(it).submit() }
            }

        //CommandListener
        //    .init(event.jda.updateCommands())
        //    .thenAccept { event.message.addReaction("✅").submit() }
    }
}
