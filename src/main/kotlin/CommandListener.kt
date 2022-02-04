package apoy2k.greenbookbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.util.concurrent.CompletableFuture

private const val FAV = "fav"
private const val LIST = "list"
private const val HELP = "help"

class CommandListener : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == FAV) {
            event.reply("<insert random fav>").submit()
        }

        if (event.name == LIST) {
            event.reply("<list of tags/favs>").submit()
        }

        if (event.name == HELP) {
            event.reply("<help>").submit()
        }
    }
}

fun initCommands(jda: JDA): CompletableFuture<Void> =
    with(jda.updateCommands()) {
        addCommands(
            Commands.slash(FAV, "Post a fav")
                .addOption(OptionType.STRING, "tag", "Tag(s) to choose fav from"),
            Commands.slash(LIST, "List all tags and information about the tagged favs")
                .addOption(OptionType.STRING, "tag", "Tag(s) to list for"),
            Commands.slash(HELP, "Display usage help")
                .addOption(OptionType.STRING, "function", "Show help for this specific function")
        )
        submit()
            .thenAccept { }
    }
