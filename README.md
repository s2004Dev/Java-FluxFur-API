### Installation

If you want to use JFA inside your Maven project as a dependency, you need to paste the following markup inside your
`pom.xml`:

```xml
<dependency>
  <groupId> it.lonter </groupId>
  <artifactId> jfa </artifactId>
  <version> 0.1.0 </version>
</dependency>
```

If you want to compile it from the source you can by pasting these commands inside your terminal emulator (you'll
need to have [GIT](https://git-scm.com/) and [Maven](https://maven.apache.org/) installed).

```bash
git clone https://github.com/s2004Dev/Java-FluxFur-API.git
cd ./JFA
mvn clean install
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
final var shard = DefaultShardManagerBuilder.createDefault(token)
  .setRestConfig(new RestConfig().setBaseUrl(url))
  .build();
```

### Credits

This project is a fork of [PhantomStray's JFA](https://github.com/Fluxer-JFA/JFA) which is a fork of
[JDA](https://github.com/discord-jda/JDA).
The main difference between their work and mine is that all "JDA" mentions are removed so that they can both work
together at the same time without conflicts.