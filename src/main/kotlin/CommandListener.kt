package apoy2k.greenbookbot

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class CommandListener : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    companion object {
        @JvmStatic
        private val FAV = "fav"

        @JvmStatic
        fun init(action: CommandListUpdateAction): CompletableFuture<Void> = with(action) {
            addCommands(
                Commands.slash(FAV, "Post a random fav")
            )
            submit()
                .thenAccept { }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == FAV) {
            event.reply("<insert random fav>").submit()
        }
    }
}
