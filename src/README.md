### Installation

Paste this inside of your console (you need to have [GIT](https://git-scm.com/) and
[Maven](https://maven.apache.org/) installed).

```bash
git clone https://github.com/s2004Dev/Java-FluxFur-API.git
cd ./JFA
mvn clean install
```

After this you should be able to open the project inside your chosen IDE or add it as a dependency in any Maven
project with the following markup:

```xml
<dependency>
  <groupId> lonter </groupId>
  <artifactId> jfa </artifactId>
  <version> 0.1 </version>
</dependency>
```

### JFA - Java-FluxFur-API

All concepts are the same as JDA's, so if you're familiar with them, you won't have any issues. Otherwise, I
recommend just looking at their repo for more detailed information.

In case you want to use a different instance of Fluxer that isn't FluxFur you can use the following code:

```java
// You can put your instance's endpoint here.
final var url = "https://examplefluxerhost.com";

// You can put your bot's token here.
final var token = "token";

// With JFABuilder
final var jfa = JFABuilder.createDefault(token)
  .setRestConfig(new RestConfig().setBaseUrl(url))
  .build();

// With DefaultShardManagerBuilder
val shard = DefaultShardManagerBuilder.createDefault(token)
  .setRestConfig(new RestConfig().setBaseUrl(url))
  .build();
```

### Credits

This project is a fork of [PhantomStray's JFA](https://github.com/Fluxer-JFA/JFA) which is a fork of
[JDA](https://github.com/discord-jda/JDA).
The main difference between their work and mine is that all "JDA" mentions are removed so that they can both work
together at the same time without conflicts.