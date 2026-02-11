package me.padi.jxh.core.di

import android.os.Build
import androidx.annotation.RequiresApi
import me.padi.jxh.core.model.ClassListViewModel
import me.padi.jxh.core.model.CourseViewModel
import me.padi.jxh.core.model.LoginViewModel
import me.padi.jxh.core.model.ScoreViewModel
import me.padi.jxh.core.network.ApiClient
import me.padi.jxh.core.network.NetworkDataSource
import me.padi.jxh.data.repository.CourseRepository
import me.padi.jxh.data.repository.LoginRepository
import me.padi.jxh.data.repository.ScoreRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    single { ApiClient() }
    single { get<ApiClient>().client }
    single { NetworkDataSource(get()) }
}

val repositoryModule = module {
    single { LoginRepository(get(), get()) }
    single { ScoreRepository(get()) }
    single { CourseRepository(get()) }
}

@RequiresApi(Build.VERSION_CODES.O)
val viewModelModule = module {
    viewModel {
        LoginViewModel(
            loginRepository = get(), apiClient = get()
        )
    }

    viewModel {
        ScoreViewModel(
            scoreRepository = get()
        )
    }

    viewModel {
        CourseViewModel(
            courseRepository = get()
        )
    }

    viewModel {
        ClassListViewModel(
            courseRepository = get()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
val appModule = module {
    includes(
        networkModule, repositoryModule, viewModelModule
    )
}