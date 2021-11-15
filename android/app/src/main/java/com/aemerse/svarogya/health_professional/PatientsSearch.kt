package com.aemerse.svarogya.health_professional

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.aemerse.svarogya.R
import com.aemerse.svarogya.databinding.PatientsSearchActivityBinding
import com.aemerse.svarogya.models.OngoingTreatments
import com.aemerse.svarogya.utils.sharedPrefFile
import java.util.*
import kotlin.collections.ArrayList

class PatientsSearch : AppCompatActivity(), UserCardAdapter.OnClickListener {

    private val list = ArrayList<OngoingTreatments>()
    private val docIds = ArrayList<String>()
    private lateinit var binding: PatientsSearchActivityBinding

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PatientsSearchActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomnavview.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        binding.bottomnavview.itemIconTintList = null

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val usersAdapter = UserCardAdapter(this, list, this)
        binding.recyclerView.adapter = usersAdapter

        FirebaseFirestore.getInstance().collection("OngoingTreatments")
            .whereEqualTo("hospitalName",getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)
                .getString(getString(R.string.professionalHospitalName),null))
            .get().addOnSuccessListener {
                list.clear()
                it.forEach { doc->
                    if(doc.get("name")!=null) {
                        list.add(0, doc.toObject(OngoingTreatments::class.java))
                        docIds.add(0, doc.id)
                    }
                }
                usersAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                binding.progressBar.isIndeterminate = false

                binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }
                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText!!.isEmpty()) {
                            list.clear()
                            docIds.clear()
                            it.forEach { doc->
                                if(doc.get("name")!=null) {
                                    list.add(0, doc.toObject(OngoingTreatments::class.java))
                                    docIds.add(0, doc.id)
                                }
                            }
                            usersAdapter.notifyDataSetChanged()
                        }
                        else {
                            list.clear()
                            docIds.clear()
                            it.forEach { doc->
                                val docHere = doc.toObject(OngoingTreatments::class.java)
                                if(docHere.name!=null && (docHere.name!!.contains(newText.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(
                                            Locale.ROOT
                                        ) else it.toString()
                                    }) ||
                                            docHere.patientId!!.contains(newText))) {
                                    list.add(docHere)
                                    docIds.add(0, doc.id)
                                }
                            }
                            usersAdapter.notifyDataSetChanged()
                        }
                        return true
                    }
                })
            }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
    }

    override fun onUserClicked(position: Int) {
        val intent = Intent(this, ProfessionalViewActivity::class.java)
        intent.putExtra("docId", docIds[position])
        startActivity(intent)
    }

    private var mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem: MenuItem ->
        when (menuItem.itemId) {
            R.id.bottom_home -> {
                val intent = Intent(this, HomeHealthProfActivity::class.java)
                startActivity(intent)
            }
        }
        false
    }
}