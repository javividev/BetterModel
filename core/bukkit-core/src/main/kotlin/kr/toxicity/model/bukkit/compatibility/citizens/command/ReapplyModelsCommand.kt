/*
 * This source file is part of BetterModel.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package kr.toxicity.model.bukkit.compatibility.citizens.command

import kr.toxicity.model.bukkit.compatibility.citizens.trait.ModelTrait
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.command.Command
import net.citizensnpcs.api.command.CommandContext
import net.citizensnpcs.api.command.Requirements
import net.citizensnpcs.api.npc.NPC
import org.bukkit.command.CommandSender

@Requirements
class ReapplyModelsCommand {
    @Command(
        aliases = ["npc"],
        usage = "reapplymodels",
        desc = "Reapplies BetterModel's saved model to every NPC that has one assigned.",
        modifiers = ["reapplymodels"],
        min = 1,
        max = 1,
        permission = "citizens.npc.reapplymodels"
    )
    @Suppress("UNUSED")
    fun reapplyModels(args: CommandContext, sender: CommandSender, npc: NPC?) {
        var success = 0
        var failed = 0
        CitizensAPI.getNPCRegistries().forEach { registry ->
            registry.forEach { target ->
                target.getTraitNullable(ModelTrait::class.java)?.let {
                    if (it.reapply()) success++ else failed++
                }
            }
        }
        sender.sendMessage(
            "Reapplied model to $success NPC(s)." +
                if (failed > 0) " $failed model(s) could not be resolved (not loaded?)." else ""
        )
    }
}
