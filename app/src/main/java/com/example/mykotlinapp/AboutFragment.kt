package com.example.mykotlinapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykotlinapp.database.Alumno
import com.example.mykotlinapp.database.AlumnosDbHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AboutFragment : Fragment() {

    private lateinit var rcvLista: RecyclerView
    private lateinit var adaptadorLista: AdaptadorLista
    private lateinit var agregarAlumno: FloatingActionButton

    private lateinit var searchView: SearchView
    private lateinit var toolbar: Toolbar

    private var listaAlumnosCompleta: List<Alumno<Any?>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Enable options menu for this fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        // Initialize views
        agregarAlumno = view.findViewById(R.id.agregarAlumno)
        rcvLista = view.findViewById(R.id.recId)
        toolbar = view.findViewById(R.id.toolbar)

        // Set toolbar in the hosting activity
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)

        // Set up RecyclerView
        rcvLista.layoutManager = LinearLayoutManager(requireContext())

        // Load all students from database
        val dbHelper = AlumnosDbHelper(requireContext())
        listaAlumnosCompleta = dbHelper.obtenerTodosLosAlumnos()

        // Set up adapter with complete list
        adaptadorLista = AdaptadorLista(listaAlumnosCompleta, requireContext()) { alumno ->
            // Handle student click: Navigate to DbFragment with student data
            val dbFragment = DbFragment.newInstance(alumno.matricula, alumno.nombre, alumno.domicilio, alumno.foto, alumno.especialidad)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frmContenedor, dbFragment)
                .addToBackStack(null)
                .commit()
        }
        rcvLista.adapter = adaptadorLista

        // Configure click on add student button
        agregarAlumno.setOnClickListener {
            val dbFragment = DbFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frmContenedor, dbFragment)
                .commit()
        }

        return view
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
                if (newText != null) {
                    filterList(newText)
                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater) // Call super after inflating
    }

    private fun filterList(query: String) {
        // Filter the complete list based on matricula, nombre, or especialidad
        val filteredList = listaAlumnosCompleta.filter { alumno ->
            alumno.matricula.contains(query, ignoreCase = true) ||
                    alumno.nombre.contains(query, ignoreCase = true) ||
                    alumno.especialidad.contains(query, ignoreCase = true)
        }

        // Update adapter with filtered list
        adaptadorLista.actualizarLista(filteredList)
    }
}