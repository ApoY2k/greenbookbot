package apoy2k.greenbookbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory

class CommandListener : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    companion object {
        @JvmStatic
        private val FAV = "fav"

        @JvmStatic
        fun init(jda: JDA) {
            val commands = jda.updateCommands()
            commands.addCommands(
                Commands.slash(FAV, "Post a random fav")
            )
            commands.queue()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == FAV) {
            log.debug("Noticed /fav command")
        }
    }
}
