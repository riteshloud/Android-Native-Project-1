package com.demo.utilities

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class PaginationScrollListener(layoutManager: LinearLayoutManager?, limit:Int) : RecyclerView.OnScrollListener() {
    var layoutManager: LinearLayoutManager? = null
    private var PAGE_SIZE = 20

    init {
        this.layoutManager = layoutManager
        this.PAGE_SIZE = limit
    }

    /**
     * Supporting only LinearLayoutManager for now.
     *
     * @param layoutManager
     */
    fun PaginationScrollListener(layoutManager: LinearLayoutManager?, limit:Int) {
        this.layoutManager = layoutManager
        this.PAGE_SIZE = limit
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount = layoutManager!!.childCount
        val totalItemCount = layoutManager!!.itemCount
        val firstVisibleItemPosition = layoutManager!!.findFirstVisibleItemPosition()

//        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
//            loadMoreItems()
//        }

        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                loadMoreItems()
            }
        }
    }

    /*fun onScrolling(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView!!, dx, dy)
        val visibleItemCount = layoutManager!!.childCount
        val totalItemCount = layoutManager!!.itemCount
        val firstVisibleItemPosition = layoutManager!!.findFirstVisibleItemPosition()
        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE
            ) {
                loadMoreItems()
            }
        }
    }*/

    protected abstract fun loadMoreItems()
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean
}