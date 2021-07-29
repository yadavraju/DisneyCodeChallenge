package com.raju.disney.ui.adapter

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import javax.inject.Inject

class CommonAdapter @Inject constructor() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val dataBoundAdapters: MutableList<BaseDataBoundAdapter<*>> = mutableListOf()
    private val viewTypeMap: SparseArrayCompat<BaseDataBoundAdapter<*>> = SparseArrayCompat()
    
    fun setDataBoundAdapter(dataBoundAdapters: Collection<BaseDataBoundAdapter<*>>) {
        this.dataBoundAdapters.clear()
        viewTypeMap.clear()
        addAll(dataBoundAdapters)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return viewTypeMap.get(viewType)!!.createViewHolder(parent)
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        dataBoundAdapters[position].bindViewHolder(holder)
    }
    
    override fun getItemCount(): Int {
        return dataBoundAdapters.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return dataBoundAdapters[position].viewType.id
    }
    
    private fun addAll(dataBoundAdapters: Collection<BaseDataBoundAdapter<*>>?) {
        dataBoundAdapters?.map { baseViewModel ->
            this.dataBoundAdapters.add(baseViewModel)
            //If there are multiple items of the same type the index will just update
            viewTypeMap.put(baseViewModel.viewType.id, baseViewModel)
        }
    }
}
