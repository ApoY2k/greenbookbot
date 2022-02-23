package apoy2k.greenbookbot.listener

import apoy2k.greenbookbot.Env
import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.commands.*
import apoy2k.greenbookbot.model.Storage
import apoy2k.greenbookbot.replyError
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

private val slashCommandData = listOf(
    HelpCommand,
    ListCommand,
    FavCommand,
    QuoteCommand,
    StatsCommand,
    GuildStatsCommand,
    MysteryFavCommand,
)

class CommandListener(
    private val storage: Storage
) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    suspend fun initCommands(jda: JDA, env: Env) {
        if (env.deployCommandsGobal == "true") {
            log.info("Initializing commands globally")
            jda.updateCommands().addCommands(slashCommandData).await()
        } else {
            jda.guilds.forEach {
                log.info("Initializing commands on [$it]")
                it.updateCommands().addCommands(slashCommandData).await()
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = runBlocking {
        try {
            when (event.name) {
                HelpCommand.name -> executeHelpCommand(event)
                ListCommand.name -> executeListCommand(storage, event)
                FavCommand.name -> executeFavCommand(storage, event)
                QuoteCommand.name -> executeQuoteCommand(event)
                StatsCommand.name -> executeStatsCommand(storage, event)
                GuildStatsCommand.name -> executeGuildStats(storage, event)
                MysteryFavCommand.name -> executeMysteryFavCommand(storage, event)
                else -> Unit
            }
        } catch (e: Exception) {
            event.replyError("Whoopsie (╯°□°）╯︵ ┻━┻\n${e.message ?: "Unknown error"}")
            log.error(e.message, e)
        }
    }

}
