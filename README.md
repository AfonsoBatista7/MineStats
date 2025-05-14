<a id="readme-top"></a>

<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![License][license-shield]][license-url]

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/AfonsoBatista7/MineStats">
    <img src="./source/favicon.ico" alt="Logo" width="80" height="80">
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

## 🛠️ Built With

- [Java](https://openjdk.org/)
- [Spigot API](https://www.spigotmc.org/)
- [MongoDB Java Driver](https://mongodb.github.io/mongo-java-driver/)
- [Maven](https://maven.apache.org/)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## 🚀 Getting Started

Follow these steps to compile and run the MineStats plugin on your own Minecraft server.

### ✅ Prerequisites

- Java 8 or higher
- Maven (for building the plugin)
- A MongoDB instance (local or remote)
- A Minecraft Java server (e.g. Spigot or Paper)

### 💾 Compile The Plugin

1. **Clone the repository**:
   ```bash
   git clone https://github.com/AfonsoBatista7/MineStats.git
   cd MineStats
   ```
2. **Build the plugin**
   - On Windows:
   Run FinishBuild.bat by double-clicking it.

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

Commands and more advanced configuration options are coming soon.

## 🤝 Contributing

Any contributions are welcomed from the community!

1. Fork this repo

2. Create a branch: git checkout -b feature/my-feature

3. Make changes and commit: git commit -m 'Add some feature'

4. Push to your branch: git push origin feature/my-feature

5. Open a pull request

Please follow good coding practices and include a clear description of your changes.

## 📄 License

Distributed under the MIT License. See LICENSE for more information.

## 📬 Contact

- Email: afonsobatista13@gmail.com

- GitHub: MineStats Project

Made with ❤️ for Minecraft server admins and their communities.