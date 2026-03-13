package com.bedrockexecutor.data

import com.bedrockexecutor.data.model.Script
import com.bedrockexecutor.data.model.ScriptCategory

object BuiltInScripts {

    val all: List<Script> = listOf(

        Script(
            id = "builtin_fly",
            name = "Fly Mode",
            description = "Gives all players the ability to fly in survival mode",
            category = ScriptCategory.MOVEMENT,
            code = """
import { world, system, GameMode } from "@minecraft/server";

// Toggle fly for all players
system.runInterval(() => {
    for (const player of world.getAllPlayers()) {
        player.onScreenDisplay.setActionBar("§a[Executor] §fFly: ON");
        player.getComponent("minecraft:flying")?.setCanFly(true);
        player.teleport(
            { x: player.location.x, y: player.location.y, z: player.location.z },
            { dimension: player.dimension }
        );
    }
}, 20);
""".trimIndent()
        ),

        Script(
            id = "builtin_speed",
            name = "Speed Boost",
            description = "Applies permanent speed effect to all players",
            category = ScriptCategory.MOVEMENT,
            code = """
import { world, system, EffectTypes } from "@minecraft/server";

system.runInterval(() => {
    for (const player of world.getAllPlayers()) {
        player.addEffect("speed", 100, { amplifier: 3, showParticles: false });
    }
}, 80);
""".trimIndent()
        ),

        Script(
            id = "builtin_kill_aura",
            name = "Kill Aura",
            description = "Damages all nearby entities around each player",
            category = ScriptCategory.COMBAT,
            code = """
import { world, system } from "@minecraft/server";

const AURA_RANGE = 5;
const DAMAGE = 4;

system.runInterval(() => {
    for (const player of world.getAllPlayers()) {
        const nearby = player.dimension.getEntities({
            location: player.location,
            maxDistance: AURA_RANGE,
            excludeTypes: ["minecraft:player"]
        });
        for (const entity of nearby) {
            entity.applyDamage(DAMAGE, { cause: "entityAttack", damagingEntity: player });
        }
    }
}, 10);
""".trimIndent()
        ),

        Script(
            id = "builtin_xray",
            name = "X-Ray Vision",
            description = "Sends players coordinates of nearby ores via action bar",
            category = ScriptCategory.VISUAL,
            code = """
import { world, system, BlockTypes } from "@minecraft/server";

const SCAN_RADIUS = 8;
const ORE_TYPES = [
    "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
    "minecraft:emerald_ore", "minecraft:gold_ore",
    "minecraft:ancient_debris"
];

system.runInterval(() => {
    for (const player of world.getAllPlayers()) {
        const loc = player.location;
        const found = [];
        for (let x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (let y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (let z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    const block = player.dimension.getBlock({
                        x: Math.floor(loc.x) + x,
                        y: Math.floor(loc.y) + y,
                        z: Math.floor(loc.z) + z
                    });
                    if (block && ORE_TYPES.includes(block.typeId)) {
                        found.push(`§e${block.typeId.replace("minecraft:", "")} §7(${block.location.x},${block.location.y},${block.location.z})`);
                    }
                }
            }
        }
        if (found.length > 0) {
            player.onScreenDisplay.setActionBar("§b[X-Ray] §f" + found.slice(0, 3).join(" | "));
        } else {
            player.onScreenDisplay.setActionBar("§b[X-Ray] §7No ores nearby");
        }
    }
}, 40);
""".trimIndent()
        ),

        Script(
            id = "builtin_god_mode",
            name = "God Mode",
            description = "Makes all players invincible (regenerates health constantly)",
            category = ScriptCategory.COMBAT,
            code = """
import { world, system } from "@minecraft/server";

system.runInterval(() => {
    for (const player of world.getAllPlayers()) {
        const health = player.getComponent("minecraft:health");
        if (health) {
            health.setCurrentValue(health.effectiveMax);
        }
        player.addEffect("resistance", 100, { amplifier: 255, showParticles: false });
        player.addEffect("regeneration", 100, { amplifier: 255, showParticles: false });
        player.addEffect("saturation", 100, { amplifier: 10, showParticles: false });
    }
}, 5);
""".trimIndent()
        ),

        Script(
            id = "builtin_infinite_items",
            name = "Infinite Items",
            description = "Refills held item stack when it gets low",
            category = ScriptCategory.UTILITY,
            code = """
import { world, system, ItemStack } from "@minecraft/server";

system.runInterval(() => {
    for (const player of world.getAllPlayers()) {
        const inventory = player.getComponent("minecraft:inventory")?.container;
        if (!inventory) continue;
        const selected = player.selectedSlotIndex;
        const item = inventory.getItem(selected);
        if (item && item.amount < 5) {
            const refill = new ItemStack(item.typeId, 64);
            inventory.setItem(selected, refill);
        }
    }
}, 10);
""".trimIndent()
        ),

        Script(
            id = "builtin_chat_prefix",
            name = "Chat Prefix",
            description = "Adds a colored [OP] tag before all player messages",
            category = ScriptCategory.UTILITY,
            code = """
import { world } from "@minecraft/server";

world.beforeEvents.chatSend.subscribe((event) => {
    event.cancel = true;
    const msg = event.message;
    const sender = event.sender;
    world.sendMessage(`§c[OP] §f${sender.name}§7: §f${msg}`);
});
""".trimIndent()
        ),

        Script(
            id = "builtin_anti_kb",
            name = "Anti Knockback",
            description = "Negates knockback on all players",
            category = ScriptCategory.COMBAT,
            code = """
import { world, system } from "@minecraft/server";

system.runInterval(() => {
    for (const player of world.getAllPlayers()) {
        player.addEffect("resistance", 60, { amplifier: 4, showParticles: false });
        // Apply slow falling to reduce knockback arc
        player.addEffect("slow_falling", 60, { amplifier: 0, showParticles: false });
    }
}, 50);
""".trimIndent()
        ),

        Script(
            id = "builtin_server_info",
            name = "Server Info Broadcast",
            description = "Broadcasts server stats to all players every minute",
            category = ScriptCategory.SERVER,
            code = """
import { world, system } from "@minecraft/server";

let tick = 0;
system.runInterval(() => {
    tick++;
    if (tick % 1200 === 0) { // Every minute
        const players = world.getAllPlayers();
        world.sendMessage(`§b[Server] §fPlayers online: §e${players.length}`);
        world.sendMessage(`§b[Server] §fTick: §e${tick} §7| Uptime: §e${Math.floor(tick / 20)}s`);
    }
}, 1);
""".trimIndent()
        )
    )
}
