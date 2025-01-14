package potatocake.katecam.everymoment.presentation.view.sub.diary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import potatocake.katecam.everymoment.R
import potatocake.katecam.everymoment.data.model.network.dto.request.postEditDiary.Category
import potatocake.katecam.everymoment.data.model.network.dto.request.postEditDiary.PatchEditedDiaryRequest
import potatocake.katecam.everymoment.data.model.network.dto.vo.DetailDiary
import potatocake.katecam.everymoment.databinding.FragmentDiaryEditBinding
import potatocake.katecam.everymoment.extensions.Bookmark
import potatocake.katecam.everymoment.extensions.CategoryPopup
import potatocake.katecam.everymoment.extensions.CustomDialog
import potatocake.katecam.everymoment.extensions.EmotionPopup
import potatocake.katecam.everymoment.extensions.GalleryUtil
import potatocake.katecam.everymoment.extensions.SendFilesUtil
import potatocake.katecam.everymoment.extensions.ToPxConverter
import potatocake.katecam.everymoment.presentation.listener.OnSingleClickListener
import potatocake.katecam.everymoment.presentation.view.main.MainActivity
import potatocake.katecam.everymoment.presentation.viewModel.DiaryViewModel

@AndroidEntryPoint
class DiaryEditFragment : Fragment() {

    private lateinit var binding: FragmentDiaryEditBinding

    private var imagesList: MutableList<String> = mutableListOf()
    private var categoryList: MutableList<Category> = mutableListOf()

    private var galleryUtil = GalleryUtil(this)
    private var toPxConverter = ToPxConverter()
    private var emotionXOffset = toPxConverter.dpToPx(10)
    private var categoryYOffset = toPxConverter.dpToPx(75)

    private lateinit var emotionPopupManager: EmotionPopup
    private lateinit var categoryManager: CategoryPopup

    private var delSelectedImgNum: Int = 0
    private var delSelectedCategoryNum: Int = 0
    private lateinit var bookmark: Bookmark

    private lateinit var delEmotionDialog: CustomDialog
    private lateinit var delCategoryDialog: CustomDialog
    private lateinit var delImageDialog: CustomDialog
    private lateinit var backButtonDialog: CustomDialog

    private val viewModel: DiaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDiaryEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideNavigationBar()

        categoryManager = CategoryPopup(requireActivity(), requireActivity(), viewModel)

        bookmark = Bookmark(requireContext(), binding.bookmark)
        setButtonClickListeners()
        setEmotionPopup()
        setDialogs()
        setDiaryContent()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showNavigationBar()
    }

    private fun setDiaryContent() {
        val diary = viewModel.diary.value
        diary?.let { it ->
            if (it.emoji != null) {
                potatocake.katecam.everymoment.data.model.entity.Emotions.fromString(it.emoji)
                    ?.getEmotionUnicode()?.let { emotion ->
                        binding.emotion.text = emotion
                        binding.emotion.visibility = View.VISIBLE
                    }
                binding.addEmotion.visibility = View.GONE
            } else {
                binding.addEmotion.visibility = View.VISIBLE
            }
            binding.location.setText(it.locationName)
            binding.address.setText(it.address)
            bookmark.setBookmark(it.bookmark)
            binding.time.text = it.createAt.substring(11, 16)
            val date = it.createAt.substring(5, 10).replace("-", "월 ")
            binding.date.text = resources.getString(R.string.formatted_date, date)

            if (it.content.isNullOrEmpty()) {
                binding.content.setText("")
            } else {
                binding.content.setText(it.content)
            }

            if (it.categories.isNotEmpty()) {
                if (it.categories.size == 2) {
                    binding.category2.visibility = View.VISIBLE
                    binding.category2.text =
                        resources.getString(R.string.category_text, it.categories[1].categoryName)
                    binding.addCategory.visibility = View.INVISIBLE
                    categoryList.add(Category(it.categories[1].id))
                }
                binding.category1.visibility = View.VISIBLE
                binding.category1.text =
                    resources.getString(R.string.category_text, it.categories[0].categoryName)
                categoryList.add(Category(it.categories[0].id))
            }
        }

        setImages()
        binding.toolBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.VISIBLE
    }

    private fun setImages() {
        val filesList = viewModel.getImages()
        if (!filesList.isNullOrEmpty()) {
            binding.image1.visibility = View.VISIBLE
            binding.image1.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(requireContext()).load(filesList[0]).into(binding.image1)
            imagesList.add(filesList[0])
            binding.images.visibility = View.VISIBLE
            binding.image2.visibility = View.VISIBLE
            if (filesList!!.size == 2) {
                binding.image2.visibility = View.VISIBLE
                binding.image2.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(requireContext()).load(filesList[1]).into(binding.image2)
                imagesList.add(filesList[1])
            }
        }
    }

    private fun setEmotionPopup() {
        emotionPopupManager = EmotionPopup(requireActivity()) { emotion ->
            binding.emotion.text = emotion.getEmotionUnicode()
            binding.addEmotion.visibility = View.GONE
            binding.emotion.visibility = View.VISIBLE
        }
    }

    private fun setDialogs() {
        delImageDialog = CustomDialog(
            resources.getString(R.string.del_image_dialog),
            resources.getString(R.string.cancel),
            resources.getString(R.string.delete),
            onPositiveClick = {
                delSelectedImage()
            }
        )

        delCategoryDialog = CustomDialog(
            resources.getString(R.string.del_category_dialog),
            resources.getString(R.string.cancel),
            resources.getString(R.string.delete),
            onPositiveClick = {
                delSelectedCategory()
            }
        )

        delEmotionDialog = CustomDialog(
            resources.getString(R.string.del_emotion_dialog),
            resources.getString(R.string.cancel),
            resources.getString(R.string.delete),
            onPositiveClick = {
                delEmotion()
            }
        )

        backButtonDialog = CustomDialog(
            resources.getString(R.string.back_button_dialog),
            resources.getString(R.string.cancel),
            resources.getString(R.string.do_stop),
            onPositiveClick = {
                requireActivity().supportFragmentManager.popBackStack()
            }
        )
    }

    private fun delEmotion() {
        binding.emotion.text = null
        binding.emotion.visibility = View.GONE
        binding.addEmotion.visibility = View.VISIBLE
    }

    private fun delSelectedCategory() {
        if (delSelectedCategoryNum == 1) {
            if (categoryList.size == 2) {
                binding.category1.text = binding.category2.text
                categoryList.removeAt(1)
                binding.addCategory.visibility = View.VISIBLE
                binding.category2.visibility = View.GONE
            } else {
                binding.category1.visibility = View.GONE
                categoryList.removeAt(0)
            }
        } else if (delSelectedCategoryNum == 2) {
            binding.category2.visibility = View.GONE
            binding.addCategory.visibility = View.VISIBLE
            categoryList.removeAt(1)
        }
        Log.d("settle54", "del category: ${categoryList.joinToString(",")}")
    }

    private fun delSelectedImage() {
        if (delSelectedImgNum == 1) {
            if (imagesList.size >= 2) {
                binding.image1.setImageDrawable(binding.image2.drawable)
                binding.image2.setImageResource(R.drawable.baseline_add_circle_outline_24)
                binding.image2.scaleType = ImageView.ScaleType.CENTER
                imagesList.removeAt(1)
            } else {
                binding.image1.setImageResource(R.drawable.baseline_add_circle_outline_24)
                binding.image1.scaleType = ImageView.ScaleType.CENTER
                binding.image2.visibility = View.INVISIBLE
                imagesList.removeAt(0)
            }
        } else if (delSelectedImgNum == 2) {
            binding.image2.setImageResource(R.drawable.baseline_add_circle_outline_24)
            binding.image2.scaleType = ImageView.ScaleType.CENTER
            imagesList.removeAt(1)
        }
    }

    private fun setButtonClickListeners() {
        binding.bookmark.setOnClickListener {
            bookmark.toggleBookmark()
            viewModel.updateBookmarkStatus()
        }

        binding.image1.setOnClickListener {
            galleryUtil.openGallery(onImageSelected = {
                binding.image1.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this).load(it).into(binding.image1)
                binding.image2.visibility = View.VISIBLE
                if (imagesList.isEmpty()) {
                    imagesList.add(it.toString())
                } else {
                    imagesList[0] = it.toString()
                }
            })
        }

        binding.image2.setOnClickListener {
            galleryUtil.openGallery(onImageSelected = {
                binding.image2.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this).load(it).into(binding.image2)
                if (imagesList.size < 2) {
                    imagesList.add(it.toString())
                } else {
                    imagesList[1] = it.toString()
                }
            })
        }

        binding.image1.setOnLongClickListener {
            delImageDialog.show(requireActivity().supportFragmentManager, "delImageDialog")
            delSelectedImgNum = 1
            true
        }

        binding.image2.setOnLongClickListener {
            delImageDialog.show(requireActivity().supportFragmentManager, "delImageDialog")
            delSelectedImgNum = 2
            true
        }

        binding.addCategory.setOnClickListener {
            categoryManager.showCategoryPopup(
                binding.address,
                0,
                categoryYOffset,
                onCategorySelected = { categoryName, categoryId ->
                    addCategory(categoryName, categoryId)
                })
        }

        binding.category1.setOnClickListener {
            categoryManager.showCategoryPopup(
                binding.address,
                0,
                categoryYOffset,
                onCategorySelected = { categoryName, categoryId ->
                    binding.category1.text = categoryName
                    categoryList[0] = Category(categoryId!!)
                    Log.d("settle54", "change category: ${categoryList.joinToString(",")}")
                })
        }

        binding.category2.setOnClickListener {
            categoryManager.showCategoryPopup(
                binding.address,
                0,
                categoryYOffset,
                onCategorySelected = { categoryName, categoryId ->
                    binding.category2.text = categoryName
                    categoryList[1] = Category(categoryId!!)
                    Log.d("settle54", "change category: ${categoryList.joinToString(",")}")
                })
        }

        binding.category1.setOnLongClickListener {
            delCategoryDialog.show(
                requireActivity().supportFragmentManager,
                "delCategoryDialog"
            )
            delSelectedCategoryNum = 1
            true
        }

        binding.category2.setOnLongClickListener {
            delCategoryDialog.show(
                requireActivity().supportFragmentManager,
                "delCategoryDialog"
            )
            delSelectedCategoryNum = 2
            true
        }

        binding.addEmotion.setOnClickListener {
            emotionPopupManager.showEmotionsPopup(binding.addEmotion, emotionXOffset)
        }

        binding.emotion.setOnClickListener {
            emotionPopupManager.showEmotionsPopup(binding.emotion, emotionXOffset)
        }

        binding.emotion.setOnLongClickListener {
            delEmotionDialog.show(
                requireActivity().supportFragmentManager,
                "delEmotionDialog"
            )
            true
        }

        binding.diaryDoneButton.setOnClickListener(object: OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                if (!checkLocationAndAddress()) {
                    return
                }

                patchViewModelDiary()
                patchViewModelFiles()

                patchEditedDiary { successDiary ->
                    Log.d("successDiary", "$successDiary")
                    if (successDiary) {
                        patchFiles {
                            Log.d("patchFiles", "$it")
                            if (it && activity != null && requireActivity().supportFragmentManager.backStackEntryCount != 0)
                                requireActivity().supportFragmentManager.popBackStack()
                        }
                    }
                }
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            backButtonDialog.show(
                requireActivity().supportFragmentManager,
                "delEmotionDialog"
            )
        }
    }

    private fun patchViewModelFiles() {
        viewModel.patchViewModelFiles(imagesList)
    }

    private fun patchFiles(callback: (Boolean) -> Unit) {
        SendFilesUtil.uriToFile(requireContext(), "files", imagesList) { images ->
            viewModel.patchFiles(images) { success ->
                callback(success)
            }
        }
    }

    private fun checkLocationAndAddress(): Boolean {
        val locationText = binding.location.text.toString()
        val addressText = binding.address.text.toString()

        return when {
            notExistTextViewText(locationText) || notExistTextViewText(addressText) -> {
                showToast(R.string.locantion_and_address_not_blank)
                false
            }

            locationText.length > 30 -> {
                showToast(R.string.location_text_length)
                false
            }

            addressText.length > 100 -> {
                showToast(R.string.address_text_length)
                false
            }

            else -> true
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun patchViewModelDiary() {
        Log.d("settle54", "${categoryList.joinToString(",")}")
        val diary = DetailDiary(
            address = binding.address.text.toString(),
            bookmark = bookmark.checkBookmarked(),
            categories = categoryList.map { category ->
                potatocake.katecam.everymoment.data.model.network.dto.vo.Category(
                    viewModel.getCategoryName(category.categoryId)!!,
                    category.categoryId
                )
            },
            content = binding.content.text.toString(),
            createAt = viewModel.diary.value!!.createAt,
            emoji = potatocake.katecam.everymoment.data.model.entity.Emotions.getEmotionNameInLowerCase(
                binding.emotion.text.toString()
            ),
            id = viewModel.getDiaryId(),
            locationName = binding.location.text.toString()
        )
        Log.d("settle54", "view: $diary")
        viewModel.patchViewModelDiary(diary)
    }

    private fun patchEditedDiary(callback: (Boolean) -> Unit) {
        val notEmotion = notExistTextViewText(binding.emotion.text.toString())
        val notContent = notExistTextViewText(binding.content.text.toString())
        val notCategory = categoryList.isEmpty()

        val request = PatchEditedDiaryRequest(
            address = binding.address.text.toString(),
            categories = categoryList,
            content = binding.content.text.toString(),
            contentDelete = notContent,
            deleteAllCategories = notCategory,
            emoji = potatocake.katecam.everymoment.data.model.entity.Emotions.getEmotionNameInLowerCase(
                binding.emotion.text.toString()
            ),
            emojiDelete = notEmotion,
            locationName = binding.location.text.toString(),
        )
        Log.d("settle54", "patch server: $request")
        viewModel.patchEditedDiary(request) { success ->
            callback(success)
        }
    }

    private fun notExistTextViewText(text: String): Boolean {
        if (text.isNullOrEmpty()) {
            return true
        } else {
            return false
        }
    }

    private fun addCategory(category: String?, categoryId: Int?) {
        if (categoryList.isEmpty()) {
            binding.category1.visibility = View.VISIBLE
            binding.category1.text = category
            categoryList.add(Category(categoryId!!))
        } else if (categoryList.size < 2) {
            binding.category2.visibility = View.VISIBLE
            binding.category2.text = category
            categoryList.add(Category(categoryId!!))
            binding.addCategory.visibility = View.GONE
        } else {
            binding.category2.text = category
            categoryList[1] = (Category(categoryId!!))
        }
        Log.d("settle54", "add category: ${categoryList.joinToString(",")}")
    }
}
