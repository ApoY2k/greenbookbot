package apoy2k.greenbookbot

interface Storage {
    suspend fun addTagsToRecentFav(userId: String, tags: Collection<String>)
    suspend fun saveNewFav(userId: String, guildId: String, channelId: String, messageId: String, authorId: String)
    suspend fun getFavs(userId: String, guildId: String?, tags: Collection<String>): List<Fav>
    suspend fun removeFav(favId: String)
    suspend fun overwriteTags(userId: String, messageId: String)
    suspend fun getFav(favId: String): Fav?
}
