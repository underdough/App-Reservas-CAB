package com.amkj.appreservascab

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.amkj.appreservascab.Adaptadores.AdapterQuejas
import com.amkj.appreservascab.Modelos.BackendMsg
import com.amkj.appreservascab.Modelos.ModeloQueja
import com.amkj.appreservascab.Modelos.QuejasQuery
import com.amkj.appreservascab.Modelos.RegistrarQuejaRequest
import com.amkj.appreservascab.databinding.ActivityMisQuejasBinding
import com.amkj.appreservascab.servicios.RetrofitClient
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MisQuejasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisQuejasBinding
    private lateinit var adapter: AdapterQuejas

    private val TAG = "MIS_QUEJAS"

    private var usuarioId = -1
    private var rolUsuario = "aprendiz"

    private var imagenSeleccionada: Uri? = null

    // Selector de imagen (SAF)
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenSeleccionada = uri
        if (uri != null) {
            Glide.with(this).load(uri).into(binding.ivAdjunto)
        } else {
            binding.ivAdjunto.setImageResource(R.drawable.placeholder_ambiente)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisQuejasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preferencias de usuario
        val sp = getSharedPreferences("UsuariosPrefs", MODE_PRIVATE)
        usuarioId = sp.getInt("id", -1)
        rolUsuario = (sp.getString("rol", "aprendiz") ?: "aprendiz").lowercase().trim()

        if (usuarioId == -1) {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (rolUsuario.isBlank()) rolUsuario = "aprendiz"

        // Recycler
        adapter = AdapterQuejas(emptyList<ModeloQueja>())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Listeners UI
        binding.btnAdjuntar.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnNuevaQueja.setOnClickListener { nuevaQueja() }
        binding.btnExportarPdf.setOnClickListener { exportarPdf() }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText.orEmpty()); return true
            }
        })

        // Cargar
        cargarQuejas()
    }

    // ==================== CARGA ====================

    private fun queryBody() = QuejasQuery(usuario_id = usuarioId, rol = rolUsuario)

    private fun cargarQuejas() {
        RetrofitClient.instance.obtenerQuejas(queryBody())
            .enqueue(object : Callback<List<ModeloQueja>> {
                override fun onResponse(
                    call: Call<List<ModeloQueja>>,
                    response: Response<List<ModeloQueja>>
                ) {
                    val lista: List<ModeloQueja> = response.body() ?: emptyList()
                    adapter.submitList(lista)
                }

                override fun onFailure(call: Call<List<ModeloQueja>>, t: Throwable) {
                    Log.e(TAG, "cargarQuejas() fail: ${t.message}", t)
                    Toast.makeText(this@MisQuejasActivity, "Fallo al cargar quejas", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ==================== NUEVA QUEJA ====================

    private fun nuevaQueja() {
        val asunto = binding.etAsunto.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        if (asunto.isBlank() || descripcion.isBlank()) {
            Toast.makeText(this, "Asunto y descripción son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Si hay imagen, primero súbela; luego registra queja con la URL resultante
        val uri = imagenSeleccionada
        if (uri != null) {
            subirImagen(uri) { imagenUrl ->
                registrarQuejaCon(imagenUrl, asunto, descripcion)
            }
        } else {
            registrarQuejaCon(null, asunto, descripcion)
        }
    }

    private fun registrarQuejaCon(imagenUrl: String?, asunto: String, descripcion: String) {
        val body = RegistrarQuejaRequest(
            usuario_id = usuarioId,
            rol = rolUsuario,
            asunto = asunto,
            descripcion = descripcion,
            imagen_url = imagenUrl
        )

        RetrofitClient.instance.registrarQueja(body)
            .enqueue(object : retrofit2.Callback<BackendMsg> {
                override fun onResponse(
                    call: retrofit2.Call<BackendMsg>,
                    response: retrofit2.Response<BackendMsg>
                ) {
                    val code = response.code()
                    val payload = response.body()
                    val serverMsg = payload?.mensaje.orEmpty()
                    val ok = response.isSuccessful && (payload?.id_queja != null ||
                            serverMsg.contains("registrada", ignoreCase = true))

                    if (ok) {
                        Toast.makeText(this@MisQuejasActivity,
                            serverMsg.ifBlank { "Queja registrada" }, Toast.LENGTH_SHORT).show()
                        binding.etAsunto.text?.clear()
                        binding.etDescripcion.text?.clear()
                        binding.ivAdjunto.setImageResource(R.drawable.placeholder_ambiente)
                        imagenSeleccionada = null
                        cargarQuejas()
                    } else {
                        val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                        Log.e(TAG, "registrarQueja() HTTP $code, err=$err, body=$payload")
                        Toast.makeText(this@MisQuejasActivity,
                            serverMsg.ifBlank { "Error al registrar (HTTP $code)" },
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<BackendMsg>, t: Throwable) {
                    Log.e(TAG, "registrarQueja() fail: ${t.message}", t)
                    Toast.makeText(this@MisQuejasActivity,
                        "Fallo de red al registrar", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun subirImagen(uri: Uri, onDone: (String?) -> Unit) {
        try {
            val input = contentResolver.openInputStream(uri) ?: run {
                Toast.makeText(this, "No se pudo abrir imagen", Toast.LENGTH_SHORT).show()
                onDone(null); return
            }
            val temp = File(cacheDir, "queja_${System.currentTimeMillis()}.bin")
            temp.outputStream().use { out -> input.use { it.copyTo(out) } }

            val mimeType = contentResolver.getType(uri) ?: "image/*"
            val reqBody = temp.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("imagen", "adjunto.jpg", reqBody)

            RetrofitClient.instance.subirImagenQueja(part)
                .enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(
                        call: Call<Map<String, String>>,
                        response: Response<Map<String, String>>
                    ) {
                        val url = response.body()?.get("imagen_url")
                        if (response.isSuccessful && !url.isNullOrBlank()) {
                            onDone(url)
                        } else {
                            Toast.makeText(this@MisQuejasActivity, "No se pudo subir imagen", Toast.LENGTH_SHORT).show()
                            onDone(null)
                        }
                        temp.delete()
                    }

                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        Log.e(TAG, "subirImagen fail: ${t.message}", t)
                        Toast.makeText(this@MisQuejasActivity, "Fallo al subir imagen", Toast.LENGTH_SHORT).show()
                        temp.delete()
                        onDone(null)
                    }
                })

        } catch (e: Exception) {
            Log.e(TAG, "subirImagen error: ${e.message}", e)
            Toast.makeText(this, "Error preparando imagen", Toast.LENGTH_SHORT).show()
            onDone(null)
        }
    }

    // ==================== EXPORTAR PDF ====================

    private fun exportarPdf() {
        RetrofitClient.instance.exportarQuejasPdf(queryBody())
            .enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(
                    call: retrofit2.Call<okhttp3.ResponseBody>,
                    response: retrofit2.Response<okhttp3.ResponseBody>
                ) {
                    val body = response.body()
                    if (!response.isSuccessful || body == null) {
                        val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                        Log.e(TAG, "exportarPdf() HTTP=${response.code()} errorBody=$err")
                        Toast.makeText(this@MisQuejasActivity, "No se pudo generar PDF", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Leer todos los bytes para validar y evitar streams a medio escribir
                    val bytes = try { body.bytes() } catch (e: Exception) {
                        Log.e(TAG, "exportarPdf() bytes() error: ${e.message}", e)
                        byteArrayOf()
                    }

                    if (bytes.isEmpty()) {
                        Toast.makeText(this@MisQuejasActivity, "PDF vacío", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Validar firma PDF %PDF-
                    val isPdf = bytes.size >= 5 &&
                            bytes[0] == 0x25.toByte() && // %
                            bytes[1] == 0x50.toByte() && // P
                            bytes[2] == 0x44.toByte() && // D
                            bytes[3] == 0x46.toByte() && // F
                            bytes[4] == 0x2D.toByte()    // -

                    if (!isPdf) {
                        val head = String(bytes.copyOfRange(0, minOf(bytes.size, 120)), Charsets.UTF_8)
                        Log.e(TAG, "exportarPdf(): respuesta no es PDF. Inicio: $head")
                        Toast.makeText(this@MisQuejasActivity, "El servidor no devolvió un PDF", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val fileName = "quejas_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}.pdf"
                    val uri = guardarEnDescargas(bytes, fileName)
                    if (uri != null) {
                        Toast.makeText(this@MisQuejasActivity, "PDF guardado en Descargas", Toast.LENGTH_SHORT).show()
                        abrirPdf(uri) // usa la versión directa
                    } else {
                        Toast.makeText(this@MisQuejasActivity, "Error al guardar PDF", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                    Log.e(TAG, "exportarPdf() fail: ${t.message}", t)
                    Toast.makeText(this@MisQuejasActivity, "Fallo al exportar PDF", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun guardarEnDescargas(bytes: ByteArray, fileName: String): android.net.Uri? {
        return try {
            val resolver = contentResolver
            val isQPlus = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
            val mime = "application/pdf"

            if (isQPlus) {
                val cv = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, mime)
                    put(android.provider.MediaStore.Downloads.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                    put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
                    put(android.provider.MediaStore.Downloads.SIZE, bytes.size.toLong())
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    // Marcar como finalizado
                    cv.clear()
                    cv.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, cv, null, null)
                }
                uri
            } else {
                // Fallback para < Android 10 (Q). MediaStore Files.
                val cv = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), cv)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { it.write(bytes) }
                }
                uri
            }
        } catch (e: Exception) {
            Log.e(TAG, "guardarEnDescargas error: ${e.message}", e)
            null
        }
    }



    private fun abrirPdf(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Abrir PDF con…"))
        } catch (_: Exception) {
            Toast.makeText(this, "No hay visor de PDF instalado", Toast.LENGTH_SHORT).show()
        }
    }
}
