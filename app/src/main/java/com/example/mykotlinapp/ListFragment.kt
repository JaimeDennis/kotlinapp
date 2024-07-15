package com.example.mykotlinapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class ListFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var arrayList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView

    private var filteredList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // handle arguments if needed
        }
        setHasOptionsMenu(true) // Enable options menu for this fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        // Initialize views
        listView = view.findViewById(R.id.lstAlumnos)
        toolbar = view.findViewById(R.id.toolbar)

        // Set toolbar in the hosting activity
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        // Populate list items
        val items = resources.getStringArray(R.array.alumnos)
        arrayList = ArrayList(items.toList())
        filteredList.addAll(arrayList) // Initialize filtered list with all items
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, filteredList)
        listView.adapter = adapter

        // Handle item click
        listView.setOnItemClickListener { parent, view, position, id ->
            val alumno: String = filteredList[position]
            showAlertDialog("$position: $alumno")
        }

        return view
    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Lista de Alumnos")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, which ->
                // Handle OK button click if needed
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu) // Inflate your menu items
        // Find the search item in your menu
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater) // Call super after inflating
    }

    private fun filterList(query: String?) {
        filteredList.clear()
        if (query.isNullOrEmpty()) {
            filteredList.addAll(arrayList)
        } else {
            val queryWords = query.lowercase().split(" ")
            val newFilteredList = arrayList.filter { item ->
                val itemLower = item.lowercase()
                queryWords.all { word -> itemLower.contains(word) }
            }
            filteredList.addAll(newFilteredList)
        }
        adapter.notifyDataSetChanged()
        listView.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}