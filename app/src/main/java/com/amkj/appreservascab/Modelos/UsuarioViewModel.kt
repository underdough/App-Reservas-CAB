package com.amkj.appreservascab.Modelos


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsuarioViewModel: ViewModel() {

        private val _datalistUsuario = MutableLiveData<MutableList<ModeloUsuarios>>(mutableListOf())

        val dataListUsuario: MutableLiveData<MutableList<ModeloUsuarios>>
            get() = _datalistUsuario

        fun addUsuarioLista(reservas: List<ModeloUsuarios>) {
            val currentList = _datalistUsuario.value ?: mutableListOf()
            currentList.addAll(reservas)
            _datalistUsuario.postValue(currentList)
        }

        fun addUsuario(reservas: ModeloUsuarios) {
            val currentList = _datalistUsuario.value ?: mutableListOf()
            currentList.add(reservas)
            _datalistUsuario.postValue(currentList)
        }

        fun actualizarUsuario(position: Int, mInstagram: ModeloUsuarios) {
            val currentList = _datalistUsuario.value ?: mutableListOf()
            if (position in currentList.indices) {
                currentList[position] = mInstagram
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