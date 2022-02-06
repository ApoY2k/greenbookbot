package apoy2k.greenbookbot

import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import org.slf4j.LoggerFactory

private const val FAV = "fav"
private const val LIST = "list"
private const val HELP = "help"

private val LOG = LoggerFactory.getLogger("CommandListener")!!

class CommandListener(
    private val storage: Storage
) : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = runBlocking {
        if (event.name == FAV) {
            val tags = event.getOption("tags")?.asString?.split(" ").orEmpty()
            val fav = storage.getFavs(event.user.id, event.guild?.id, tags).random()
            // TODO Retrieve message and post
            // What about posting in PMs with bot?
        }

        if (event.name == LIST) {
            event.reply("<list of tags/favs>").submit()
        }

        if (event.name == HELP) {
            event.reply("<help>").submit()
        }
    }
}

suspend fun initCommands(jda: JDA, dotenv: Dotenv) {
    if (dotenv["DEPLOY_COMMANDS_GLOBAL"] == "true") {
        LOG.info("Initializing commands globally")
        updateCommands(jda.updateCommands())
    }

    jda.guilds.forEach {
        LOG.info("Initializing commands on guild [$it]")
        updateCommands(it.updateCommands())
    }
}

private suspend fun updateCommands(updateCommands: CommandListUpdateAction) {
    with(updateCommands) {
        addCommands(
            Commands.slash(FAV, "Post a fav")
                .addOption(OptionType.STRING, "tag", "Tag(s) to choose fav from"),
            Commands.slash(LIST, "List all tags and information about the tagged favs")
                .addOption(OptionType.STRING, "tag", "Tag(s) to list for"),
            Commands.slash(HELP, "Display usage help")
                .addOption(OptionType.STRING, "function", "Show help for this specific function")
        )
        submit().await()
    }
}
