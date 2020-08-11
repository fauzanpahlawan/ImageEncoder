package com.example.imageencoder.fragment

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imageencoder.R
import com.example.imageencoder.database.ImageDatabase
import com.example.imageencoder.entity.Image
import com.example.imageencoder.recyclerview.ImageAdapter
import kotlinx.android.synthetic.main.fragment_view_all_image.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ViewAllImageFragment : BaseFragment() {

    companion object {
        const val SELECT_IMAGE_REQUEST_CODE = 181
    }

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var database: ImageDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_all_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab_add_image.setOnClickListener { intentSelectImage() }
        database = context?.let { ImageDatabase.getImageDatabase(it) }!!

        launch {
            val images = database.imageDao().getImages()
            if (images.isNotEmpty()) {
                tv_such_empty.visibility = View.VISIBLE
                tv_such_empty.text = resources.getString(R.string.loading_images)
            } else {
                tv_such_empty.visibility = View.VISIBLE
                tv_such_empty.text = resources.getString(R.string.no_images)
            }
            imageAdapter =
                ImageAdapter(images) { imgData ->
                    onItemClicked(imgData)
                }.apply { notifyDataSetChanged() }
            tv_such_empty.visibility = View.GONE

            rv_images.apply {
                layoutManager = LinearLayoutManager(this.context)
                rv_images.adapter = imageAdapter
            }
        }
    }

    private fun onItemClicked(image: Image) {
        val action = ViewAllImageFragmentDirections.actionViewAllImageFragmentToViewImageFragment()
        action.imageData = image.imageBase64
        view?.findNavController()?.navigate(action)
    }

    private fun intentSelectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
                tv_such_empty.visibility = View.VISIBLE
                tv_such_empty.text = resources.getString(R.string.loading_images)
                val clipData = data?.clipData
                val dataUri = data?.data
                if (clipData != null) {
                    val imagesInputStream = getImagesInputStream(clipData)
                    launch {
                        var encodedImageList: List<Image>
                        withContext(Dispatchers.Default) {
                            encodedImageList = imageToBase64(imagesInputStream)
                        }
                        database.imageDao().insertAllImages(encodedImageList)
                        imageAdapter.setNewData(database.imageDao().getImages())
                        imageAdapter.notifyDataSetChanged()
                        tv_such_empty.visibility = View.GONE
                    }
                } else {
                    val imageInputStream = getImageInputStream(dataUri)
                    launch {
                        var encodedImage: Image
                        withContext(Dispatchers.Default) {
                            encodedImage = imageToBase64(imageInputStream)
                        }
                        database.imageDao().insertImage(encodedImage)
                        imageAdapter.setNewData(database.imageDao().getImages())
                        imageAdapter.notifyDataSetChanged()
                        tv_such_empty.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun getImagesInputStream(data: ClipData?): List<InputStream?> {
        val imagesInputStream = mutableListOf<InputStream?>()
        for (i in 0 until (data?.itemCount ?: 0)) {
            val imageStream =
                context?.contentResolver?.openInputStream(data?.getItemAt(i)?.uri as Uri)
            imagesInputStream.add(imageStream)
        }
        return imagesInputStream.toList()
    }

    private fun getImageInputStream(imageUri: Uri?): InputStream {
        return context?.contentResolver?.openInputStream(imageUri as Uri) as InputStream
    }

    private fun imageToBase64(imagesInputStream: List<InputStream?>): List<Image> {
        val encodedImageList = mutableListOf<Image>()
        for (i in imagesInputStream.indices) {
            val selectedImage = BitmapFactory.decodeStream(imagesInputStream[i])
            val encodedImage = encodeToBase64(selectedImage)
            encodedImageList.add(Image(encodedImage))
        }
        return encodedImageList
    }

    private fun imageToBase64(imageInputStream: InputStream): Image {
        val selectedImage = BitmapFactory.decodeStream(imageInputStream)
        val encodedImage = encodeToBase64(selectedImage)
        return Image(encodedImage)
    }

    private fun encodeToBase64(bm: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream)
        val b = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete_all_data) {
            launch {
                database.imageDao().deleteAll()
                imageAdapter.setNewData(database.imageDao().getImages())
                imageAdapter.notifyDataSetChanged()
                tv_such_empty.visibility = View.VISIBLE
                tv_such_empty.text = resources.getString(R.string.no_images)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}