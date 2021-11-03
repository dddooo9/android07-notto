package com.gojol.notto.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.gojol.notto.R
import com.gojol.notto.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    private val calendarAdapter = CalendarAdapter("2021년 11월 2일")
    private val labelAdapter = LabelAdapter { setAllChipCheckedEvent() }
    private val labelWrapperAdapter = LabelWrapperAdapter(labelAdapter)
    private val todoAdapter = TodoAdapter()

    private val concatAdapter: ConcatAdapter by lazy {
        val config = ConcatAdapter.Config.Builder().apply {
            setIsolateViewTypes(false)
        }.build()
        ConcatAdapter(config, calendarAdapter, labelWrapperAdapter, todoAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = homeViewModel

        val layoutManager = GridLayoutManager(context, 7)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (concatAdapter.getItemViewType(position)) {
                    CalendarAdapter.VIEW_TYPE -> 7
                    LabelAdapter.VIEW_TYPE -> 1
                    TodoAdapter.VIEW_TYPE -> 7
                    else -> 7
                }
            }
        }
        binding.rvHome.layoutManager = layoutManager
        binding.rvHome.adapter = concatAdapter

        homeViewModel.date.observe(viewLifecycleOwner, {
            calendarAdapter.setDate(it)
        })

//        homeViewModel.labelList.observe(viewLifecycleOwner, {
//            labelAdapter.submitList(it)
//        })

        homeViewModel.todoList.observe(viewLifecycleOwner, {
            todoAdapter.submitList(it)
        })

        CoroutineScope(Dispatchers.IO).launch {
            context?.let {
                Dummy(it).run {
                    getLabelWithTodo().map { label ->
                        if (label.labelId == 1) {
                            LabelWithCheck(label, true)
                        } else {
                            LabelWithCheck(label, false)
                        }
                    }.also { labelList -> labelAdapter.submitList(labelList) }
                }
            }
        }
    }

    private fun setAllChipCheckedEvent() {
        labelAdapter.allChipChecked()
    }
}
