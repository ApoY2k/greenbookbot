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
private const val COMMAND_QUOTE = "quote"
private const val COMMAND_STATS = "stats"
private const val COMMAND_STATS_GUILD = "serverstats"
private const val COMMAND_STATS_GLOBAL = "globalstats"
private const val OPTION_TAG = "tag"
private const val OPTION_ID = "id"
private const val OPTION_MESSAGE_LINK = "link"

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

**Quoting messages**
Use the `/quote` command and a message link to embed a quote.
"""

private val COMMANDS = listOf(
    Commands.slash(COMMAND_FAV, "Post a random fav, filtered by the given tags or from all favs")
        .addOption(
            OptionType.STRING,
            OPTION_TAG,
            "Limit the favs to only include favs with at least one of these (space-separated) tags"
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
            "Limit the listed counts to favs with at least one of these (space-separated) tags"
        ),
    Commands.slash(COMMAND_QUOTE, "quote message")
        .addOption(
            OptionType.STRING,
            OPTION_MESSAGE_LINK, "Link to message", true
        ),
    Commands.slash(COMMAND_HELP, "Display usage help"),
    Commands.slash(COMMAND_STATS, "Display your fav stats")
        .addOption(
            OptionType.STRING,
            OPTION_TAG,
            "Limit the listed counts to favs with at least one of these (space-separated) tags"
        ),
    Commands.slash(COMMAND_STATS_GUILD, "Display server-wide fav stats"),
    Commands.slash(COMMAND_STATS_GLOBAL, "Display bot-global fav stats"),
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
                COMMAND_QUOTE -> quote(event)
                COMMAND_STATS -> stats(event)
                COMMAND_STATS_GUILD -> guildStats(event)
                COMMAND_STATS_GLOBAL -> globalStats(event)
                else -> Unit
            }
        } catch (e: Exception) {
            event.replyError("Something did a whoopsie:\n${e.message ?: "Unknown error"}")
            log.error(e.message, e)
        }
    }

    private suspend fun quote(event: SlashCommandInteractionEvent) {
        val messageLink = event.getOption(OPTION_MESSAGE_LINK)?.asString.orEmpty()
        val tokenizedLink = messageLink.substringAfter("/channels/", "").split("/")
        if (tokenizedLink.size != 3) {
            return event.replyError("Invalid link format!")
        }

        val channelId = tokenizedLink[1]
        val messageId = tokenizedLink[2]
        val message = retrieveMessage(event, channelId, messageId)
            ?: return event.replyError("No message found at that link!")

        with(message) {
            val builder = EmbedBuilder()
                .setAuthor(author.name, jumpUrl, author.effectiveAvatarUrl)
                .setColor(Color(80, 150, 25))
                .setDescription(contentRaw)
                .setTimestamp(timeCreated)

            attachImageToBuilder(builder, message)

            event.replyEmbeds(builder.build()).await()
        }
    }

    private suspend fun retrieveMessage(
        event: SlashCommandInteractionEvent,
        channelId: String,
        messageId: String
    ): Message? {
        val channel = event.jda.getTextChannelById(channelId) ?: event.jda.getThreadChannelById(channelId)
        val message = channel?.retrieveMessageById(messageId)?.await()
        log.info("Retrieved message [${message?.id}] from channel [$channel]")
        return message
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

        val fav = candidates.weightedRandom()
            ?: return event.replyError("No favs found")

        storage.increaseUsed(fav)

        val guild = event.jda.guilds
            .firstOrNull { it.id == fav.guildId }
            ?: return event.replyError("Guild not found:\n${fav.guildUrl()}", fav.id)

        var channel: GuildMessageChannel? = guild.textChannels
            .firstOrNull { it.id == fav.channelId }

        if (channel == null) {
            channel = guild.threadChannels
                .firstOrNull { it.id == fav.channelId }
        }

        if (channel == null) {
            return event.replyError("Channel not found:\n${fav.channelUrl()}", fav.id)
        }

        log.debug("Retrieving message for [$fav]")
        val message = retrieveMessage(event, channel, fav) ?: return

        with(message) {
            val builder = EmbedBuilder()
                .setAuthor(author.name, message.jumpUrl, author.effectiveAvatarUrl)
                .setColor(Color(80, 150, 25))
                .setDescription(contentRaw)
                .setFooter(fav.id)
                .setTimestamp(timeCreated)

            attachImageToBuilder(builder, message)

            event.replyEmbeds(builder.build()).await()
        }
    }

    private fun attachImageToBuilder(builder: EmbedBuilder, message: Message) {
        with(message) {
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
            .sortedBy { it.key }
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

    private suspend fun stats(event: SlashCommandInteractionEvent) {
        val tags = event.getOption(OPTION_TAG)?.asString.orEmpty().split(" ").filter { it.isNotBlank() }
        val favs = storage.getFavs(event.user.id, event.guild?.id, tags)

        val usedCount = favs.sumOf { it.used }

        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("${event.user.name} Stats")
                .addField("Amount of favs", favs.count().toString(), true)
                .addField("Amount posted", usedCount.toString(), true)
                .build()
        )
            .await()
    }

    private suspend fun guildStats(event: SlashCommandInteractionEvent) {
        val favs = storage.getFavs(null, event.guild?.id, emptyList())

        val usedCount = favs.sumOf { it.used }

        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("Server Stats")
                .addField("Amount of favs", favs.count().toString(), true)
                .addField("Amount posted", usedCount.toString(), true)
                .build()
        )
            .await()
    }

    private suspend fun globalStats(event: SlashCommandInteractionEvent) {
        val favs = storage.getFavs(null, null, emptyList())

        val usedCount = favs.sumOf { it.used }

        event.replyEmbeds(
            EmbedBuilder()
                .setTitle("Global Stats")
                .addField("Amount of favs", favs.count().toString(), true)
                .addField("Amount posted", usedCount.toString(), true)
                .build()
        )
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
                        "No permission to channel:\n${fav.channelUrl()}\nPlease check my privileges.",
                        fav.id
                    )
                    return null
                }
            }

            throw e
        }
    }
}
