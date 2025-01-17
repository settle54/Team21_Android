package potatocake.katecam.everymoment.data.repository.impl

import android.util.Log
import okhttp3.MultipartBody
import potatocake.katecam.everymoment.data.model.network.api.PotatoCakeApiService
import potatocake.katecam.everymoment.data.model.network.dto.request.EmojiRequest
import potatocake.katecam.everymoment.data.model.network.dto.request.LocationNameRequest
import potatocake.katecam.everymoment.data.model.network.dto.request.ManualDiaryRequest
import potatocake.katecam.everymoment.data.model.network.dto.request.PostCategoryRequest
import potatocake.katecam.everymoment.data.model.network.dto.request.postEditDiary.PatchEditedDiaryRequest
import potatocake.katecam.everymoment.data.model.network.dto.response.CoordinatesResponse
import potatocake.katecam.everymoment.data.model.network.dto.response.DiaryResponse
import potatocake.katecam.everymoment.data.model.network.dto.response.GetCategoriesResponse
import potatocake.katecam.everymoment.data.model.network.dto.response.GetDetailDiaryResponse
import potatocake.katecam.everymoment.data.model.network.dto.response.GetFilesResponse
import potatocake.katecam.everymoment.data.model.network.dto.response.ServerResponse
import potatocake.katecam.everymoment.data.repository.DiaryRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class DiaryRepositoryImpl @Inject constructor(
    private val apiService: PotatoCakeApiService,
    @Named("jwtToken") private val token: String
) : DiaryRepository {

    override fun getDiaries(
        date: String,
        callback: (Boolean, DiaryResponse?) -> Unit
    ) {
        apiService.getDiaries(token, date).enqueue(object : Callback<DiaryResponse> {
            override fun onResponse(p0: Call<DiaryResponse>, p1: Response<DiaryResponse>) {
                if (p1.isSuccessful) {
                    Log.d("arieum", "${p1.body()}")
                    Log.d("arieum", token)
                    callback(true, p1.body())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<DiaryResponse>, p1: Throwable) {
                Log.d("arieum", "Failed to fetch diaries: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun updateBookmarkStatus(
        diaryId: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        apiService.updateBookmarkStatus(token, diaryId).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(p0: Call<ServerResponse>, p1: Response<ServerResponse>) {
                if (p1.isSuccessful) {
                    Log.d("arieum", "${p1.body()}")
                    callback(true, p1.message())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                Log.d("arieum", "Failed to update bookmark state: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun updateShareStatus(
        diaryId: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        apiService.updateShareStatus(token, diaryId).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(p0: Call<ServerResponse>, p1: Response<ServerResponse>) {
                if (p1.isSuccessful) {
                    Log.d("arieum", "${p1.body()}")
                    callback(true, p1.message())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                Log.d("arieum", "Failed to update share state: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun deleteDiary(
        diaryId: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        apiService.deleteDiary(token, diaryId).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(p0: Call<ServerResponse>, p1: Response<ServerResponse>) {
                if (p1.isSuccessful) {
                    Log.d("arieum", "${p1.body()}")
                    callback(true, p1.message())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                Log.d("arieum", "Failed to delete diary: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun getDiaryinDetail(
        diaryId: Int,
        callback: (Boolean, GetDetailDiaryResponse?) -> Unit
    ) {
        apiService.getDiaryInDetail(token, diaryId)
            .enqueue(object : Callback<GetDetailDiaryResponse> {
                override fun onResponse(
                    p0: Call<GetDetailDiaryResponse>,
                    p1: Response<GetDetailDiaryResponse>
                ) {
                    if (p1.isSuccessful) {
                        Log.d("settle54", "${p1.body()}")
                        callback.invoke(true, p1.body())
                    } else {
                        callback(false, null)
                    }
                }

                override fun onFailure(p0: Call<GetDetailDiaryResponse>, p1: Throwable) {
                    Log.d("settle54", "Failed to get diaryInDetail: ${p1.message}")
                    callback(false, null)
                }
            })
    }

    override fun postCategory(
        categoryName: String, callback: (Boolean, String?) -> Unit
    ) {
        val categoryRequest = PostCategoryRequest(categoryName)
        apiService.postCategory(token, categoryRequest).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(p0: Call<ServerResponse>, p1: Response<ServerResponse>) {
                if (p1.isSuccessful) {
                    Log.d("settle54", "${p1.body()}")
                    callback(true, p1.message())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                Log.d("settle54", "Failed to post category: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun delCategory(categoryId: Int, callback: (Boolean, String?) -> Unit) {
        apiService.delCategory(token, categoryId).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                p0: Call<ServerResponse>,
                p1: Response<ServerResponse>
            ) {
                if (p1.isSuccessful) {
                    Log.d("settle54", "${p1.body()}")
                    callback(true, p1.message())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                Log.d("settle54", "Failed to get diaryInDetail: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun getCategories(callback: (Boolean, GetCategoriesResponse?) -> Unit) {
        apiService.getCategories(token).enqueue(object : Callback<GetCategoriesResponse> {
            override fun onResponse(
                p0: Call<GetCategoriesResponse>,
                p1: Response<GetCategoriesResponse>
            ) {
                if (p1.isSuccessful) {
                    callback(true, p1.body())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<GetCategoriesResponse>, p1: Throwable) {
                callback(false, null)
            }
        })
    }

    override fun getFiles(diaryId: Int, callback: (Boolean, GetFilesResponse?) -> Unit) {
        apiService.getFiles(token, diaryId).enqueue(object : Callback<GetFilesResponse> {
            override fun onResponse(
                p0: Call<GetFilesResponse>,
                p1: Response<GetFilesResponse>
            ) {
                if (p1.isSuccessful) {
                    Log.d("settle54", "${p1.body()}")
                    callback(true, p1.body())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<GetFilesResponse>, p1: Throwable) {
                Log.d("settle54", "Failed to Get Files: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun patchFiles(diaryId: Int, files: List<MultipartBody.Part>, callback: (Boolean, String?) -> Unit) {
        val parts = files.ifEmpty {
            listOf(MultipartBody.Part.createFormData("emptyPart", ""))
        }

        apiService.patchFiles(token, diaryId, parts).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                p0: Call<ServerResponse>,
                p1: Response<ServerResponse>
            ) {
                if (p1.isSuccessful) {
                    Log.d("settle54", "postFiles: ${p1.body()}")
                    callback(true, p1.message())
                } else {
                    Log.d("settle54", "failPostFiles: ${p1.code()} - ${p1.errorBody()?.string()}")
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                Log.d("settle54", "Failed to Post Files: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun getSearchedDiaries(
        keyword: String?,
        emoji: String?,
        category: String?,
        from: String?,
        until: String?,
        bookmark: Boolean?,
        callback: (Boolean, DiaryResponse?) -> Unit
    ) {
        apiService.getSearchedDiaries(token, keyword, emoji, category, from, until, bookmark).enqueue(object : Callback<DiaryResponse> {
            override fun onResponse(p0: Call<DiaryResponse>, p1: Response<DiaryResponse>) {
                if (p1.isSuccessful) {
                    Log.d("SearchedDiary", "${p1.body()}")
                    Log.d("SearchedDiary", token)
                    callback(true, p1.body())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(p0: Call<DiaryResponse>, p1: Throwable) {
                Log.d("SearchedDiary", "Failed to search diaries: ${p1.message}")
                callback(false, null)
            }
        })
    }

    override fun patchEditedDiary(
        diaryId: Int,
        request: PatchEditedDiaryRequest,
        callback: (Boolean, String?) -> Unit
    ) {
        apiService.patchEditedDiary(token, diaryId, request)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(p0: Call<ServerResponse>, p1: Response<ServerResponse>) {
                    if (p1.isSuccessful) {
                        Log.d("settle54", "${p1.body()}")
                        callback(true, p1.message())
                    } else {
                        callback(false, null)
                    }
                }

                override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                    Log.d("settle54", "Failed to patch editedDairy: ${p1.message}")
                    callback(false, null)
                }
            })
    }

    override fun getDiaryLocation(diaryId: Int, callback: (Boolean, CoordinatesResponse?) -> Unit) {
        apiService.getDiaryLocation(token, diaryId).enqueue(object : Callback<CoordinatesResponse> {
            override fun onResponse(call: Call<CoordinatesResponse>, response: Response<CoordinatesResponse>) {
                if (response.isSuccessful) {
                    callback(true, response.body())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(call: Call<CoordinatesResponse>, t: Throwable) {
                Log.d("arieum", "Failed to fetch location: ${t.message}")
                callback(false, null)
            }
        })
    }

    override fun getDiariesWithPage(
        date: String,
        nextPage: Int,
        callback: (Boolean, DiaryResponse?) -> Unit) {
        apiService.getDiaries(token, date, nextPage).enqueue(object : Callback<DiaryResponse> {
            override fun onResponse(call: Call<DiaryResponse>, response: Response<DiaryResponse>) {
                if (response.isSuccessful) {
                    callback(true, response.body())
                } else {
                    callback(false, null)
                }
            }

            override fun onFailure(call: Call<DiaryResponse>, t: Throwable) {
                callback(false, null)
            }
        })
    }

    override fun patchEmoji(
        diaryId: Int,
        request: EmojiRequest,
        callback: (Boolean, String?) -> Unit
    ){
        apiService.patchEditedEmoji(token, diaryId, request)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(p0: Call<ServerResponse>, p1: Response<ServerResponse>) {
                    if (p1.isSuccessful) {
                        Log.d("settle54", "${p1.body()}")
                        callback(true, p1.message())
                    } else {
                        callback(false, null)
                    }
                }

                override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                    Log.d("settle54", "Failed to patch editedDairy: ${p1.message}")
                    callback(false, null)
                }
            })
    }

    override fun patchLocationName(
        diaryId: Int,
        request: LocationNameRequest,
        callback: (Boolean, String?) -> Unit
    ){
        apiService.patchEditedLocationName(token, diaryId, request)
            .enqueue(object : Callback<ServerResponse> {
                override fun onResponse(p0: Call<ServerResponse>, p1: Response<ServerResponse>) {
                    if (p1.isSuccessful) {
                        Log.d("settle54", "${p1.body()}")
                        callback(true, p1.message())
                    } else {
                        callback(false, null)
                    }
                }

                override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                    Log.d("settle54", "Failed to patch editedDairy: ${p1.message}")
                    callback(false, null)
                }
            })
    }

    override fun postNewManualDiary(
        request: ManualDiaryRequest,
        callback: (Boolean, String?) -> Unit
    ) {
        apiService.postManualDiary(token, request).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(p0: Call<ServerResponse>, p1: Response<ServerResponse>) {
                if (p1.isSuccessful) {
                    Log.d("arieum", "${p1.body()}")
                    callback(true, p1.message())
                }
            }

            override fun onFailure(p0: Call<ServerResponse>, p1: Throwable) {
                Log.d("arieum", "Failed to post manualDairy: ${p1.message}")
                callback(false, null)
            }
        })
    }
}