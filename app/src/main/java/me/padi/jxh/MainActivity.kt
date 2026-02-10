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
import me.padi.jxh.core.ui.ClassListPage
import me.padi.jxh.core.ui.CoursePage
import me.padi.jxh.core.ui.HomePage
import me.padi.jxh.core.ui.LoginPage
import me.padi.jxh.core.ui.MapPage
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
    data object Map : Screen
    data object Egg : Screen
    data object About : Screen
    data object ClassList : Screen
    data class Course(val params: ClassParams) : Screen
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun App() {
    val backStack = remember { mutableStateListOf<NavKey>(Screen.Login) }

    val entryProvider = remember(backStack) {
        entryProvider<NavKey> {
            entry(Screen.Login) {
                LoginPage {
                    backStack.clear()
                    backStack.add(Screen.Home)
                }
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


            entry(Screen.Map) {
                MapPage {
                    if (backStack.isNotEmpty()) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
            }
            entry<Screen.Score> {
                ScorePage {
                    if (backStack.isNotEmpty()) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
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
