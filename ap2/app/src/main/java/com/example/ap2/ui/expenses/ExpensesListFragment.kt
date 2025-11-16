package com.example.ap2.ui.expenses

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ap2.AddExpenseActivity
import com.example.ap2.R
import com.example.ap2.data.TripRepository
import com.example.ap2.model.Expense

class ExpensesListFragment : Fragment() {

    private var adapter: ExpenseAdapter? = null
    private var expensesRecycler: RecyclerView? = null
    private var emptyState: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_expenses_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expensesRecycler = view.findViewById(R.id.expensesRecycler)
        emptyState = view.findViewById(R.id.emptyState)
        adapter = ExpenseAdapter(
            onEdit = { expense -> openExpenseEditor(expense) },
            onDelete = { expense -> confirmDelete(expense) }
        )
        expensesRecycler?.layoutManager = LinearLayoutManager(requireContext())
        expensesRecycler?.adapter = adapter
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val expenses = TripRepository.getExpenses()
        adapter?.displayCurrency = TripRepository.displayCurrency
        adapter?.submitList(expenses)
        emptyState?.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
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

}
