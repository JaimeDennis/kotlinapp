package com.example.mykotlinapp.database

import android.content.ContentValues
import android.content.Context

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class AlumnosDbHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "sistema.db"
        private const val DATABASE_VERSION = 3
        private const val TEXT_TYPE = " TEXT"
        private const val INTEGER_TYPE = " INTEGER"
        private const val COMA = ","
        private const val SQL_CREATE_ALUMNO =
            "CREATE TABLE " +
                    DatabaseDefinition.Alumnos.tabla +
                    "(${DatabaseDefinition.Alumnos.id}$INTEGER_TYPE PRIMARY KEY $COMA" +
                    "${DatabaseDefinition.Alumnos.matricula}$TEXT_TYPE$COMA" +
                    "${DatabaseDefinition.Alumnos.nombre}$TEXT_TYPE$COMA" +
                    "${DatabaseDefinition.Alumnos.domicilio}$TEXT_TYPE$COMA" +
                    "${DatabaseDefinition.Alumnos.especialidad}$TEXT_TYPE$COMA" +
                    "${DatabaseDefinition.Alumnos.foto}$TEXT_TYPE)"
        private const val SQL_DELETE_ALUMNO = "DROP TABLE IF EXISTS ${DatabaseDefinition.Alumnos.tabla}"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ALUMNO)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELETE_ALUMNO)
        onCreate(db)
    }

    fun insertarAlumno(alumno: Alumno<Any?>): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseDefinition.Alumnos.matricula, alumno.matricula)
            put(DatabaseDefinition.Alumnos.nombre, alumno.nombre)
            put(DatabaseDefinition.Alumnos.domicilio, alumno.domicilio)
            put(DatabaseDefinition.Alumnos.especialidad, alumno.especialidad)
            put(DatabaseDefinition.Alumnos.foto, alumno.foto) // Guardar la ruta de la imagen
        }
        val id = db.insert(DatabaseDefinition.Alumnos.tabla, null, contentValues)
        db.close()
        return id
    }

    fun actualizarAlumno(alumno: Alumno<Any>, id: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseDefinition.Alumnos.matricula, alumno.matricula)
            put(DatabaseDefinition.Alumnos.nombre, alumno.nombre)
            put(DatabaseDefinition.Alumnos.domicilio, alumno.domicilio)
            put(DatabaseDefinition.Alumnos.especialidad, alumno.especialidad)
            put(DatabaseDefinition.Alumnos.foto, alumno.foto) // Guardar la ruta de la imagen
        }
        val rowsAffected = db.update(DatabaseDefinition.Alumnos.tabla, contentValues, "${DatabaseDefinition.Alumnos.id} = ?", arrayOf(id.toString()))
        db.close()
        return rowsAffected
    }


    fun getAlumnoByMatricula(matricula: String): Alumno<Any> {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(DatabaseDefinition.Alumnos.tabla, null, "${DatabaseDefinition.Alumnos.matricula} = ?", arrayOf(matricula), null, null, null)
        val alumno = if (cursor.moveToFirst()) {
            Alumno<Any>(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.id)),
                matricula = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.matricula)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.nombre)),
                domicilio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.domicilio)),
                especialidad = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.especialidad)),
                foto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.foto))
            )
        } else {
            Alumno<Any>()
        }
        cursor.close()
        db.close()
        return alumno
    }

    fun obtenerTodosLosAlumnos(): List<Alumno<Any?>> {
        val alumnosList = ArrayList<Alumno<Any?>>()
        val db = this.readableDatabase
        val cursor: Cursor = db.query(DatabaseDefinition.Alumnos.tabla, null, null, null, null, null, null)

        while (cursor.moveToNext()) {
            val alumno = Alumno<Any?>(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.id)),
                matricula = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.matricula)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.nombre)),
                domicilio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.domicilio)),
                especialidad = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.especialidad)),
                foto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseDefinition.Alumnos.foto))
            )
            alumnosList.add(alumno)
        }
        cursor.close()
        db.close()

        return alumnosList.map { alumno ->
            // Mapear la URL de la foto correctamente
            if (alumno.foto.isNotEmpty() && alumno.foto != "0") {
                // Si la URL no está vacía y no es "0", usar la URL proporcionada
                alumno.copy(foto = alumno.foto)
            } else {
                // Si no hay URL válida, asignar la imagen predeterminada
                alumno.copy(foto = "0")
            }
        }
    }



    fun borrarAlumno(id: Int): Int {
        val db = this.writableDatabase
        val rowsAffected = db.delete(DatabaseDefinition.Alumnos.tabla, "${DatabaseDefinition.Alumnos.id} = ?", arrayOf(id.toString()))
        db.close()
        return rowsAffected
    }
}