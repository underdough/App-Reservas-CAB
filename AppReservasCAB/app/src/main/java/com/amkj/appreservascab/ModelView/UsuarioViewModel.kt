package com.amkj.appreservascab.ModelView

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amkj.appreservascab.Modelos.ModeloUsuarios

class UsuarioViewModel: ViewModel() {

        private val _datalistUsuario = MutableLiveData<MutableList<ModeloUsuarios>>(mutableListOf())

        val dataListUsuario: MutableLiveData<MutableList<ModeloUsuarios>>
            get() = _datalistUsuario

        fun addUsuarioLista(mReservas: List<ModeloUsuarios>) {
            val currentList = _datalistUsuario.value ?: mutableListOf()
            currentList.addAll(mReservas)
            _datalistUsuario.postValue(currentList)
        }

        fun addUsuario(mReservas: ModeloUsuarios) {
            val currentList = _datalistUsuario.value ?: mutableListOf()
            currentList.add(mReservas)
            _datalistUsuario.postValue(currentList)
        }

        fun actualizarUsuario(position: Int, mReservas: ModeloUsuarios) {
            val currentList = _datalistUsuario.value ?: mutableListOf()
            if (position in currentList.indices) {
                currentList[position] = mReservas
                _datalistUsuario.postValue(currentList)
            }
        }

        fun eliminarUsuario(position: Int) {
            val currentList = _datalistUsuario.value ?: mutableListOf()
            if (position in currentList.indices) {
                currentList.removeAt(position)
                _datalistUsuario.postValue(currentList)
            }
        }



}


