package apoy2k.greenbookbot.listener

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.RestAction
import java.awt.Color

suspend fun <T> RestAction<T>.await(): T = this.submit().await()

suspend fun SlashCommandInteractionEvent.replyError(message: String) {
    this.replyEmbeds(
        EmbedBuilder()
            .setDescription(message)
            .setColor(Color(150, 25, 25))
            .build()
    )
        .setEphemeral(true)
        .await()
}
