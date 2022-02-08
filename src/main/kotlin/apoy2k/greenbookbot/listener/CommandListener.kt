package apoy2k.greenbookbot.listener

import apoy2k.greenbookbot.Env
import apoy2k.greenbookbot.model.Storage
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory
import java.awt.Color

private const val COMMAND_FAV = "fav"
private const val COMMAND_LIST = "list"
private const val COMMAND_HELP = "help"
private const val OPTION_TAG = "tag"

private const val HELP_TEXT = """
**GreenBookBot** allows you to fav messages and re-post them later by referencing tags set on fav creation.

**Creating a fav**
React with :green_book: on any posted message and then set the tags for the fav by replying to the bots message.

**Posting a fav**
Use the `/fav` command to post a random fav from your whole list.
If you also add a (space-separated) list of tags, the posted fav will be selected (randomly) only from favs that have any of the provided tags.

**Removing a fav**
React with :wastebasket: on the posted fav message from the bot.

**Listing favs**
Use the `/list` command to get a list of all your used tags of all favs.
As with `/fav`, provide a (space-separated) list of tags to limit the list to only those tags.

**Editing tags on a fav**
React with :label: on the posted fav to re-set all tags for this fav.
"""

private val COMMANDS = listOf(
    Commands.slash(COMMAND_FAV, "Post a random fav, filtered by the given tags or from all favs")
        .addOption(
            OptionType.STRING,
            OPTION_TAG,
            "Limit the favs to only include favs with at least one of these (comma-separated) tags"
        ),
    Commands.slash(COMMAND_LIST, "List amount of favs per tag")
        .addOption(
            OptionType.STRING,
            OPTION_TAG,
            "Limit the listed counts to favs with at least one of these (comma-separated) tags"
        ),
    Commands.slash(COMMAND_HELP, "Display usage help")
)

class CommandListener(
    private val storage: Storage
) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(this::class.java)!!

    suspend fun initCommands(jda: JDA, env: Env) {
        if (env.deployCommandsGobal == "true") {
            log.info("Initializing commands globally")
            jda.updateCommands().addCommands(COMMANDS).await()
        } else {
            jda.guilds.forEach {
                log.info("Initializing commands on [$it]")
                it.updateCommands().addCommands(COMMANDS).await()
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = runBlocking {
        when (event.name) {
            COMMAND_FAV -> postFav(event)
            COMMAND_HELP -> help(event)
            COMMAND_LIST -> list(event)
            else -> Unit
        }
    }

    private suspend fun postFav(event: SlashCommandInteractionEvent) {
        val tags = event.getOption(OPTION_TAG)?.asString?.split(" ").orEmpty()
        val guildIds = event.jda.guilds.map { it.id }
        val fav = storage
            .getFavs(event.user.id, event.guild?.id, tags)
            .filter { guildIds.contains(it.guildId) }
            .randomOrNull()
            ?: return event.replyError("No favs found")

        val message = event.jda.guilds
            .firstOrNull { it.id == fav.guildId }
            ?.getTextChannelById(fav.channelId)
            ?.retrieveMessageById(fav.messageId)
            ?.await()
            ?: return event.replyError("Original message could not be found")

        with(message) {
            event.replyEmbeds(
                EmbedBuilder()
                    .setAuthor(author.name, null, author.avatarUrl)
                    .setColor(Color(80, 150, 25))
                    .setDescription(contentRaw)
                    .setFooter(fav.id)
                    .setTimestamp(timeCreated)
                    .build()
            ).await()
        }
    }

    private suspend fun help(event: SlashCommandInteractionEvent) {
        event.replyEmbeds(
            EmbedBuilder()
                .setColor(Color(25, 80, 150))
                .setDescription(HELP_TEXT)
                .build()
        )
            .setEphemeral(true)
            .await()
    }

    private suspend fun list(event: SlashCommandInteractionEvent) {
        val tags = event.getOption(OPTION_TAG)?.asString.orEmpty().split(" ").filter { it.isNotBlank() }
        val favs = storage.getFavs(event.user.id, event.guild?.id, tags)

        if (favs.isEmpty()) {
            return event.replyError("No favs found")
        }

        val builder = EmbedBuilder()
        val tagCount = mutableMapOf<String, Int>()
        favs
            .forEach { fav ->
                fav.tags.forEach {
                    val count = tagCount[it] ?: 0
                    tagCount[it] = count + 1
                }
            }
        tagCount.forEach { builder.addField(it.key, it.value.toString(), false) }

        event
            .replyEmbeds(builder.build())
            .setEphemeral(true)
            .await()
    }
}
