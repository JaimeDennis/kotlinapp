package com.example.mykotlinapp

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mykotlinapp.database.Alumno
import com.google.android.material.imageview.ShapeableImageView

class AdaptadorLista(
    private var listaAlumnos: List<Alumno<Any?>>, // Cambiado a var para poder actualizar la lista
    private val context: Context,
    private val itemClickListener: (Alumno<Any?>) -> Unit
) : RecyclerView.Adapter<AdaptadorLista.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Referenciar las vistas en el layout del elemento de la lista
        val txtMatricula: TextView = itemView.findViewById(R.id.txtMatricula)
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        val txtCarrera: TextView = itemView.findViewById(R.id.txtCarrera)
        val imgProfilePic: ShapeableImageView = itemView.findViewById(R.id.imgProfilePic)

        fun bind(alumno: Alumno<Any?>) {
            txtMatricula.text = alumno.matricula
            txtNombre.text = alumno.nombre
            txtCarrera.text = alumno.especialidad

            // Cargar la imagen usando Glide si la URI no es "Pendiente"
            if (alumno.foto != "Pendiente") {
                Glide.with(itemView.context)
                    .load(Uri.parse(alumno.foto))
                    .placeholder(R.mipmap.foto) // Imagen predeterminada mientras carga
                    .error(R.mipmap.foto) // Imagen predeterminada en caso de error
                    .into(imgProfilePic)
            } else {
                imgProfilePic.setImageResource(R.mipmap.foto)  // Imagen predeterminada
            }

            // Configurar clic en el elemento de la lista
            itemView.setOnClickListener {
                itemClickListener.invoke(alumno)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.alumnoitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alumno = listaAlumnos[position]
        holder.bind(alumno)
    }

    override fun getItemCount(): Int {
        return listaAlumnos.size
    }

    // Método para actualizar la lista de alumnos y notificar cambios
    fun actualizarLista(nuevaLista: List<Alumno<Any?>>) {
        listaAlumnos = nuevaLista
        notifyDataSetChanged()
    }

}
