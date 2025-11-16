package com.example.ap2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.example.ap2.data.TripRepository
import com.example.ap2.ui.expenses.ExpensesListFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ExpensesActivity : AppCompatActivity() {

    private val toolbar: MaterialToolbar
        get() = findViewById(R.id.toolbar)
    private val currencyInfo: TextView
        get() = findViewById(R.id.currencyInfo)
    private val viewSettlementButton: MaterialButton
        get() = findViewById(R.id.viewSettlementButton)
    private val addExpenseFab: ExtendedFloatingActionButton
        get() = findViewById(R.id.addExpenseFab)
    private val viewOverviewButton: MaterialButton
        get() = findViewById(R.id.viewOverviewButton)
    private val fragmentContainer: FragmentContainerView
        get() = findViewById(R.id.fragmentContainer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        toolbar.title = TripRepository.tripName
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(fragmentContainer.id, ExpensesListFragment())
                .commit()
        }

        addExpenseFab.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        viewSettlementButton.setOnClickListener {
            startActivity(Intent(this, SettlementActivity::class.java))
        }

        viewOverviewButton.setOnClickListener {
            startActivity(Intent(this, OverviewActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        currencyInfo.text = getString(
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
