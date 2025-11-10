package com.example.ap2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.ap2.data.TripRepository
import com.example.ap2.databinding.ActivityExpensesBinding
import com.example.ap2.ui.expenses.ExpensesListFragment

class ExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpensesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = TripRepository.tripName
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, ExpensesListFragment())
                .commit()
        }

        binding.addExpenseFab.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.viewSettlementButton.setOnClickListener {
            startActivity(Intent(this, SettlementActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.currencyInfo.text = getString(
            R.string.currency_display,
            TripRepository.displayCurrency.displayName
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.expenses_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_site -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ibmec.br"))
                startActivity(intent)
                true
            }
            R.id.action_send_feedback -> {
                val uri = Uri.parse("mailto:")
                val email = Intent(Intent.ACTION_SENDTO, uri).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("coordenacao@exemplo.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Feedback SplitEasy")
                    putExtra(Intent.EXTRA_TEXT, "Olá, segue meu feedback:")
                }
                startActivity(Intent.createChooser(email, getString(R.string.share_summary)))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
