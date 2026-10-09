# Settings provider

Authority: `com.jeremykenedy.contourflow.settings`

The provider exposes schema and stored settings through `ContentProvider.call`. It performs no network operations. Schema version 1 is:

| Key | Type | Choices or range | Default |
|---|---|---|---|
| `palette` | choice | Abyss, Slate, Sandstone, Glacier | Abyss |
| `relief` | choice | Ridges, Basins, Coast | Ridges |
| `density` | integer | 1 through 5 | 3 |
| `speed` | integer | 1 through 5 | 3 |
| `weight` | integer | 1 through 5 | 2 |
| `lighting` | choice | Auto, Day, Night | Auto |
| `brightness` | integer | 1 through 5 | 3 |

Every field supports the value `random`. `get_settings` returns current values. `set_setting` takes the setting key as the method argument and the value in a string Bundle entry named `value`. `set_random_all` and `set_random_none` toggle randomization for all fields. Random values resolve when the scene begins. Unsupported keys and values fail with `IllegalArgumentException`.
