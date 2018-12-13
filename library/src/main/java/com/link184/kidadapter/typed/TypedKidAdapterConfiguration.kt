package com.link184.kidadapter.typed

import androidx.recyclerview.widget.RecyclerView
import com.link184.kidadapter.exceptions.UndeclaredTag
import com.link184.kidadapter.exceptions.UndefinedLayout
import com.link184.kidadapter.exceptions.ZeroViewTypes
import com.link184.kidadapter.simple.SingleKidAdapterConfiguration

class TypedKidAdapterConfiguration {
    internal val viewTypes = mutableListOf<AdapterViewType<Any>>()
    internal var layoutManager: RecyclerView.LayoutManager? = null
    private val tags = mutableMapOf<String, Int>()

    fun withViewType(tag: String? = null, block: AdapterViewTypeConfiguration.() -> Unit) {
        val fromPosition =
            viewTypes.fold(0) { acc, adapterViewType -> acc + adapterViewType.configuration.getInternalItems().size }
        viewTypes.add(AdapterViewType(fromPosition, block))
        tag?.let { tags.put(it, viewTypes.lastIndex) }
    }

    fun withLayoutManager(block: () -> RecyclerView.LayoutManager?) {
        layoutManager = block()
    }

    /**
     * Useful to build [TypedKidAdapter] from [SingleKidAdapterConfiguration]
     */
    fun fromSimpleConfiguration(block: SingleKidAdapterConfiguration<*>.() -> Unit): TypedKidAdapterConfiguration {
        return TypedKidAdapterConfiguration().apply {
            val adapterConfiguration = SingleKidAdapterConfiguration<Any>().apply(block)
            withLayoutManager { adapterConfiguration.layoutManager }
            withViewType {
                withItems(adapterConfiguration.items.newList)
                bind<Any> {
                    adapterConfiguration.bindHolder(this, it)
                }
            }
        }
    }

    internal fun getAllItems(): MutableList<Any> {
        val alignedItems = viewTypes
            .map { it.configuration.getInternalItems().toMutableList() }
        if (alignedItems.isNotEmpty()) {
            return alignedItems
                .reduce { acc, items -> acc.apply { addAll(items) } }
        }
        throw ZeroViewTypes()
    }

    /**
     * Get FIRST mutable list where item type is [T]
     * @param T model type from adapter
     * @return mutable list of [T] items
     */
    internal inline fun <reified T> getItemsByType(): MutableList<T> {
        return viewTypes
            .map { it.configuration.getInternalItems().newList }
            .first {
                it.any { it is T }
            } as MutableList<T>
    }

    /**
     * Get [AdapterViewType] by given tag.
     */
    internal fun getViewTypeByTag(tag: String): AdapterViewType<Any> {
        tags[tag]?.let { return viewTypes[it] }
        throw UndeclaredTag(tag)
    }

    internal fun invalidateItems() {
        val newViewTypes = mutableListOf<AdapterViewType<Any>>()
        viewTypes.forEach { adapterViewType ->
            val fromPosition =
                newViewTypes.fold(0) { acc, adapterViewType -> acc + adapterViewType.configuration.getInternalItems().size }
            newViewTypes.add(adapterViewType)
            val toPosition = fromPosition + adapterViewType.configuration.getInternalItems().size
            adapterViewType.positionRange = fromPosition until if (toPosition < 1) 1 else toPosition
        }
    }

    internal fun validate() {
        when {
            viewTypes.firstOrNull { it.configuration.layoutResId == -1 } != null -> throw UndefinedLayout(
                "Adapter layout is not set, please declare it for each AdapterViewType with withLayoutResId() function"
            )
        }
    }
}