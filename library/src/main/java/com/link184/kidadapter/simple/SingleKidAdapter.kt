package com.link184.kidadapter.simple

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.link184.kidadapter.base.BaseAdapter
import com.link184.kidadapter.base.BaseViewHolder
import com.link184.kidadapter.base.KidDiffUtilCallback

open class SingleKidAdapter<T> (private val configuration: SingleKidAdapterConfiguration<T>)
    : BaseAdapter<T, BaseViewHolder<T>>(configuration.items) {
    init {
        configuration.validate()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(configuration.layoutResId, parent, false)
        val viewHolder = object : BaseViewHolder<T>(view) {
            override fun bindView(item: T) {
                configuration.bindHolderIndexed(itemView, item, adapterPosition)
                configuration.bindHolder(itemView, item)
            }
        }
        val itemView = viewHolder.itemView
        itemView.setOnClickListener {
            val adapterPosition = viewHolder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onItemClick(itemView, adapterPosition)
            }
        }
        return viewHolder
    }

    open fun onItemClick(itemView: View, position: Int) {}

    override operator fun plusAssign(itemList: MutableList<T>) {
        this.itemList.reset(itemList)
        notifyDataSetChanged()
    }

    override operator fun plus(itemList: List<T>) {
        this.itemList.addAll(itemList).also(::dispatchUpdates)
    }

    override operator fun plus(item: T) {
        itemList.add(item).also(::dispatchUpdates)
    }

    override fun add(index: Int, item: T) {
        itemList.add(index, item).also(::dispatchUpdates)
    }

    override fun addAll(items: MutableList<T>) {
        itemList.addAll(items).also(::dispatchUpdates)
    }

    override fun addAll(index: Int, items: MutableList<T>) {
        itemList.addAll(index, items).also(::dispatchUpdates)
    }

    override operator fun set(index: Int, item: T) {
        itemList.set(index, item).also(::dispatchUpdates)
    }

    override fun insert(index: Int, itemList: List<T>) {
        this.itemList.addAll(index, itemList).also(::dispatchUpdates)
    }

    override operator fun minus(index: Int) {
        itemList.removeAt(index).also(::dispatchUpdates)
    }

    override operator fun minus(item: T) {
        itemList.remove(item).also(::dispatchUpdates)
    }

    override fun clear() {
        itemList.clear().also(::dispatchUpdates)
    }

    private fun dispatchUpdates(diffUtilCallback: KidDiffUtilCallback<T>) {
        with(diffUtilCallback) {
            contentComparator = configuration.contentComparator
            itemsComparator = configuration.itemsComparator
            DiffUtil.calculateDiff(this).dispatchUpdatesTo(this@SingleKidAdapter)
        }
    }
}