package com.leo.overtime.statistics.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

@Deprecated("弃用", ReplaceWith("BaseQuickAdapter"))
abstract class BaseAdapter<T>(var data: List<T>) : RecyclerView.Adapter<BaseAdapter<T>.Helper>() {

    lateinit var context: Context
    private var listener: ((Int, T) -> Unit)? = null


    fun setOnItemClick(listener: ((position: Int, item: T) -> Unit)) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Helper {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(getLayout(), parent, false)
        view.setOnClickListener(this::onItemClicked)
        return Helper(view)
    }

    override fun onBindViewHolder(holder: Helper, position: Int) {
        holder.itemView.tag = position
        bindData(holder, position, data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }


    private fun onItemClicked(v: View) {
        if (listener != null) {
            val position = v.tag as Int
            listener!!.invoke(position, data[position])
        }
    }

    @LayoutRes
    abstract fun getLayout(): Int

    abstract fun bindData(helper: Helper, position: Int, item: T)

    inner class Helper(itemView: View) : RecyclerView.ViewHolder(itemView)

}