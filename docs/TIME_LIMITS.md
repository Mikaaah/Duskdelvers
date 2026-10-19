# Expedition timers — 0.4.0

The five portal tiers retain their expedition **time allowances**. Biome difficulty now changes dynamically during exploration, independently of the gateway tier.

| Gateway | Time |
| --- | ---: |
| Tier I | 20 min |
| Tier II | 25 min |
| Tier III | 30 min |
| Tier IV | 35 min |
| Tier V | 40 min |

Each team uses **one server-side countdown** which pauses when nobody is online inside that run. The action bar shows the current **cave biome, biome danger, time remaining and team size**. Warnings occur at five minutes, one minute, and ten seconds.

On timeout the team is extracted to each person's saved origin and keeps collected inventory. On the **first natural cache discovery**, the remaining time is extended to **at least 3 minutes** (configurable). There is **no boss** to defeat.

Standalone/foreign-command entries still receive a 30-minute independent fallback timer. \`/duskdelvers enter\` is now a Tier I expedition alias and consumes a daily entry.

World server config file (usually \`<world>/serverconfig/duskdelvers-server.toml\`):

~~~toml
[timeLimits]
enabled = true
actionBarCountdown = true
outpostMinutes = 30
tier1Minutes = 20
tier2Minutes = 25
tier3Minutes = 30
tier4Minutes = 35
tier5Minutes = 40
extractionGraceMinutes = 3
~~~
