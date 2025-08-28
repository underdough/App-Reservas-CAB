package com.amkj.appreservascab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DisponibilidadActivity : AppCompatActivity(R.layout.activity_disponibilidad) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val tipo = intent.getStringExtra("tipo") ?: "ambiente"
            val id   = intent.getIntExtra("recursoId", 0)
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragContainer,
                    CalendarioDisponibilidadFragment.newInstance(tipo, id, /*returnResult=*/ true)
                )
                .commit()
        }
    }
}
