package apoy2k.greenbookbot

import io.github.cdimascio.dotenv.Dotenv

class Env {
    private val env = Dotenv.configure().ignoreIfMissing().load()!!

    val dbUrl: String
        get() = get("DB_URL")

    val dbUser: String
        get() = get("DB_USER")

    val dbPassword: String
        get() = get("DB_PASSWORD")

    val authToken: String
        get() = get("AUTH_TOKEN")

    val deployCommandsGobal: String
        get() = get("DEPLOY_COMMANDS_GLOBAL")

    private fun get(name: String) = env[name].orEmpty()
}
