# 🤝 Contributing Guide

Panduan lengkap untuk berkontribusi pada Local YouTube Downloader Android project.

## 🎯 Welcome Contributors!

Terima kasih atas minat Anda untuk berkontribusi! Project ini terbuka untuk semua jenis kontribusi, mulai dari bug reports, feature requests, documentation improvements, hingga code contributions.

## 📋 Table of Contents

- [Getting Started](#getting-started)
- [Types of Contributions](#types-of-contributions)
- [Development Setup](#development-setup)
- [Code Guidelines](#code-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)
- [Community Guidelines](#community-guidelines)

## 🚀 Getting Started

### Prerequisites
Sebelum berkontribusi, pastikan Anda memiliki:
- **GitHub account** untuk fork dan pull requests
- **Android Studio** Flamingo atau lebih baru
- **Git** untuk version control
- **Basic knowledge** of Kotlin dan Android development

### First Steps
1. **⭐ Star the repository** jika Anda menyukai project ini
2. **🍴 Fork the repository** ke akun GitHub Anda
3. **📖 Read the documentation** untuk memahami project
4. **🔍 Browse existing issues** untuk melihat apa yang bisa dikerjakan

## 🎨 Types of Contributions

### 🐛 Bug Reports
Laporkan bugs yang Anda temukan dengan informasi detail.

**What makes a good bug report:**
- Clear, descriptive title
- Steps to reproduce
- Expected vs actual behavior
- Device/OS information
- Screenshots/logs if applicable

### 💡 Feature Requests
Usulkan fitur baru yang akan meningkatkan aplikasi.

**What makes a good feature request:**
- Clear problem statement
- Proposed solution
- Use cases and benefits
- Mockups/wireframes (optional)

### 📚 Documentation
Improve documentation, tutorials, atau wiki pages.

**Documentation contributions:**
- Fix typos or unclear explanations
- Add missing documentation
- Create tutorials or guides
- Translate documentation

### 💻 Code Contributions
Contribute code untuk bug fixes, new features, atau improvements.

**Code contribution areas:**
- Bug fixes
- New features
- Performance improvements
- UI/UX enhancements
- Test coverage
- Refactoring

### 🧪 Testing
Help improve test coverage dan quality assurance.

**Testing contributions:**
- Write unit tests
- Create integration tests
- Manual testing on different devices
- Performance testing

## 🛠️ Development Setup

### 1. Fork and Clone
```bash
# Fork repository di GitHub, kemudian clone
git clone https://github.com/YOUR_USERNAME/Local-Youtube-Downloader-Android.git
cd Local-Youtube-Downloader-Android

# Add upstream remote
git remote add upstream https://github.com/ORIGINAL_OWNER/Local-Youtube-Downloader-Android.git
```

### 2. Setup Development Environment
```bash
# Open project di Android Studio
# Atau via command line:
./gradlew build

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### 3. Create Development Branch
```bash
# Update main branch
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/your-feature-name
# atau
git checkout -b bugfix/issue-number-description
```

### 4. Make Changes
- Follow [Code Guidelines](#code-guidelines)
- Write tests for new functionality
- Update documentation if needed
- Test your changes thoroughly

### 5. Commit and Push
```bash
# Stage changes
git add .

# Commit with descriptive message
git commit -m "feat: add video quality selection feature"

# Push to your fork
git push origin feature/your-feature-name
```

## 📝 Code Guidelines

### Kotlin Style Guide
Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ Good
class DownloadRepository(private val context: Context) {
    
    suspend fun getVideoInfo(url: String): Result<VideoInfo> {
        return try {
            // Implementation
            Result.success(videoInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ❌ Bad
class downloadRepository(context:Context){
    suspend fun getVideoInfo(url:String):Result<VideoInfo>{
        try{
            // Implementation
            return Result.success(videoInfo)
        }catch(e:Exception){
            return Result.failure(e)
        }
    }
}
```

### Architecture Guidelines

#### MVVM Pattern
```kotlin
// ✅ Good - Proper separation of concerns
class DownloadViewModel : ViewModel() {
    private val repository = DownloadRepository()
    
    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()
    
    fun startDownload() {
        viewModelScope.launch {
            repository.downloadVideo(request).collect { state ->
                _downloadState.value = state
            }
        }
    }
}
```

#### Compose Best Practices
```kotlin
// ✅ Good - Stateless composable
@Composable
fun VideoInfoCard(
    videoInfo: VideoInfo,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        // UI implementation
    }
}

// ❌ Bad - Stateful composable with business logic
@Composable
fun VideoInfoCard(url: String) {
    var videoInfo by remember { mutableStateOf<VideoInfo?>(null) }
    
    LaunchedEffect(url) {
        // Don't do business logic in composables
        videoInfo = repository.getVideoInfo(url)
    }
}
```

### Naming Conventions

#### Classes
```kotlin
// ✅ Good
class DownloadRepository
class YouTubeExtractorService
class DownloadViewModel

// ❌ Bad
class downloadRepo
class youtubeService
class VM
```

#### Functions
```kotlin
// ✅ Good
fun getVideoInfo()
fun startDownload()
fun validateUrl()

// ❌ Bad
fun getInfo()
fun start()
fun validate()
```

#### Variables
```kotlin
// ✅ Good
val downloadState: StateFlow<DownloadState>
val videoInfo: VideoInfo?
val isLoading: Boolean

// ❌ Bad
val state: StateFlow<DownloadState>
val info: VideoInfo?
val loading: Boolean
```

### Error Handling
```kotlin
// ✅ Good - Use Result type for operations that can fail
suspend fun getVideoInfo(url: String): Result<VideoInfo> {
    return try {
        val info = youtubeExtractor.extractVideoInfo(url)
        Result.success(info)
    } catch (e: Exception) {
        Result.failure(AppError.NetworkError("Failed to get video info: ${e.message}"))
    }
}

// ✅ Good - Handle errors gracefully in UI
when (val result = repository.getVideoInfo(url)) {
    is Result.Success -> {
        _uiState.value = _uiState.value.copy(
            videoInfo = result.data,
            isLoading = false
        )
    }
    is Result.Failure -> {
        _uiState.value = _uiState.value.copy(
            errorMessage = result.error.message,
            isLoading = false
        )
    }
}
```

### Testing Guidelines

#### Unit Tests
```kotlin
class DownloadRepositoryTest {
    
    @Test
    fun `getVideoInfo returns success for valid URL`() = runTest {
        // Given
        val repository = DownloadRepository(mockContext)
        val validUrl = "https://youtube.com/watch?v=dQw4w9WgXcQ"
        
        // When
        val result = repository.getVideoInfo(validUrl)
        
        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun `getVideoInfo returns failure for invalid URL`() = runTest {
        // Given
        val repository = DownloadRepository(mockContext)
        val invalidUrl = "invalid-url"
        
        // When
        val result = repository.getVideoInfo(invalidUrl)
        
        // Then
        assertTrue(result.isFailure)
    }
}
```

#### Integration Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class DownloadIntegrationTest {
    
    @Test
    fun downloadFlow_completesSuccessfully() {
        // Test complete download flow
        val scenario = launchActivity<MainActivity>()
        
        onView(withId(R.id.url_input))
            .perform(typeText("https://youtube.com/watch?v=dQw4w9WgXcQ"))
        
        onView(withId(R.id.download_button))
            .perform(click())
        
        // Verify download completes
        onView(withText("Download selesai"))
            .check(matches(isDisplayed()))
    }
}
```

## 🔄 Pull Request Process

### 1. Pre-submission Checklist
- [ ] Code follows style guidelines
- [ ] Tests pass locally (`./gradlew test`)
- [ ] New functionality has tests
- [ ] Documentation updated if needed
- [ ] No merge conflicts with main branch
- [ ] Commit messages are descriptive

### 2. Create Pull Request
1. **Push your branch** to your fork
2. **Create PR** from your branch to `main` branch
3. **Fill PR template** with required information
4. **Link related issues** using keywords (fixes #123)

### 3. PR Template
```markdown
## Description
Brief description of changes made.

## Type of Change
- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Screenshots (if applicable)
Add screenshots to help explain your changes.

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
```

### 4. Review Process
1. **Automated checks** run (CI/CD)
2. **Code review** by maintainers
3. **Address feedback** if requested
4. **Approval and merge** by maintainers

### 5. After Merge
```bash
# Update your local main branch
git checkout main
git pull upstream main

# Delete feature branch
git branch -d feature/your-feature-name
git push origin --delete feature/your-feature-name
```

## 📋 Issue Guidelines

### Bug Report Template
```markdown
**Bug Description**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected Behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Device Information:**
 - Device: [e.g. Samsung Galaxy S21]
 - OS: [e.g. Android 12]
 - App Version: [e.g. 1.0.0]

**Additional Context**
Add any other context about the problem here.
```

### Feature Request Template
```markdown
**Is your feature request related to a problem? Please describe.**
A clear and concise description of what the problem is. Ex. I'm always frustrated when [...]

**Describe the solution you'd like**
A clear and concise description of what you want to happen.

**Describe alternatives you've considered**
A clear and concise description of any alternative solutions or features you've considered.

**Additional context**
Add any other context or screenshots about the feature request here.
```

## 🏷️ Labels and Milestones

### Issue Labels
- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Improvements or additions to documentation
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention is needed
- `question` - Further information is requested
- `wontfix` - This will not be worked on

### Priority Labels
- `priority: high` - Critical issues
- `priority: medium` - Important issues
- `priority: low` - Nice to have

### Status Labels
- `status: in progress` - Currently being worked on
- `status: needs review` - Waiting for review
- `status: blocked` - Blocked by other issues

## 👥 Community Guidelines

### Code of Conduct
We follow the [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/version/2/1/code_of_conduct/):

#### Our Pledge
- Be welcoming and inclusive
- Be respectful of differing viewpoints
- Accept constructive criticism gracefully
- Focus on what is best for the community

#### Our Standards
**Examples of behavior that contributes to a positive environment:**
- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

**Examples of unacceptable behavior:**
- The use of sexualized language or imagery
- Trolling, insulting/derogatory comments, and personal or political attacks
- Public or private harassment
- Publishing others' private information without explicit permission

### Communication Guidelines

#### GitHub Issues
- **Search existing issues** before creating new ones
- **Use clear, descriptive titles**
- **Provide detailed information** in issue descriptions
- **Be patient** - maintainers are volunteers
- **Be respectful** in all interactions

#### Pull Requests
- **Keep PRs focused** - one feature/fix per PR
- **Write clear commit messages**
- **Respond to feedback** constructively
- **Be patient** during review process

#### Discussions
- **Stay on topic** in discussions
- **Help other contributors** when possible
- **Share knowledge** and experiences
- **Be constructive** in feedback

## 🎉 Recognition

### Contributors
All contributors are recognized in:
- **README.md** contributors section
- **Release notes** for significant contributions
- **GitHub contributors** page

### Types of Recognition
- **Code contributors** - Direct code contributions
- **Documentation contributors** - Documentation improvements
- **Community contributors** - Helping others, reporting bugs
- **Design contributors** - UI/UX improvements

## 📞 Getting Help

### Where to Ask Questions
1. **GitHub Discussions** - General questions and discussions
2. **GitHub Issues** - Bug reports and feature requests
3. **Wiki** - Documentation and guides

### Mentorship
New contributors can get help from:
- **Good first issues** - Labeled for beginners
- **Maintainer guidance** - Ask questions in issues/PRs
- **Community support** - Other contributors helping

## 🚀 Next Steps

After reading this guide:
1. **Set up development environment**
2. **Look for "good first issue" labels**
3. **Join GitHub Discussions**
4. **Start with small contributions**
5. **Ask questions when needed**

---

**Thank you for contributing!** 🙏 Your contributions make this project better for everyone.

**Questions?** Feel free to ask in [GitHub Discussions](https://github.com/yourusername/Local-Youtube-Downloader-Android/discussions) or create an issue.
