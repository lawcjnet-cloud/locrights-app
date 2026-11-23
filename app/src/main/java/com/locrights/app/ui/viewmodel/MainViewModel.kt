package com.locrights.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.locrights.app.AppContainer
import com.locrights.app.data.local.ThemeMode
import com.locrights.app.data.local.UserSettings
import com.locrights.app.data.repository.BlogRepository
import com.locrights.app.domain.model.Article
import com.locrights.app.domain.model.BlogPage
import com.locrights.app.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val articles: List<Article> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val totalResults: Int = 0,
    val hasMore: Boolean = false,
    val fromCache: Boolean = false,
    val errorMessage: String? = null
)

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<Article> = emptyList(),
    val hasSearched: Boolean = false,
    val errorMessage: String? = null
)

data class ArticleDetailUiState(
    val article: Article? = null,
    val relatedArticles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class AboutUiState(
    val isLoading: Boolean = true,
    val pages: List<BlogPage> = emptyList(),
    val errorMessage: String? = null
)

class MainViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val repository: BlogRepository = container.blogRepository
    private val themePrefs = container.themePreferences
    private val networkMonitor: NetworkMonitor = container.networkMonitor

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val settings: StateFlow<UserSettings> = themePrefs.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val bookmarks: StateFlow<List<Article>> = repository.bookmarkedArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkIds: StateFlow<Set<String>> = repository.bookmarkIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _detailState = MutableStateFlow(ArticleDetailUiState())
    val detailState: StateFlow<ArticleDetailUiState> = _detailState.asStateFlow()

    private val _aboutState = MutableStateFlow(AboutUiState())
    val aboutState: StateFlow<AboutUiState> = _aboutState.asStateFlow()

    private var searchJob: Job? = null
    private val pageSize = 15

    init {
        loadFeed(reset = true)
        loadBlogPages()
    }

    fun loadFeed(reset: Boolean = false, isPullToRefresh: Boolean = false) {
        val current = _homeState.value
        val category = current.selectedCategory
        val startIndex = if (reset) 1 else current.articles.size + 1

        if (!reset && (current.isLoadingMore || !current.hasMore)) return

        viewModelScope.launch {
            _homeState.update {
                if (reset) {
                    it.copy(
                        isLoading = !isPullToRefresh && it.articles.isEmpty(),
                        isRefreshing = isPullToRefresh,
                        errorMessage = null
                    )
                } else {
                    it.copy(isLoadingMore = true, errorMessage = null)
                }
            }

            repository.fetchArticles(
                startIndex = startIndex,
                maxResults = pageSize,
                category = category
            ).onSuccess { result ->
                _homeState.update { prev ->
                    val merged = if (reset) {
                        result.articles
                    } else {
                        (prev.articles + result.articles).distinctBy { it.id }
                    }
                    val allCategories = if (prev.categories.isEmpty() && result.categories.isNotEmpty()) {
                        result.categories
                    } else if (category == null && result.categories.isNotEmpty()) {
                        result.categories
                    } else {
                        prev.categories
                    }
                    prev.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        articles = merged,
                        categories = allCategories,
                        totalResults = result.totalResults,
                        hasMore = !result.fromCache && merged.size < result.totalResults && result.articles.isNotEmpty(),
                        fromCache = result.fromCache,
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _homeState.update { prev ->
                    prev.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = err.localizedMessage ?: "Unable to connect to locrights-23.blogspot.com"
                    )
                }
            }
        }
    }

    fun selectCategory(category: String?) {
        if (_homeState.value.selectedCategory == category) return
        _homeState.update { it.copy(selectedCategory = category, articles = emptyList(), hasMore = false) }
        loadFeed(reset = true)
    }

    fun openArticle(articleId: String) {
        viewModelScope.launch {
            _detailState.value = ArticleDetailUiState(isLoading = true)
            val article = repository.getArticleById(articleId)
            if (article != null) {
                val related = repository.getRelatedArticles(article)
                _detailState.value = ArticleDetailUiState(
                    article = article,
                    relatedArticles = related,
                    isLoading = false
                )
            } else {
                _detailState.value = ArticleDetailUiState(
                    isLoading = false,
                    errorMessage = "Article not found in feed cache."
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchState.value = SearchUiState(query = query)
            return
        }
        searchJob = viewModelScope.launch {
            delay(320)
            executeSearch(query)
        }
    }

    fun executeSearch(query: String = _searchState.value.query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _searchState.update { it.copy(query = query, isSearching = true, errorMessage = null) }
            repository.searchArticles(trimmed)
                .onSuccess { list ->
                    _searchState.update {
                        it.copy(isSearching = false, results = list, hasSearched = true)
                    }
                }
                .onFailure { err ->
                    _searchState.update {
                        it.copy(
                            isSearching = false,
                            hasSearched = true,
                            errorMessage = err.localizedMessage ?: "Search failed"
                        )
                    }
                }
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
        }
    }

    fun removeBookmark(articleId: String) {
        viewModelScope.launch {
            repository.removeBookmark(articleId)
        }
    }

    fun loadBlogPages() {
        viewModelScope.launch {
            _aboutState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.fetchBlogPages()
                .onSuccess { pages ->
                    _aboutState.value = AboutUiState(isLoading = false, pages = pages)
                }
                .onFailure { err ->
                    _aboutState.value = AboutUiState(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Unable to fetch blog pages"
                    )
                }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePrefs.setThemeMode(mode) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { themePrefs.setFontScale(scale) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { themePrefs.setNotificationsEnabled(enabled) }
    }
}

class MainViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(container) as T
    }
}
