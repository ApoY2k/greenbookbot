# 📗 greenbookbot

A bot that allows you to "fav" messages, tag them and quote them later, so you never lose track of any hot takes or
funny jokes on your server.

## Invite

There is no public invite link yet.

## Hosting

To host your own instance, check out [the docker image](https://hub.docker.com/r/apoy2k/greenbookbot). You will need to
also provide a postgres instance for the database.

- Create Application & Bot User on Discord Developer Portal
- Checkout the image
- Authorize bot to your dev server with
  ```
  https://discord.com/api/oauth2/authorize?client_id=<appid>&permissions=2147486784&scope=bot%20applications.commands
  ```
- Make sure postgres instance is running
- Provide env variables as defined in the `.env.template` file
- Start the image

## Development

- Set up local dev environment as required
- Create Application & Bot User on Discord Developer Portal
- Authorize bot to your dev server with
  ```
  https://discord.com/api/oauth2/authorize?client_id=<appid>&permissions=2147486784&scope=bot%20applications.commands
  ```
- Start supporting services via docker-compose
- Copy the .env.tamplate file and adjust accordingly
- Run/Debug the project
