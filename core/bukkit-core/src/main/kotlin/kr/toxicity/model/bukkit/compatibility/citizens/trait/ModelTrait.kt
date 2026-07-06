/*
 * This source file is part of BetterModel.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package kr.toxicity.model.bukkit.compatibility.citizens.trait

import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.data.renderer.ModelRenderer
import kr.toxicity.model.bukkit.util.wrap
import net.citizensnpcs.api.trait.Trait
import net.citizensnpcs.api.trait.TraitName
import net.citizensnpcs.api.util.DataKey

@TraitName("model")
class ModelTrait : Trait("model") {
    private var modelName: String? = null
    private var _renderer: ModelRenderer? = null
    var renderer
        get() = _renderer
        set(value) {
            npc?.entity?.let {
                value?.create(it.wrap()) ?: BetterModel.registryOrNull(it.uniqueId)?.close()
            }
            _renderer = value
            modelName = value?.name()
        }

    override fun load(key: DataKey) {
        modelName = key.getString("")?.takeIf { it.isNotEmpty() }
        modelName?.let {
            BetterModel.modelOrNull(it)?.let { model ->
                renderer = model
            }
        }
    }

    override fun save(key: DataKey) {
        key.setString("", modelName)
    }

    override fun onSpawn() {
        npc?.entity?.let {
            if (BetterModel.registryOrNull(it.uniqueId) == null) {
                renderer?.create(it.wrap())
            }
        }
    }

    /**
     * Reapplies the assigned model to this NPC's entity, re-resolving it by name if it
     * failed to resolve earlier (e.g. BetterModel's assets were not loaded yet when
     * Citizens deserialized this trait).
     *
     * @return true if a model is assigned and was (re)applied to the entity
     */
    fun reapply(): Boolean {
        val name = modelName ?: return false
        val model = _renderer ?: BetterModel.modelOrNull(name)?.also { _renderer = it } ?: return false
        npc?.entity?.let { model.create(it.wrap()) }
        return true
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
}
