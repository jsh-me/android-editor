package kr.co.jsh.feature.photoedit

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import com.byox.drawview.enums.DrawingCapture
import kotlinx.android.synthetic.main.activity_photo_edit.*
import kr.co.domain.globalconst.Consts.Companion.EXTRA_PHOTO_PATH
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityPhotoEditBinding
import kr.co.jsh.utils.setupPermissions
import org.koin.android.ext.android.get
import java.io.File


class PhotoActivity : AppCompatActivity() , PhotoContract.View{
    private lateinit var binding: ActivityPhotoEditBinding
    private lateinit var presenter : PhotoPresenter
    var texteColor : ObservableField<Array<Boolean>> = ObservableField(arrayOf(false,false,false))
    var drawCheck : ObservableField<Boolean> = ObservableField(false)
    var path = ""
    private var destinationPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo_edit)
        binding.photo = this@PhotoActivity
    }

    private fun initView() {
        val extraIntent = intent

        presenter = PhotoPresenter(this, get(), get())
        setupPermissions(this) {
            extraIntent?.let {
                path = extraIntent.getStringExtra(EXTRA_PHOTO_PATH)
                presenter.setImageView(this, "file://" + path)
                Glide.with(this).load(path).into(photoImageView)
            }
        }
        destinationPath =  Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Images" + File.separator
    }

    override fun displayPhotoView(file: File) {
        binding.drawPhotoview.post {
            binding.drawPhotoview.apply {
                setBackgroundResource(R.color.background_space)
                setBackgroundImage(file, BackgroundType.FILE, BackgroundScale.CENTER_INSIDE)
            }
        }
    }


    fun resetButton(v: View){
        binding.drawPhotoview.apply{
            restartDrawing()
        }
        texteColor.set(arrayOf(false,false,false))
        texteColor.set(arrayOf(true,false,false))
        initView()
    }

    //https://codechacha.com/ko/android-mediastore-insert-media-files/
    //Unknown URI: content://media/external_primary/images/media
    //오른쪽 위 아이콘
    fun savePhoto(v: View){
        val saveImage = binding.drawPhotoview.createCapture(DrawingCapture.BITMAP)
        saveImage?.let {
            presenter.uploadFrameFile(saveImage[0] as Bitmap, this) //마스크까지 그려진 그림
        }?:run{
            Toast.makeText(this, "마스크를 그려주세요", Toast.LENGTH_SHORT).show()
        }
        //presenter.saveImage(this, Uri.parse("file://" + path))
    }

    fun drawPhotoMask(){
        texteColor.set(arrayOf(false,false,false))
        texteColor.set(arrayOf(false,true,false))
        drawCheck.set(true)
        presenter.uploadFile("file://" + path) //원본 그림

    }


    override fun uploadSuccess(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }

    override fun uploadFailed(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }
}