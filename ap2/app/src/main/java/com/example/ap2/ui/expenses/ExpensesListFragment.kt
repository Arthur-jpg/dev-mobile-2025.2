package com.example.ap2.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.data.TripRepository
import com.example.ap2.databinding.FragmentExpensesListBinding

class ExpensesListFragment : Fragment() {

    private var _binding: FragmentExpensesListBinding? = null
    private val binding get() = _binding!!
    private val adapter = ExpenseAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.expensesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.expensesRecycler.adapter = adapter
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val expenses = TripRepository.getExpenses()
        adapter.displayCurrency = TripRepository.displayCurrency
        adapter.submitList(expenses)
        binding.emptyState.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
