<a id="readme-top"></a>

<!-- PROJECT SHIELDS -->
<!--
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![License][license-shield]][license-url]

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/AfonsoBatista7/MineStats">
    <img src="./src/org/rage/pluginstats/img/favicon.ico" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">MineStats Plugin</h3>

  <p align="center">
    A Minecraft plugin for tracking player stats and rewarding medals based on in-game performance.
    <br />
    <a href="https://github.com/AfonsoBatista7/MineStats"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/AfonsoBatista7/MineStats/issues">Report Bug</a>
    ·
    <a href="https://github.com/AfonsoBatista7/MineStats/issues">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#compile">Compile</a></li>
        <li><a href="#configuration">Configuration</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

[![Java][java-shield]][java-url]
[![Spigot][spigot-shield]][spigot-url]
[![MongoDB][mongodb-shield]][mongodb-url]
[![Maven][maven-shield]][maven-url]

---

## 🧩 About The Project

MineStats is a custom Minecraft Java Edition plugin that tracks detailed player statistics and stores them in a MongoDB database. It features a medal system that awards in-game tags to players based on their performance, creating a competitive and rewarding experience.

Key Features:
- Tracks key player stats (e.g. blocks mined, time played, mobs killed).
- Stores data in MongoDB for persistence and extensibility.
- Medals and tags reward high-performing players.
- Designed to be extensible and server-admin friendly.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 🚀 Getting Started

Follow these steps to compile and run the MineStats plugin on your own Minecraft server.

### ✅ Prerequisites

- Java 8 or higher
- Maven (for building the plugin)
- A MongoDB instance (local or remote)
- A Minecraft Java server (e.g. Spigot or Paper)

### 💾 Compile

1. **Clone the repository**:
   ```bash
   git clone https://github.com/AfonsoBatista7/MineStats.git
   cd MineStats
   ```
2. **Build the plugin**
   - On Windows:
   Run `FinishBuild.bat` by double-clicking it.

   - On macOS/Linux:
   ```bash
   mvn clean package
   ```
3. Find the built .jar file in the target/ folder.

4. Move it to your server’s plugins folder.

5. Configure and restart the server.

### ⚙️ Configuration

After the plugin runs once, a config.yml file is generated. Update it to point to your MongoDB instance:

```yaml
  mongodb:
    host: localhost
    port: 27017
    database: minestats
    username: your_username
    password: your_password
```

## 🛠️ Usage

- The plugin automatically tracks player actions and stores data in MongoDB.

- Based on performance, players earn medals.

- Each medal gives the player a special in-game tag they can use or show off.

### 📋 Commands

| Command         | Aliases                                                  | Description                                              |
|-----------------|----------------------------------------------------------|----------------------------------------------------------|
| `/upload`       | –                                                        | Uploads all the player stats to the database.            |
| `/uploadall`    | `uploadAll`                                              | Uploads all the stats from all players.                  |
| `/download`     | –                                                        | Downloads all the stats for a player from the database.  |
| `/merge`        | –                                                        | Merges two player documents from the database.           |
| `/givemedal`    | `giveMedal`                                              | Gives a medal to a player.                               |
| `/updateall`    | `updateAll`                                              | Updates all documents in the database.                   |
| `/medal`        | `Medal`, `medalinfo`, `Medalinfo`, `medalInfo`, `MedalInfo` | Shows information about a specific medal.            |
| `/medals`       | `Medals`, `allmedals`, `allMedals`                       | Displays all available medals.                           |
| `/playermedals` | `playerMedals`, `playermedal`, `playerMedal`, `pmedals`, `pm` | Shows all the medals a specified player has.      |
| `/stats`        | `playerstats`, `playerStats`, `PlayerStats`, `Playerstats`, `pstats` | Displays current stats for the player.   |
| `/tag`          | `tg`                                                     | Shows a list of tag commands.                            |
| `/link`         | –                                                        | Links your Minecraft account with your Discord account.  |
| `/unlink`       | –                                                        | Unlinks your Minecraft account from your Discord account.|

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 🤝 Contributing

Any contributions are welcomed from the community!

1. Fork this repo

2. Create a branch: git checkout -b feature/my-feature

3. Make changes and commit: git commit -m 'Add some feature'

4. Push to your branch: git push origin feature/my-feature

5. Open a pull request

Please follow good coding practices and include a clear description of your changes.

## 📄 License

Distributed under the MIT License. See `LICENSE.txt` for more information.

## 📬 Contact

- Email: [afonsobatista13@gmail.com](mailto://afonsobatista13@gmail.com)

- GitHub: [https://github.com/AfonsoBatista7/MineStats](https://github.com/AfonsoBatista7/MineStats)

---

Made with ❤️ for Minecraft server admins and their communities.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
<!-- From own repo -->
[contributors-shield]: https://img.shields.io/github/contributors/AfonsoBatista7/MineStats.svg?style=for-the-badge
[contributors-url]: https://github.com/AfonsoBatista7/MineStats/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/AfonsoBatista7/MineStats.svg?style=for-the-badge
[forks-url]: https://github.com/AfonsoBatista7/MineStats/network/members
[issues-shield]: https://img.shields.io/github/issues/AfonsoBatista7/MineStats.svg?style=for-the-badge
[issues-url]: https://github.com/AfonsoBatista7/MineStats/issues
[license-shield]: https://img.shields.io/github/license/AfonsoBatista7/MineStats.svg?style=for-the-badge
[license-url]: https://github.com/AfonsoBatista7/MineStats/blob/master/LICENSE.txt
[stars-shield]: https://img.shields.io/github/stars/AfonsoBatista7/MineStats.svg?style=for-the-badge
[stars-url]: https://github.com/AfonsoBatista7/MineStats/stargazers
<!-- From repo images -->
[product-screenshot]: ./docs/images/product.png
<!-- From badges -->
[java-shield]: https://img.shields.io/badge/Java-FF0000?logoColor=white
[java-url]: https://openjdk.org/
[mongodb-shield]: https://img.shields.io/badge/MongoDB-%234ea94b.svg?logo=mongodb&logoColor=white
[mongodb-url]: https://mongodb.github.io/mongo-java-driver/
[maven-shield]: https://img.shields.io/badge/Maven-purple?logoColor=white
[maven-url]: https://maven.apache.org/
[spigot-shield]: https://img.shields.io/badge/Spigot-yellow?logoColor=black
[spigot-url]: https://www.spigotmc.org/
