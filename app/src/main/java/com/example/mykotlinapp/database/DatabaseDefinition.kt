package com.example.mykotlinapp.database

import android.provider.BaseColumns
import com.example.mykotlinapp.database.Alumno;


class DatabaseDefinition {

    object Alumnos:BaseColumns{
        const val tabla = "alumnos"
        const val id = "id"
        const val matricula = "matricula"
        const val nombre = "nombre"
        const val domicilio = "domicilio"
        const val especialidad = "especialidad"
        const val foto = "foto"
    }
}