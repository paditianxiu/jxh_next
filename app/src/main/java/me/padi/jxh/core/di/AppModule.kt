package me.padi.jxh.core.di

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
    single { ApiClient.client }
    single { NetworkDataSource(get()) }
}

val repositoryModule = module {
    single { LoginRepository(get()) }
    single { ScoreRepository(get()) }
    single { CourseRepository(get()) }
}

val viewModelModule = module {
    viewModel {
        LoginViewModel(
            loginRepository = get()
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


val appModule = module {
    includes(
        networkModule, repositoryModule, viewModelModule
    )
}