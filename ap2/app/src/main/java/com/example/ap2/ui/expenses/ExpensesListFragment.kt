package com.example.ap2.ui.expenses

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ap2.R
import com.example.ap2.AddExpenseActivity
import com.example.ap2.data.TripRepository
import com.example.ap2.databinding.FragmentExpensesListBinding
import com.example.ap2.model.Expense

class ExpensesListFragment : Fragment() {

    private var _binding: FragmentExpensesListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpenseAdapter

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
        adapter = ExpenseAdapter(
            onEdit = { expense -> openExpenseEditor(expense) },
            onDelete = { expense -> confirmDelete(expense) }
        )
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

    private fun openExpenseEditor(expense: Expense) {
        val intent = Intent(requireContext(), AddExpenseActivity::class.java).apply {
            putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expense.id)
        }
        startActivity(intent)
    }

    private fun confirmDelete(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.expense_delete_title)
            .setMessage(R.string.expense_delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                TripRepository.removeExpense(expense.id)
                refreshList()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
