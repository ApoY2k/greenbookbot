package apoy2k.greenbookbot.listener

import apoy2k.greenbookbot.model.Fav
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
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
    val map = this.associateWith { 1 / (it.used.toDouble() + 1) }
    return try {
        DiscreteProbabilityCollectionSampler(RNG, map).sample()
    } catch (e: IndexOutOfBoundsException) {
        null
    }
}
