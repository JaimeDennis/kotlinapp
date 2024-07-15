package com.example.mykotlinapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mykotlinapp.database.Alumno
import com.example.mykotlinapp.database.AlumnosDbHelper
import com.example.mykotlinapp.database.dbAlumnos
import com.google.android.material.imageview.ShapeableImageView
import android.Manifest
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

private const val ARG_MATRICULA = "matricula"
private const val ARG_NOMBRE = "nombre"
private const val ARG_DOMICILIO = "domicilio"
private const val ARG_FOTO = "foto"
private const val ARG_ESPECIALIDAD = "especialidad"

class DbFragment : Fragment() {
    private var matricula: String? = null
    private var nombre: String? = null
    private var domicilio: String? = null
    private var foto: String? = null
    private var especialidad: String? = null

    private val REQUEST_CODE_PICK_IMAGE = 100
    private val REQUEST_CODE_STORAGE_PERMISSION = 200

    var db: dbAlumnos? = null

    private lateinit var btnGuardar: Button
    private lateinit var btnBuscar: Button
    private lateinit var btnBorrar: Button

    private lateinit var txtMatricula: EditText
    private lateinit var txtNombre: EditText
    private lateinit var txtDomicilio: EditText
    private lateinit var txtEspecialidad: EditText
    private lateinit var imgProfilePic: ShapeableImageView
    private lateinit var lblUrlFoto: TextView

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            matricula = it.getString(ARG_MATRICULA)
            nombre = it.getString(ARG_NOMBRE)
            domicilio = it.getString(ARG_DOMICILIO)
            foto = it.getString(ARG_FOTO)
            especialidad = it.getString(ARG_ESPECIALIDAD)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_db, container, false)

        // Inicializar vistas
        this.btnGuardar = view.findViewById(R.id.btnGuardar)
        this.btnBuscar = view.findViewById(R.id.btnBuscar)
        this.btnBorrar = view.findViewById(R.id.btnBorrar)

        txtMatricula = view.findViewById(R.id.txtMatricula)
        txtNombre = view.findViewById(R.id.txtNombre)
        txtDomicilio = view.findViewById(R.id.txtDomicilio)
        txtEspecialidad = view.findViewById(R.id.txtEspecialidad)
        imgProfilePic = view.findViewById(R.id.imgProfilePic)
        lblUrlFoto = view.findViewById(R.id.lblUrlFoto)

        // Preparar la vista con datos si se recibieron argumentos
        matricula?.let {
            txtMatricula.setText(it)
            txtMatricula.isEnabled = false  // Deshabilitar edición de matrícula
        }
        nombre?.let { txtNombre.setText(it) }
        domicilio?.let { txtDomicilio.setText(it) }
        especialidad?.let { txtEspecialidad.setText(it) }
        foto?.let { cargarImagen(it) }
        foto?.let { lblUrlFoto.text = it }

        // Configurar el botón Guardar
        btnGuardar.setOnClickListener {
            guardarAlumno()
        }

        // Configurar el botón Buscar
        btnBuscar.setOnClickListener {
            buscarAlumno()
        }

        // Configurar el botón Borrar
        btnBorrar.setOnClickListener {
            confirmarEliminarAlumno()
        }

        // Configurar clic en la imagen de perfil
        imgProfilePic.setOnClickListener {
            solicitarPermisoGaleria()
        }

        // Configurar texto de matrícula para habilitar/deshabilitar el botón Borrar
        txtMatricula.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setEnableDeleteButton(!s.isNullOrEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        lblUrlFoto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val url = s?.toString() ?: ""
                if (url.isNotEmpty()) {
                    cargarImagen(url)
                }
            }
        })

        // Inicialmente deshabilitar el botón Borrar si la matrícula está vacía
        setEnableDeleteButton(!txtMatricula.text.isNullOrEmpty())

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE) {
            data?.data?.let { uri ->
                imageUri = uri
                imgProfilePic.setImageURI(uri) // Mostrar la imagen seleccionada inmediatamente
                lblUrlFoto.text = uri.toString() // Actualizar la etiqueta de URL de la foto
            }
        }
    }

    private fun buscarAlumno() {
        val matricula = txtMatricula.text?.toString() ?: ""
        if (matricula.isEmpty()) {
            mostrarMensaje("Faltó ingresar matrícula")
        } else {
            val dbHelper = AlumnosDbHelper(requireContext())
            val alumno = dbHelper.getAlumnoByMatricula(matricula)
            if (alumno.id != 0) {
                txtNombre.setText(alumno.nombre)
                txtDomicilio.setText(alumno.domicilio)
                txtEspecialidad.setText(alumno.especialidad)

                // Mostrar la imagen actual del alumno
                if (alumno.foto != "Pendiente") {
                    alumno.foto.let { cargarImagen(it) }
                    lblUrlFoto.text = alumno.foto
                } else {
                    imgProfilePic.setImageResource(R.mipmap.foto)
                    lblUrlFoto.text = ""
                }

                // Actualizar imageUri para la imagen actual del alumno
                if (alumno.foto != "Pendiente") {
                    imageUri = Uri.parse(alumno.foto)
                } else {
                    imageUri = null
                }
            } else {
                mostrarMensaje("No se encontró el alumno")
            }
        }
    }

    private fun guardarAlumno() {
        val nombre = txtNombre.text?.toString() ?: ""
        val matricula = txtMatricula.text?.toString() ?: ""
        val domicilio = txtDomicilio.text?.toString() ?: ""
        val especialidad = txtEspecialidad.text?.toString() ?: ""

        if (nombre.isEmpty() || matricula.isEmpty() || domicilio.isEmpty() || especialidad.isEmpty()) {
            mostrarMensaje("Faltó información")
        } else {
            val dbHelper = AlumnosDbHelper(requireContext())

            // Obtener la URL de la foto a guardar
            val fotoUrl = if (imageUri != null) {
                imageUri.toString()  // Usar la nueva imagen seleccionada
            } else {
                lblUrlFoto.text?.toString() ?: foto
                ?: "Pendiente" // Usar la URL existente si no se seleccionó nueva imagen
            }

            val alumno = Alumno<Any>().apply {
                this.nombre = nombre
                this.matricula = matricula
                this.domicilio = domicilio
                this.especialidad = especialidad
                this.foto = fotoUrl
            }

            val existingAlumno = dbHelper.getAlumnoByMatricula(matricula)
            if (existingAlumno.id != 0) {
                if (existingAlumno.nombre != nombre ||
                    existingAlumno.domicilio != domicilio ||
                    existingAlumno.especialidad != especialidad ||
                    existingAlumno.foto != alumno.foto
                ) {
                    val rowsAffected = dbHelper.actualizarAlumno(alumno, existingAlumno.id)
                    if (rowsAffected > 0) {
                        mostrarMensaje("Alumno editado exitosamente")
                        val matricula = arguments?.getString(ARG_MATRICULA)
                        if (!matricula.isNullOrEmpty()) {
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                    } else {
                        mostrarMensaje("Error al editar el alumno")
                    }
                } else {
                    mostrarMensaje("No hay cambios en los datos del alumno")
                }
            } else {
                val id: Long = dbHelper.insertarAlumno(Alumno())
                mostrarMensaje("Se agregó el alumno con el id: $id")
            }
        }
    }


    private fun confirmarEliminarAlumno() {
        val matricula = txtMatricula.text?.toString() ?: ""
        if (matricula.isEmpty()) {
            mostrarMensaje("Faltó ingresar matrícula")
        } else {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmación de eliminación")
            builder.setMessage("¿Estás seguro de que deseas eliminar este alumno?")

            // Configurar botón Aceptar con color personalizado
            builder.setPositiveButton("Aceptar") { dialog, which ->
                eliminarAlumno()
                dialog.dismiss()
            }
            // Configurar botón Cancelar con color personalizado
            builder.setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

            val dialog = builder.create()

            dialog.setOnShowListener {
                val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                negativeButton.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.miColor
                    )
                )
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.ripple
                    )
                )
            }
            dialog.show()
        }
    }

    private fun eliminarAlumno() {
        val matricula = txtMatricula.text?.toString() ?: ""
        val dbHelper = AlumnosDbHelper(requireContext())
        val alumno = dbHelper.getAlumnoByMatricula(matricula)
        if (alumno.id != 0) {
            val result = dbHelper.borrarAlumno(alumno.id)
            val matricula = arguments?.getString(ARG_MATRICULA)
            if (!matricula.isNullOrEmpty()) {
                requireActivity().supportFragmentManager.popBackStack()
            }
            if (result > 0) {
                mostrarMensaje("Alumno eliminado exitosamente")
                limpiarCampos()
            } else {
                mostrarMensaje("Error al eliminar el alumno")
            }
        } else {
            mostrarMensaje("No se encontró el alumno")
        }
    }

    private fun cargarImagen(urlImagen: String) {
        Glide.with(requireContext())
            .load(Uri.parse(urlImagen))
            .placeholder(R.mipmap.foto)
            .error(R.mipmap.foto)
            .into(imgProfilePic)
    }

    private fun solicitarPermisoGaleria() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si el permiso no está concedido, solicitarlo al usuario
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        } else {
            // Si el permiso ya está concedido, abrir la galería
            abrirGaleria()
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, abrir la galería
                abrirGaleria()
            } else {
                // Permiso denegado, mostrar un mensaje
                mostrarMensaje("Permiso denegado para acceder a la galería")
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun limpiarCampos() {
        txtMatricula.text.clear()
        txtNombre.text.clear()
        txtDomicilio.text.clear()
        txtEspecialidad.text.clear()
        imgProfilePic.setImageResource(R.mipmap.foto)
        lblUrlFoto.text = ""
    }

    private fun setEnableDeleteButton(enable: Boolean) {
        btnBorrar.isEnabled = enable
        btnBorrar.isClickable = enable
        btnBorrar.alpha = if (enable) 1f else 0.5f
    }

    companion object {
        @JvmStatic
        fun newInstance(
            matricula: String? = null,
            nombre: String? = null,
            domicilio: String? = null,
            foto: String? = null,
            especialidad: String? = null
        ) = DbFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_MATRICULA, matricula)
                putString(ARG_NOMBRE, nombre)
                putString(ARG_DOMICILIO, domicilio)
                putString(ARG_FOTO, foto)
                putString(ARG_ESPECIALIDAD, especialidad)
            }
        }
    }
}
