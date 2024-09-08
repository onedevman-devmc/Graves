# Graves

A simple plugin to have graves when players dies, preventing them from loosing their stuff but not as cheated as keeping their inventory :\).

### Download

You can download the plugin from [releases](https://github.com/onedevman-devmc/BarrelShop/releases)

### Configuration

```yaml
graves:
    keep-bedrock: true # Prevent graves from breaking bedrock blocks when placed. If enabled, the plugin will try to find the nearest vertical position to place the grave.
```

### Commands

| Command                  | Description                                      | Permission                              |
| ------------------------ | ------------------------------------------------ | --------------------------------------- |
| `/graves reload-config`  | Reload plugin configuration.                     | mc.graves.commands.graves.reload-config |
| `/bury [target: Player]` | Bury a player (or itself if no target specified) | mc.graves.commands.graves.bury*         |

### Permissions

| Permission  | Description                   |
| ----------- | ----------------------------- |
| mc.graves.* | Gives all plugin permissions. |

<details>
    <summary><strong><i>mc.graves.*</i></strong></summary>

| Permission           | Description                             |
| -------------------- | --------------------------------------- |
| mc.graves.commands.* | Gives all commands related permissions. |
| mc.graves.graves.*   | Gives all graves related permissions.   |

</details>

<details>
    <summary><strong><i>mc.graves.commands.*</i></strong></summary>

| Permission                  | Description                                   |
| --------------------------- | --------------------------------------------- |
| mc.graves.commands.graves.* | Gives all graves command related permissions. |

</details>

<details>
    <summary><strong><i>mc.graves.commands.graves.*</i></strong></summary>

| Permission                              | Description                                 |
| --------------------------------------- | ------------------------------------------- |
| mc.graves.commands.graves.reload-config | Allows to reload plugin configuration.      |
| mc.graves.commands.graves.bury.*        | Gives all bury command related permissions. |

</details>

<details open>
    <summary><strong><i>mc.graves.commands.graves.bury.*</i></strong></summary>

| Permission                            | Description                                    |
| ------------------------------------- | ---------------------------------------------- |
| mc.graves.commands.graves.bury.self   | Allows to use this command to bury itself.     |
| mc.graves.commands.graves.bury.others | Allows to use this command bury other players. |

</details>

<details open>
    <summary><strong><i>mc.graves.bury.*</i></i></strong></summary>

| Permission          | Description                       |
| ------------------- | --------------------------------- |
| mc.graves.bury.self | Allows to bury itself when dying. |

</details>
