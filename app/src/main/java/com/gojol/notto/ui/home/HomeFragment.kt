package com.gojol.notto.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.gojol.notto.R
import com.gojol.notto.databinding.FragmentHomeBinding
import com.gojol.notto.model.database.label.Label
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private val homeViewModel: HomeViewModel by viewModels()
    private val calendarAdapter = CalendarAdapter("2021년 11월 2일")
    private val customCalendarAdapter = CustomCalendarAdapter()
    private val labelAdapter = LabelAdapter()
    private val labelWrapperAdapter = LabelWrapperAdapter(labelAdapter)
    private val todoAdapter = TodoAdapter()

    private val concatAdapter: ConcatAdapter by lazy {
        val config = ConcatAdapter.Config.Builder().apply {
            setIsolateViewTypes(false)
        }.build()
        ConcatAdapter(config, customCalendarAdapter, labelWrapperAdapter, todoAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                    CustomCalendarAdapter.VIEW_TYPE -> 1
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

        val date =
            listOf("일", "월", "화", "수", "목", "금", "토") + (1..31).toList().map { it.toString() }
        val today = Calendar.getInstance().get(Calendar.DATE).toString()
        val checkDate = date.map { DateWithFocus(it, Random().nextInt(5), it == today) }
        customCalendarAdapter.submitList(checkDate)

        homeViewModel.todoList.observe(viewLifecycleOwner, {
            todoAdapter.submitList(it)
        })

        CoroutineScope(Dispatchers.IO).launch {
            context?.let {
                val labelList = Dummy(it).getLabel()
                    .map { label -> LabelWithCheck(label, false) }
                    .toMutableList()
                labelList.add(0, LabelWithCheck(Label(0, "전체"), true))
                labelAdapter.submitList(labelList)
            }
        }
    }
}
