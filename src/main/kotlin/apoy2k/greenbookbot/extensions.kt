package apoy2k.greenbookbot

import apoy2k.greenbookbot.model.Fav
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.RestAction
import org.apache.commons.rng.sampling.DiscreteProbabilityCollectionSampler
import org.apache.commons.rng.simple.RandomSource
import java.awt.Color

val RNG = RandomSource.JDK.create()!!

suspend fun <T> RestAction<T>.await(): T = this.submit().await()

suspend fun SlashCommandInteractionEvent.replyError(message: String, favId: String? = null) {
    this.replyEmbeds(
        EmbedBuilder()
            .setDescription(message)
            .setFooter(favId)
            .setColor(Color(150, 25, 25))
            .build()
    )
        .setEphemeral(true)
        .await()
}

fun Collection<Fav>.weightedRandom(): Fav? {
    if (this.isEmpty()) {
        return null
    }

    val map = this.associateWith { 1 / (it.used.toDouble() + 1) }
    return try {
        DiscreteProbabilityCollectionSampler(RNG, map).sample()
    } catch (e: IndexOutOfBoundsException) {
        null
    }
}

fun EmbedBuilder.forMessage(message: Message, favId: String? = ""): EmbedBuilder = with(this) {
    val channelName = message.channel.name
    setAuthor("${message.author.name} in #$channelName", message.jumpUrl, message.author.effectiveAvatarUrl)
    setColor(Color(80, 150, 25))
    setDescription(message.contentRaw)
    setTimestamp(message.timeCreated)

    val embedImageUrl = message.attachments
        .firstOrNull { it.isImage }
        ?.proxyUrl
        ?.also { setImage(it) }

    message.attachments
        .filter { embedImageUrl != null && it.proxyUrl != embedImageUrl }
        .forEach {
            var description = ""
            if (it.description != null) {
                description = "${it.description}: "
            }
            appendDescription("\n$description${it.proxyUrl}")
        }

    if (!favId.isNullOrBlank()) {
        setFooter(favId)
    }

    this
}
