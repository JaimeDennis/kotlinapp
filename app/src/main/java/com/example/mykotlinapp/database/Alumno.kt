package com.example.mykotlinapp.database

import com.example.mykotlinapp.R
import java.io.Serializable

data class Alumno<T>(
    var id: Int = 0,
    var matricula: String = "",
    var nombre: String = "",
    var domicilio: String = "",
    var especialidad: String = "",
    var foto: String = ""
) : Serializable
