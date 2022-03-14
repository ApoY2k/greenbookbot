package apoy2k.greenbookbot.commands

import apoy2k.greenbookbot.await
import apoy2k.greenbookbot.model.Storage
import apoy2k.greenbookbot.replyError
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

private const val OPTION_TAG = "tag"

val ListCommand = Commands.slash("list", "List amount of favs per tag")
    .addOption(
        OptionType.STRING,
        OPTION_TAG,
        "Limit the listed counts to favs with at least one of these (space-separated) tags"
    )

suspend fun executeListCommand(storage: Storage, event: SlashCommandInteractionEvent) {
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

    event
        .reply("Listing total of ${tagCount.size} tags")
        .await()

    tagCount
        .entries
        .sortedBy { it.key }
        .chunked(25)
        .chunked(10)
        .forEach { messageEntries ->
            val embeds = mutableListOf<MessageEmbed>()
            messageEntries.forEach { fields ->
                val builder = EmbedBuilder()
                fields.forEach { builder.addField(it.key, it.value.toString(), true) }
                embeds.add(builder.build())
            }
            event
                .channel
                .sendMessage(MessageBuilder().setEmbeds(embeds).build())
                .await()
        }
}
