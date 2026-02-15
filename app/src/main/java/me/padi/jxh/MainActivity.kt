package me.padi.jxh

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import jnu.kulipai.exam.ui.screens.egg.EmojiEasterEggPage
import me.padi.jxh.core.ui.AboutPage
import me.padi.jxh.core.ui.CampusLifePage
import me.padi.jxh.core.ui.ClassListPage
import me.padi.jxh.core.ui.CoursePage
import me.padi.jxh.core.ui.CourseSetting
import me.padi.jxh.core.ui.HomePage
import me.padi.jxh.core.ui.ImagePage
import me.padi.jxh.core.ui.LoginPage
import me.padi.jxh.core.ui.NewsDetailPage
import me.padi.jxh.core.ui.PdfReaderView
import me.padi.jxh.core.ui.ScorePage
import me.padi.jxh.data.repository.ClassParams
import me.padi.jxh.ui.theme.Theme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            Theme {
                App()
            }
        }
    }
}


sealed interface Screen : NavKey {
    data object Login : Screen
    data object Score : Screen
    data object Home : Screen
    data class Image(
        val title: String, val url: String, val backStack: MutableList<NavKey>
    ) : Screen {
        override fun toString(): String {
            return "Screen.Image(title='$title', url='$url', backStackSize=${backStack.size})"
        }
    }

    data object Egg : Screen
    data object About : Screen
    data object ClassList : Screen
    data class Course(val params: ClassParams) : Screen
    data object CourseSetting : Screen
    data object CampusLife : Screen
    data class NewsDetail(val url: String) : Screen
    data class PdfReader(val title: String, val url: String, val backStack: MutableList<NavKey>) :
        Screen {
        override fun toString(): String {
            return "Screen.PdfReader(title='$title', url='$url', backStackSize=${backStack.size})"
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    val backStack = remember { mutableStateListOf<NavKey>(Screen.Login) }

    val entryProvider = remember(backStack) {
        entryProvider<NavKey> {
            entry(Screen.Login) {
                LoginPage(backStack)
            }
            entry(Screen.Home) {
                HomePage(backStack)
            }
            entry(Screen.Egg) {
                EmojiEasterEggPage()
            }
            entry(Screen.ClassList) {
                ClassListPage(backStack)
            }
            entry(Screen.About) {
                AboutPage(backStack)
            }
            entry<Screen.Course> { screen ->
                CoursePage(
                    screen.params, backStack
                )
            }

            entry<Screen.NewsDetail> { screen ->
                NewsDetailPage(
                    screen.url, backStack
                )
            }
            entry(Screen.CourseSetting) {
                CourseSetting(backStack)
            }
            entry<Screen.Image> { screen ->
                ImagePage(screen.title, screen.url, screen.backStack)
            }

            entry<Screen.PdfReader> { screen ->
                PdfReaderView(screen.title, screen.url, screen.backStack)
            }
            entry<Screen.Score> {
                ScorePage(backStack)
            }
            entry(Screen.CampusLife) {
                CampusLifePage(backStack)
            }

        }
    }

    val entries = rememberDecoratedNavEntries(
        entryProvider = entryProvider,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
        ),
    )

    NavDisplay(
        entries = entries, onBack = {
            if (backStack.isNotEmpty()) {
                backStack.removeAt(backStack.lastIndex)
            }
        })
}
