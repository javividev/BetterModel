/**
 * This source file is part of BetterModel.
 * Copyright (c) 2024–2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */
package kr.toxicity.model.bukkit.compatibility.citizens

import kr.toxicity.model.api.bukkit.BetterModelBukkit
import kr.toxicity.model.api.bukkit.event.BetterModelBukkitEvent
import kr.toxicity.model.api.event.PluginEndReloadEvent
import kr.toxicity.model.bukkit.compatibility.Compatibility
import kr.toxicity.model.bukkit.compatibility.citizens.command.AnimateCommand
import kr.toxicity.model.bukkit.compatibility.citizens.command.LimbCommand
import kr.toxicity.model.bukkit.compatibility.citizens.command.ModelCommand
import kr.toxicity.model.bukkit.compatibility.citizens.trait.ModelTrait
import kr.toxicity.model.bukkit.util.PLUGIN
import kr.toxicity.model.bukkit.util.registerListener
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.trait.TraitInfo
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CitizensCompatibility : Compatibility {
    override fun start() {
        CitizensAPI.getTraitFactory()
            .registerTrait(TraitInfo.create(ModelTrait::class.java))
        CitizensAPI.getCommandManager().run {
            register(ModelCommand::class.java)
            register(AnimateCommand::class.java)
            register(LimbCommand::class.java)
        }
        registerListener(object : Listener {
            @EventHandler
            fun onBetterModelEvent(event: BetterModelBukkitEvent) {
                event.`as`(PluginEndReloadEvent::class.java) ?: return
                val refresh = Runnable {
                    runCatching {
                        for (npc in CitizensAPI.getNPCRegistry()) {
                            if (!npc.hasTrait(ModelTrait::class.java)) continue
                            npc.getTrait(ModelTrait::class.java).reapplyAfterAssetsLoad()
                        }
                    }
                }
                when {
                    BetterModelBukkit.IS_FOLIA ->
                        Bukkit.getGlobalRegionScheduler().run(PLUGIN) { refresh.run() }
                    Bukkit.isPrimaryThread() -> refresh.run()
                    else -> Bukkit.getScheduler().runTask(PLUGIN, refresh)
                }
            }
        })
    }
}
