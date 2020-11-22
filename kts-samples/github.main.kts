// Try "%KOTLIN_HOME%\bin\kotlinc" -cp "%KOTLIN_HOME%/lib/kotlin-main-kts.jar" -script github.main.kts twisterrob

@file:Suppress("RedundantSuspendModifier")
@file:Repository("https://jcenter.bintray.com")
// prod
// These coroutines don't work because kotlin-main-kts has embedded coroutines
// see https://youtrack.jetbrains.com/issue/KT-30210
//@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")
//@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.1.1")
@file:DependsOn("com.google.code.gson:gson:2.8.0")
@file:DependsOn("io.reactivex:rxjava:1.3.8")
@file:DependsOn("com.squareup.okhttp3:okhttp:3.12.0")
@file:DependsOn("com.squareup.okio:okio:1.15.0")
@file:DependsOn("com.squareup.retrofit2:retrofit:2.5.0")
@file:DependsOn("com.squareup.retrofit2:converter-gson:2.5.0")
@file:DependsOn("com.squareup.retrofit2:adapter-rxjava:2.5.0")
// TODO https://github.com/square/retrofit/pull/2886 merged 2019-02, will be in Retrofit 2.6
//@file:DependsOn("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
// test
@file:DependsOn("junit:junit:4.13-beta-2")
@file:DependsOn("org.hamcrest:hamcrest-all:1.3")
@file:DependsOn("com.flextrade.jfixture:jfixture:2.7.2")
@file:DependsOn("org.mockito:mockito-core:2.24.5")
@file:DependsOn("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
@file:DependsOn("net.bytebuddy:byte-buddy:1.9.7")
@file:DependsOn("net.bytebuddy:byte-buddy-agent:1.9.7")
@file:DependsOn("org.objenesis:objenesis:2.6")

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.JFixture
import com.flextrade.jfixture.annotations.Fixture
import com.google.gson.annotations.SerializedName
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.junit.runner.JUnitCore
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import rx.Single
import rx.schedulers.Schedulers
import kotlin.system.exitProcess

//Class.forName("kotlinx.coroutines.BuildersKt").declaredMethods.forEach(::println)


class Script(
	private val github: GitHub,
	private val output: (String) -> Unit
) {

	companion object {
		fun di(userName: String) {
			val retrofit = Retrofit.Builder()
				.baseUrl("https://api.github.com/")
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				//.addCallAdapterFactory(CoroutineCallAdapterFactory())
				.build()
			val github = retrofit.create(GitHub::class.java)

			Script(github, ::println).start(userName)
		}
	}

	fun start(userName: String) {
		val user = github.user(userName)
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.trampoline())
			.toBlocking() // because background threads are daemon, need to keep alive
			.value()
		handleUser(user)
	}

	private fun handleUser(user: GitHub.User) {
		output("${user.login}: ${user.name} works at ${user.company}")
	}

	@Suppress("FunctionName", "unused")
	class ScriptTest {

		@Mock lateinit var mockGithub: GitHub

		@Fixture lateinit var fixtUser: GitHub.User

		private lateinit var fixture: JFixture
		private lateinit var sut: Script

		@Before fun setUp() {
			fixture = JFixture()
			FixtureAnnotations.initFixtures(this, fixture)
			MockitoAnnotations.initMocks(this)
			sut = Script(mockGithub, ::capture)
		}

		private val captured = mutableListOf<String>()
		private fun capture(output: String) {
			captured.add(output)
		}
	}
}

interface GitHub {

	@GET("users/{user}")
	fun user(@Path("user") userName: String): Single<User>

	data class User(
		@SerializedName("login")
		val login: String,
		@SerializedName("id")
		val id: String,
		@SerializedName("node_id")
		val nodeId: String,
		@SerializedName("avatar_url")
		val avatarUrl: String,
		@SerializedName("gravatar_id")
		val gravatarId: String,
		@SerializedName("url")
		val url: String,
		@SerializedName("html_url")
		val htmlUrl: String,
		@SerializedName("followers_url")
		val followersUrl: String,
		@SerializedName("following_url")
		val followingUrl: String,
		@SerializedName("gists_url")
		val gistsUrl: String,
		@SerializedName("starred_url")
		val starredUrl: String,
		@SerializedName("subscriptions_url")
		val subscriptionsUrl: String,
		@SerializedName("organizations_url")
		val organizationsUrl: String,
		@SerializedName("repos_url")
		val reposUrl: String,
		@SerializedName("events_url")
		val eventsUrl: String,
		@SerializedName("received_events_url")
		val receivedEventsUrl: String,
		@SerializedName("type")
		val type: String,
		@SerializedName("site_admin")
		val siteAdmin: Boolean,
		@SerializedName("name")
		val name: String,
		@SerializedName("company")
		val company: String,
		@SerializedName("blog")
		val blog: String,
		@SerializedName("location")
		val location: String,
		@SerializedName("email")
		val email: String,
		@SerializedName("hireable")
		val hireable: Boolean,
		@SerializedName("bio")
		val bio: String,
		@SerializedName("public_repos")
		val publicRepos: Int,
		@SerializedName("public_gists")
		val publicGists: Int,
		@SerializedName("followers")
		val followers: Int,
		@SerializedName("following")
		val following: Int,
		@SerializedName("created_at")
		val createdAt: String,
		@SerializedName("updated_at")
		val updatedAt: String
	)
}

fun help() {
	val dollar = '$'
	println(
		"""
			Usage
			 * Windows: "%KOTLIN_HOME%\bin\kotlinc" -cp "%KOTLIN_HOME%/lib/kotlin-main-kts.jar" -script github.main.kts <username>
			 * Unix: "${dollar}{KOTLIN_HOME}/bin/kotlinc" -cp "${dollar}{KOTLIN_HOME}/lib/kotlin-main-kts.jar" -script github.main.kts <username>
		""".trimIndent()
	)
}

fun test() {
	JUnitCore.runClasses(
		Script.ScriptTest::class.java
	).run {
		val stats = "in ${runTime}ms: ran ${runCount} (ignored ${ignoreCount}), failed ${failureCount}"
		if (wasSuccessful()) {
			println("Self test complete $stats")
		} else {
			println("Self test failed $stats")
			failures.forEach(::println)
		}
	}
} 