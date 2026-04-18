/**
 * This source file is part of BetterModel.
 * Copyright (c) 2024–2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */
package kr.toxicity.model.bukkit.compatibility.citizens.trait

import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.bukkit.scheduler.BukkitModelScheduler
import kr.toxicity.model.api.data.renderer.ModelRenderer
import kr.toxicity.model.bukkit.util.wrap
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitName
import net.citizensnpcs.api.util.DataKey

@TraitName("model")
class ModelTrait : Trait("model") {
    private var _renderer: ModelRenderer? = null

    /**
     * Model id persisted for Citizens saves. The in-memory registry is cleared on NPC despawn
     * (e.g. shutdown), so [save] must not rely on [BetterModel.registryOrNull] alone.
     */
    private var storedModelName: String? = null

    var renderer
        get() = _renderer
        set(value) {
            npc?.entity?.let {
                value?.create(it.wrap()) ?: BetterModel.registryOrNull(it.uniqueId)?.close()
            }
            _renderer = value
            storedModelName = value?.name()
        }

    override fun load(key: DataKey) {
        val raw = key.getString("")
        if (raw.isNullOrEmpty()) {
            storedModelName = null
            renderer = null
            return
        }
        storedModelName = raw
        val resolved = BetterModel.modelOrNull(raw)
        if (resolved != null) {
            renderer = resolved
        } else {
            npc?.entity?.uniqueId?.let {
                BetterModel.registryOrNull(it)?.close()
            }
            _renderer = null
        }
    }

    override fun save(key: DataKey) {
        val fromRegistry = npc?.entity?.uniqueId?.let { uuid ->
            BetterModel.registryOrNull(uuid)?.first()?.renderer()?.name()
        }
        key.setString("", storedModelName ?: fromRegistry)
    }

    /** Re-applies the model when assets become available later (Nexo merge, delayed reload, etc.). */
    fun reapplyAfterAssetsLoad() {
        tryApplyModelToNpcEntity()
        scheduleApplyRetries()
    }

    override fun onSpawn() {
        tryApplyModelToNpcEntity()
        scheduleApplyRetries()
    }

    override fun onCopy() {
        onSpawn()
    }

    override fun onDespawn() {
        npc?.entity?.uniqueId?.let {
            BetterModel.registryOrNull(it)?.close()
        }
    }

    override fun onRemove() {
        npc?.entity?.uniqueId?.let {
            BetterModel.registryOrNull(it)?.close()
        }
    }

    private fun tryApplyModelToNpcEntity(): Boolean {
        val entity = npc?.entity ?: return false
        if (BetterModel.registryOrNull(entity.uniqueId) != null) return true
        val name = storedModelName ?: _renderer?.name() ?: return false
        val model = BetterModel.modelOrNull(name) ?: return false
        model.create(entity.wrap())
        if (_renderer == null) {
            _renderer = model
        }
        return true
    }

    private fun scheduleApplyRetries() {
        npc ?: return
        npc?.entity ?: return
        val pendingName = storedModelName ?: _renderer?.name() ?: return
        val entity = npc?.entity ?: return
        val loc = entity.location
        val sched = BetterModel.platform().scheduler() as BukkitModelScheduler
        for (delay in RETRY_DELAYS_TICKS) {
            sched.taskLater(loc, delay) {
                val n = npc ?: return@taskLater
                if (!n.isSpawned) return@taskLater
                val ent = n.entity ?: return@taskLater
                if (BetterModel.registryOrNull(ent.uniqueId) != null) return@taskLater
                if (BetterModel.modelOrNull(pendingName) == null) return@taskLater
                tryApplyModelToNpcEntity()
            }
        }
    }

    private companion object {
        private val RETRY_DELAYS_TICKS = longArrayOf(1L, 5L, 20L, 60L, 100L, 200L, 400L)
    }
}
