package apoy2k.greenbookbot.listener

import apoy2k.greenbookbot.Env
import apoy2k.greenbookbot.model.Fav
import apoy2k.greenbookbot.model.Storage
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
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
private const val OPTION_ID = "id"

private const val HELP_TEXT = """
**GreenBookBot** allows you to fav messages and re-post them later by referencing tags set on fav creation.
https://github.com/ApoY2k/greenbookbot

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
        )
        .addOption(
            OptionType.STRING,
            OPTION_ID,
            "Post the fav associated with this specific ID"
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
        try {
            when (event.name) {
                COMMAND_FAV -> postFav(event)
                COMMAND_HELP -> help(event)
                COMMAND_LIST -> list(event)
                else -> Unit
            }
        } catch (e: Exception) {
            event.replyError("Something did a whoopsie:\n${e.message ?: "Unknown error"}")
            log.error(e.message, e)
        }
    }

    private suspend fun postFav(event: SlashCommandInteractionEvent) {
        val id = event.getOption(OPTION_ID)?.asString.orEmpty()
        val tags = event.getOption(OPTION_TAG)?.asString?.split(" ").orEmpty()
        val guildIds = event.jda.guilds.map { it.id }

        val candidates = mutableListOf<Fav>()
        if (id.isNotBlank()) {
            val fav = storage.getFav(id)
                ?: return event.replyError("Fav with id [$id] not found")

            if (fav.userId != event.user.id) {
                return event.replyError("That fav does not belong to you!")
            }

            candidates.add(fav)
        } else {
            storage
                .getFavs(event.user.id, event.guild?.id, tags)
                .filter { guildIds.contains(it.guildId) }
                .also { candidates.addAll(it) }
        }

        val fav = candidates.randomOrNull()
            ?: return event.replyError("No favs found")

        val guild = event.jda.guilds
            .firstOrNull { it.id == fav.guildId }
            ?: return

        var channel: GuildMessageChannel? = guild.textChannels
            .firstOrNull { it.id == fav.channelId }

        if (channel == null) {
            channel = guild.threadChannels
                .firstOrNull { it.id == fav.channelId }
        }

        if (channel == null) {
            return event.replyError("Channel not found:\n${fav.url()}", fav.id)
        }

        log.debug("Retrieving message for [$fav]")
        val message = retrieveMessage(event, channel, fav) ?: return

        with(message) {
            val builder = EmbedBuilder()
                .setAuthor(author.name, message.jumpUrl, author.avatarUrl)
                .setColor(Color(80, 150, 25))
                .setDescription(contentRaw)
                .setFooter(fav.id)
                .setTimestamp(timeCreated)

            val embedImageUrl = attachments
                .firstOrNull { it.isImage }
                ?.proxyUrl
                ?.also { builder.setImage(it) }

            attachments
                .filter { embedImageUrl != null && it.proxyUrl != embedImageUrl }
                .forEach {
                    var description = ""
                    if (it.description != null) {
                        description = "${it.description}: "
                    }
                    builder.appendDescription("\n$description${it.proxyUrl}")
                }

            event.replyEmbeds(builder.build()).await()
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

        val tagCount = mutableMapOf<String, Int>()
        favs
            .forEach { fav ->
                fav.tags.forEach {
                    val count = tagCount[it] ?: 0
                    tagCount[it] = count + 1
                }
            }

        val embeds = mutableListOf<MessageEmbed>()
        tagCount
            .entries
            .chunked(25)
            .forEach { chunk ->
                val builder = EmbedBuilder()
                chunk.forEach {
                    builder.addField(it.key, it.value.toString(), true)
                }
                embeds.add(builder.build())
            }

        event
            .replyEmbeds(embeds)
            .setEphemeral(true)
            .await()
    }

    private suspend fun retrieveMessage(
        event: SlashCommandInteractionEvent,
        channel: GuildMessageChannel,
        fav: Fav
    ): Message? {
        try {
            return channel.retrieveMessageById(fav.messageId).await()
        } catch (e: Exception) {
            with(e.message.orEmpty()) {
                if (contains("10008: Unknown Message")) {
                    event.replyError(
                        "Fav [${fav.id}] points to a removed message.\n"
                                + "It will be removed so this doesn't happen again.",
                        fav.id
                    )
                    storage.removeFav(fav.id)
                    return null
                }

                if (contains("Missing permission")) {
                    event.replyError(
                        "No permission to channel:\n${fav.url()}\nPlease check my privileges.",
                        fav.id
                    )
                    return null
                }
            }

            throw e
        }
    }
}
